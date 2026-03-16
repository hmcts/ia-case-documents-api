package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.bail;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.bail.UploadSignedDecisionOrchestrator;

@Component
public class UploadSignedDecisionNoticeHandler implements PreSubmitCallbackHandler<BailCase> {

    private final UploadSignedDecisionOrchestrator uploadSignedDecisionOrchestrator;

    public UploadSignedDecisionNoticeHandler(
        UploadSignedDecisionOrchestrator uploadSignedDecisionOrchestrator
    ) {
        this.uploadSignedDecisionOrchestrator = uploadSignedDecisionOrchestrator;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && List.of(Event.UPLOAD_SIGNED_DECISION_NOTICE, Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<BailCase> caseDetails = callback.getCaseDetails();

        uploadSignedDecisionOrchestrator.uploadSignedDecision(caseDetails);

        BailCase bailCase = caseDetails.getCaseData();

        return new PreSubmitCallbackResponse<>(bailCase);
    }
}
