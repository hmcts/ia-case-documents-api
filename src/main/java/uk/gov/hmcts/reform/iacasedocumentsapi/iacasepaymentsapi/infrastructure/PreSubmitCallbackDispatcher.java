package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PreSubmitCallbackHandler<T>> callbackHandlers;

    public PreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<T>> callbackHandlers
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");

        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.callbackHandlers = callbackHandlers;
    }

    public PreSubmitCallbackResponse<T> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        ccdEventAuthorizor.throwIfNotAuthorized(callback.getEvent());

        T caseData =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<T> callbackResponse =
            new PreSubmitCallbackResponse<>(caseData);

        dispatchToHandlers(
            callbackStage,
            callback,
            callbackHandlers
                .stream()
                .filter(callbackHandler -> callbackHandler.getDispatchPriority() == DispatchPriority.EARLY)
                .toList(),
            callbackResponse
        );

        dispatchToHandlers(
            callbackStage,
            callback,
            callbackHandlers
                .stream()
                .filter(callbackHandler -> callbackHandler.getDispatchPriority() == DispatchPriority.LATE)
                .toList(),
            callbackResponse
        );

        return callbackResponse;
    }

    private void dispatchToHandlers(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackHandler<T>> callbackHandlers,
        PreSubmitCallbackResponse<T> callbackResponse
    ) {
        for (PreSubmitCallbackHandler<T> callbackHandler : callbackHandlers) {

            if (callbackHandler.canHandle(callbackStage, callback)) {

                PreSubmitCallbackResponse<T> callbackResponseFromHandler =
                    callbackHandler.handle(callbackStage, callback);

                if (!callbackResponseFromHandler.getErrors().isEmpty()) {
                    callbackResponse.addErrors(callbackResponseFromHandler.getErrors());
                }
            }
        }
    }
}
