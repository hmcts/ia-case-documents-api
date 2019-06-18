package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@Component
public class AppealSubmittedCaseOfficerNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String appealSubmittedCaseOfficerTemplateId;
    private final CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    private final Map<HearingCentre, String> hearingCentreEmailAddresses;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;

    public AppealSubmittedCaseOfficerNotifier(
        @Value("${govnotify.template.appealSubmittedCaseOfficer}") String appealSubmittedCaseOfficerTemplateId,
        CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory,
        Map<HearingCentre, String> hearingCentreEmailAddresses,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        requireNonNull(appealSubmittedCaseOfficerTemplateId, "appealSubmittedCaseOfficerTemplateId must not be null");

        this.appealSubmittedCaseOfficerTemplateId = appealSubmittedCaseOfficerTemplateId;
        this.caseOfficerPersonalisationFactory = caseOfficerPersonalisationFactory;
        this.hearingCentreEmailAddresses = hearingCentreEmailAddresses;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final HearingCentre hearingCentre =
            asylumCase
                .read(HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("hearingCentre is not present"));

        final String hearingCentreEmailAddress =
            hearingCentreEmailAddresses
                .get(hearingCentre);

        if (hearingCentreEmailAddress == null) {
            throw new IllegalStateException("Hearing centre email address not found: " + hearingCentre.toString());
        }

        Map<String, String> personalisation =
            caseOfficerPersonalisationFactory
                .create(asylumCase);

        String reference =
            callback.getCaseDetails().getId()
            + "_APPEAL_SUBMITTED_CASE_OFFICER";

        String notificationId =
            notificationSender.sendEmail(
                appealSubmittedCaseOfficerTemplateId,
                hearingCentreEmailAddress,
                personalisation,
                reference
            );

        Optional<List<IdValue<String>>> maybeNotificationSent = asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent = maybeNotificationSent
                .orElseGet(ArrayList::new);

        asylumCase.write(NOTIFICATIONS_SENT,
            notificationIdAppender.append(
                notificationsSent,
                reference,
                notificationId
            )
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
