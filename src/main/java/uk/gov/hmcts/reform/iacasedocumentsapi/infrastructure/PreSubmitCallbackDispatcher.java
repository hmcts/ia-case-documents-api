package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

public class PreSubmitCallbackDispatcher<T extends CaseData> {

    private static final org.slf4j.Logger LOG = getLogger(PreSubmitCallbackDispatcher.class);

    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PreSubmitCallbackHandler<T>> sortedCallbackHandlers;

    public PreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<T>> callbackHandlers
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .collect(Collectors.toList());
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

        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.EARLIEST);
        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.EARLY);
        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.LATE);
        dispatchToHandlers(callbackStage, callback, sortedCallbackHandlers, callbackResponse, DispatchPriority.LATEST);

        return callbackResponse;
    }

    private void dispatchToHandlers(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback,
        List<PreSubmitCallbackHandler<T>> callbackHandlers,
        PreSubmitCallbackResponse<T> callbackResponse,
        DispatchPriority dispatchPriority
    ) {
        for (PreSubmitCallbackHandler<T> callbackHandler : callbackHandlers) {

            if (callbackHandler.getDispatchPriority() == dispatchPriority) {

                Callback<T> callbackForHandler = new Callback<>(
                    new CaseDetails<>(
                        callback.getCaseDetails().getId(),
                        callback.getCaseDetails().getJurisdiction(),
                        callback.getCaseDetails().getState(),
                        callbackResponse.getData(),
                        callback.getCaseDetails().getCreatedDate()
                    ),
                    callback.getCaseDetailsBefore(),
                    callback.getEvent()
                );

                LOG.info("Checking if can handler `{}`", callbackHandler.getClass().getSimpleName());

                if (callbackHandler.canHandle(callbackStage, callbackForHandler)) {
                    LOG.info("Handler `{}` is applicable", callbackHandler.getClass().getSimpleName());

                    long startTime = System.currentTimeMillis();
                    PreSubmitCallbackResponse<T> callbackResponseFromHandler =
                        callbackHandler.handle(callbackStage, callbackForHandler);
                    long executionTime = System.currentTimeMillis() - startTime;
                    LOG.info("Handler `{}` executed in {} ms", callbackHandler.getClass().getSimpleName(), executionTime);

                    callbackResponse.setData(callbackResponseFromHandler.getData());

                    if (!callbackResponseFromHandler.getErrors().isEmpty()) {
                        callbackResponse.addErrors(callbackResponseFromHandler.getErrors());
                    }
                }
            }
        }
    }
}
