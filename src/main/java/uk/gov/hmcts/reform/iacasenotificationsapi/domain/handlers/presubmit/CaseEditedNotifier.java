package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;

@Component
public class CaseEditedNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String caseEditedCaseOfficerTemplateId;

    private final CaseOfficerCaseEditedNotifier caseOfficerCaseEditedNotifier;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;

    public CaseEditedNotifier(
        @Value("${govnotify.template.caseOfficerCaseEdited}") String caseOfficerCaseEditedTemplateId,
        CaseOfficerCaseEditedNotifier caseOfficerCaseEditedNotifier,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender
    ) {
        requireNonNull(caseOfficerCaseEditedTemplateId, "caseOfficerCaseEditedTemplateId must not be null");

        this.caseEditedCaseOfficerTemplateId = caseOfficerCaseEditedTemplateId;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
        this.caseOfficerCaseEditedNotifier = caseOfficerCaseEditedNotifier;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.EDIT_CASE_LISTING;
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

        final Optional<CaseDetails<AsylumCase>> caseDetailsBefore =
            callback.getCaseDetailsBefore();

        handleCaseOfficer(callback, asylumCase, caseDetailsBefore);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    protected void handleCaseOfficer(
        Callback<AsylumCase> callback,
        AsylumCase asylumCase,
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore

    ) {

        sendGovNotifyEmail(
            callback,
            "CASE_OFFICER",
            caseEditedCaseOfficerTemplateId,
            caseOfficerCaseEditedNotifier.getEmailAddress(asylumCase),
            caseOfficerCaseEditedNotifier.getPersonalisation(asylumCase, caseDetailsBefore),
            asylumCase);
    }

    protected void sendGovNotifyEmail(
        Callback<AsylumCase> callback,
        String referenceSuffix,
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        AsylumCase asylumCase
    ) {

        String reference =
            callback.getCaseDetails().getId()
            + "_CASE_LISTED_" + referenceSuffix;

        String notificationId =
            notificationSender.sendEmail(
                templateId,
                emailAddress,
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
    }
}
