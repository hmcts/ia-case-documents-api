package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SaveNotificationsToDataPdfService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS;

@Component
public class SaveNotificationsToDataHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final SaveNotificationsToDataPdfService saveNotificationsToDataPdfService;

    public SaveNotificationsToDataHandler(
        SaveNotificationsToDataPdfService saveNotificationsToDataPdfService
    ) {
        this.saveNotificationsToDataPdfService = saveNotificationsToDataPdfService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SAVE_NOTIFICATIONS_TO_DATA;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        final AsylumCase asylumCase = caseDetails.getCaseData();

        Optional<List<IdValue<StoredNotification>>> maybeExistingNotifications =
            asylumCase.read(NOTIFICATIONS);

        ArrayList<IdValue<StoredNotification>> newNotifications = new ArrayList<>();
        List<String> invalidNotificationStatuses = List.of("cancelled", "failed", "technical-failure",
            "temporary-failure", "permanent-failure", "validation-failed", "virus-scan-failed");
        for (IdValue<StoredNotification> notification : maybeExistingNotifications.orElse(emptyList())) {
            StoredNotification storedNotification = notification.getValue();
            if (storedNotification.getNotificationDocument() == null
                && !invalidNotificationStatuses.contains(storedNotification.getNotificationStatus())) {
                String notificationBody = storedNotification.getNotificationBody();
                String notificationReference = storedNotification.getNotificationReference();
                Document notificationPdf =
                    saveNotificationsToDataPdfService.createPdf(notificationBody, notificationReference);
                storedNotification.setNotificationDocument(notificationPdf);
                notification = new IdValue<>(notification.getId(), storedNotification);
            }
            newNotifications.add(notification);
        }
        asylumCase.write(NOTIFICATIONS, newNotifications);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
