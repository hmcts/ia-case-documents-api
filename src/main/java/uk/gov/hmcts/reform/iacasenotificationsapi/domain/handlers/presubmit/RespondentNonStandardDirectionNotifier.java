package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RespondentDirectionPersonalisationFactory;

@Component
public class RespondentNonStandardDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final List<State> allowedCaseStates =
        Arrays.asList(
            State.APPEAL_SUBMITTED,
            State.APPEAL_SUBMITTED_OUT_OF_TIME,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING
        );

    private final String respondentNonStandardDirectionTemplateId;
    private final String respondentNonStandardDirectionEmailAddress;
    private final RespondentDirectionPersonalisationFactory respondentDirectionPersonalisationFactory;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;

    public RespondentNonStandardDirectionNotifier(
        @Value("${govnotify.template.respondentNonStandardDirection}") String respondentNonStandardDirectionTemplateId,
        @Value("${respondentEmailAddresses.nonStandardDirectionUntilListing}") String respondentNonStandardDirectionEmailAddress,
        RespondentDirectionPersonalisationFactory respondentDirectionPersonalisationFactory,
        DirectionFinder directionFinder,
        NotificationSender notificationSender
    ) {
        requireNonNull(respondentNonStandardDirectionTemplateId, "respondentNonStandardDirectionTemplateId must not be null");
        requireNonNull(respondentNonStandardDirectionEmailAddress, "respondentNonStandardDirectionEmailAddress must not be null");

        this.respondentNonStandardDirectionTemplateId = respondentNonStandardDirectionTemplateId;
        this.respondentNonStandardDirectionEmailAddress = respondentNonStandardDirectionEmailAddress;
        this.respondentDirectionPersonalisationFactory = respondentDirectionPersonalisationFactory;
        this.directionFinder = directionFinder;
        this.notificationSender = notificationSender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        final State caseState =
            callback
                .getCaseDetails()
                .getState();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SEND_DIRECTION
               && allowedCaseStates.contains(caseState);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        Direction nonStandardDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        if (!nonStandardDirection.getParties().equals(Parties.RESPONDENT)) {
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        Map<String, String> personalisation =
            respondentDirectionPersonalisationFactory
                .create(asylumCase, nonStandardDirection);

        String reference =
            callback.getCaseDetails().getId()
            + "_RESPONDENT_NON_STANDARD_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                respondentNonStandardDirectionTemplateId,
                respondentNonStandardDirectionEmailAddress,
                personalisation,
                reference
            );

        List<IdValue<String>> notificationsSent =
            asylumCase
                .getNotificationsSent()
                .orElseGet(ArrayList::new);

        notificationsSent.add(new IdValue<>(reference, notificationId));

        asylumCase.setNotificationsSent(notificationsSent);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
