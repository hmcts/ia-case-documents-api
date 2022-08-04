package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;


public class PostSubmitNotificationHandler implements PostSubmitCallbackHandler<AsylumCase> {

    private final BiPredicate<PostSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction;
    private final List<? extends NotificationGenerator> notificationGenerators;
    private final Optional<ErrorHandler<AsylumCase>> errorHandling;

    public PostSubmitNotificationHandler(BiPredicate<PostSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                                         List<? extends NotificationGenerator> notificationGenerator
    ) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerators = notificationGenerator;
        this.errorHandling = Optional.empty();
    }

    public PostSubmitNotificationHandler(BiPredicate<PostSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                                         List<? extends NotificationGenerator> notificationGenerator,
                                         ErrorHandler<AsylumCase> errorHandling
    ) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerators = notificationGenerator;
        this.errorHandling = Optional.ofNullable(errorHandling);
    }

    @Override
    public boolean canHandle(PostSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return canHandleFunction.test(callbackStage, callback);
    }

    @Override
    public PostSubmitCallbackResponse handle(PostSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitCallbackResponse = new PostSubmitCallbackResponse("success", "success");

        try {
            notificationGenerators.forEach(notificationGenerator -> notificationGenerator.generate(callback));

            if (!notificationGenerators.isEmpty()) {

                int lastNotificationGeneratorIndex = notificationGenerators.size() - 1;
                Message message = notificationGenerators.get(lastNotificationGeneratorIndex).getSuccessMessage();

                if (message.getMessageHeader() != null) {
                    postSubmitCallbackResponse.setConfirmationHeader(message.getMessageHeader());
                }
                if (message.getMessageBody() != null) {

                    AsylumCase asylumCase =
                        callback
                            .getCaseDetails()
                            .getCaseData();

                    postSubmitCallbackResponse.setConfirmationBody(asylumCase.toString());
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
