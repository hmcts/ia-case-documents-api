package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

public class NotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction;
    private final List<? extends NotificationGenerator> notificationGenerators;
    private final Optional<ErrorHandler> errorHandling;

    public NotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                               List<? extends NotificationGenerator> notificationGenerator
    ) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerators = notificationGenerator;
        this.errorHandling = Optional.empty();
    }

    public NotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                               List<? extends NotificationGenerator> notificationGenerator,
                               ErrorHandler errorHandling
    ) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerators = notificationGenerator;
        this.errorHandling = Optional.ofNullable(errorHandling);
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return canHandleFunction.test(callbackStage, callback);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        try {
            notificationGenerators.forEach(notificationGenerator -> notificationGenerator.generate(callback));
        } catch (Exception e) {
            if (errorHandling.isPresent()) {
                errorHandling.get().accept(callback, e);
            } else {
                throw e;
            }
        }

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
    }
}
