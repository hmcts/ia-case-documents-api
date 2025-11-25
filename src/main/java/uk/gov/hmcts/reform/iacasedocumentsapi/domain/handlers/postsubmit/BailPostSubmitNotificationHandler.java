package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailNotificationGenerator;


public class BailPostSubmitNotificationHandler implements PostSubmitCallbackHandler<BailCase> {

    private final BiPredicate<PostSubmitCallbackStage, Callback<BailCase>> canHandleFunction;
    private final List<? extends BailNotificationGenerator> bailNotificationGenerators;
    private final Optional<ErrorHandler<BailCase>> errorHandling;

    public BailPostSubmitNotificationHandler(BiPredicate<PostSubmitCallbackStage, Callback<BailCase>> canHandleFunction,
                                             List<? extends BailNotificationGenerator> bailNotificationGenerators
    ) {
        this.canHandleFunction = canHandleFunction;
        this.bailNotificationGenerators = bailNotificationGenerators;
        this.errorHandling = Optional.empty();
    }

    public BailPostSubmitNotificationHandler(BiPredicate<PostSubmitCallbackStage, Callback<BailCase>> canHandleFunction,
                                             List<? extends BailNotificationGenerator> bailNotificationGenerators,
                                             ErrorHandler<BailCase> errorHandling
    ) {
        this.canHandleFunction = canHandleFunction;
        this.bailNotificationGenerators = bailNotificationGenerators;
        this.errorHandling = Optional.ofNullable(errorHandling);
    }

    @Override
    public boolean canHandle(PostSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return canHandleFunction.test(callbackStage, callback);
    }

    @Override
    public PostSubmitCallbackResponse handle(PostSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitCallbackResponse = new PostSubmitCallbackResponse("success", "success");

        try {

            bailNotificationGenerators.forEach(bailNotificationGenerator -> bailNotificationGenerator.generate(callback));

            if (!bailNotificationGenerators.isEmpty()) {

                int lastBailNotificationGeneratorIndex = bailNotificationGenerators.size() - 1;
                Message message = bailNotificationGenerators.get(lastBailNotificationGeneratorIndex).getSuccessMessage();

                if (message.getMessageHeader() != null) {
                    postSubmitCallbackResponse.setConfirmationHeader(message.getMessageHeader());
                }
                if (message.getMessageBody() != null) {

                    BailCase bailCase =
                        callback
                            .getCaseDetails()
                            .getCaseData();

                    postSubmitCallbackResponse.setConfirmationBody(bailCase.toString());
                }
            }
        } catch (Exception e) {
            if (errorHandling.isPresent()) {
                errorHandling.get().accept(callback, e);
            } else {
                throw e;
            }
        }
        return postSubmitCallbackResponse;
    }
}
