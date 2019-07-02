package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_AND_REASONS_AVAILABLE;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.SendDecisionAndReasonsOrchestrator;

@Component
public class SendDecisionAndReasonsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final SendDecisionAndReasonsOrchestrator sendDecisionAndReasonsOrchestrator;

    public SendDecisionAndReasonsHandler(
        SendDecisionAndReasonsOrchestrator sendDecisionAndReasonsOrchestrator
    ) {
        this.sendDecisionAndReasonsOrchestrator = sendDecisionAndReasonsOrchestrator;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SEND_DECISION_AND_REASONS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();

        sendDecisionAndReasonsOrchestrator.sendDecisionAndReasons(caseDetails);

        AsylumCase asylumCase = caseDetails.getCaseData();

        asylumCase.clear(DECISION_AND_REASONS_AVAILABLE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
