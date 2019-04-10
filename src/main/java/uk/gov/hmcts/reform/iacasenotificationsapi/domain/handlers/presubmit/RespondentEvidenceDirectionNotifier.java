package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.RespondentDirectionPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;

@Component
public class RespondentEvidenceDirectionNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String respondentEvidenceDirectionTemplateId;
    private final String respondentEvidenceDirectionEmailAddress;
    private final RespondentDirectionPersonalisationFactory respondentDirectionPersonalisationFactory;
    private final DirectionFinder directionFinder;
    private final NotificationSender notificationSender;

    public RespondentEvidenceDirectionNotifier(
        @Value("${govnotify.template.respondentEvidenceDirection}") String respondentEvidenceDirectionTemplateId,
        @Value("${respondentEmailAddresses.respondentEvidenceDirection}") String respondentEvidenceDirectionEmailAddress,
        RespondentDirectionPersonalisationFactory respondentDirectionPersonalisationFactory,
        DirectionFinder directionFinder,
        NotificationSender notificationSender
    ) {
        requireNonNull(respondentEvidenceDirectionTemplateId, "respondentEvidenceDirectionTemplateId must not be null");
        requireNonNull(respondentEvidenceDirectionEmailAddress, "respondentEvidenceDirectionEmailAddress must not be null");

        this.respondentEvidenceDirectionTemplateId = respondentEvidenceDirectionTemplateId;
        this.respondentEvidenceDirectionEmailAddress = respondentEvidenceDirectionEmailAddress;
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE;
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

        Direction respondentEvidenceDirection =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        Map<String, String> personalisation =
            respondentDirectionPersonalisationFactory
                .create(asylumCase, respondentEvidenceDirection);

        String reference =
            callback.getCaseDetails().getId()
            + "_RESPONDENT_EVIDENCE_DIRECTION";

        String notificationId =
            notificationSender.sendEmail(
                respondentEvidenceDirectionTemplateId,
                respondentEvidenceDirectionEmailAddress,
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
