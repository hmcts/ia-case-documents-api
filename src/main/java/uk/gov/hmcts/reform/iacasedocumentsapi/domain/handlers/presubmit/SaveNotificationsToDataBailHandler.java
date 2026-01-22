package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SaveNotificationsToDataPdfService;

@Component
public class SaveNotificationsToDataBailHandler implements PreSubmitCallbackHandler<BailCase> {

    private final SaveNotificationsToDataPdfService saveNotificationsToDataPdfService;

    public SaveNotificationsToDataBailHandler(
        SaveNotificationsToDataPdfService saveNotificationsToDataPdfService
    ) {
        this.saveNotificationsToDataPdfService = saveNotificationsToDataPdfService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.SAVE_NOTIFICATIONS_TO_DATA;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<BailCase> caseDetails = callback.getCaseDetails();
        final BailCase bailCase = caseDetails.getCaseData();

        Optional<List<IdValue<StoredNotification>>> existingNotifications =
            bailCase.read(BailCaseFieldDefinition.NOTIFICATIONS);

        List<IdValue<StoredNotification>> newNotifications =
            saveNotificationsToDataPdfService.generatePdfsForNotifications(existingNotifications.orElse(emptyList()));

        bailCase.write(BailCaseFieldDefinition.NOTIFICATIONS, newNotifications);

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
