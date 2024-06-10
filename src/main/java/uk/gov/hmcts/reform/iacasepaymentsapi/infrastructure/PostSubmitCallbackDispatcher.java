package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class PostSubmitCallbackDispatcher<T extends CaseData> {
    private final CcdEventAuthorizor ccdEventAuthorizor;
    private final List<PostSubmitCallbackHandler<T>> sortedCallbackHandlers;

    public PostSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PostSubmitCallbackHandler<T>> callbackHandlers
    ) {
        requireNonNull(ccdEventAuthorizor, "ccdEventAuthorizor must not be null");
        requireNonNull(callbackHandlers, "callbackHandlers must not be null");
        this.ccdEventAuthorizor = ccdEventAuthorizor;
        this.sortedCallbackHandlers = callbackHandlers.stream()
            // sorting handlers by handler class name
            .sorted(Comparator.comparing(h -> h.getClass().getSimpleName()))
            .toList();
    }

    public PostSubmitCallbackResponse handle(PostSubmitCallbackStage callbackStage,
                                             Callback<T> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        ccdEventAuthorizor.throwIfNotAuthorized(callback.getEvent());

        PostSubmitCallbackResponse callbackResponse =
            new PostSubmitCallbackResponse();

        for (PostSubmitCallbackHandler<T> callbackHandler : sortedCallbackHandlers) {

            if (callbackHandler.canHandle(callbackStage, callback)) {

                PostSubmitCallbackResponse callbackResponseFromHandler =
                    callbackHandler.handle(callbackStage, callback);

                callbackResponseFromHandler
                    .getConfirmationHeader()
                    .ifPresent(callbackResponse::setConfirmationHeader);

                callbackResponseFromHandler
                    .getConfirmationBody()
                    .ifPresent(callbackResponse::setConfirmationBody);
            }
        }

        return callbackResponse;
    }
}
