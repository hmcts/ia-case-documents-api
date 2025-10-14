package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailNotificationGenerator;

public class BailNotificationHandler implements PreSubmitCallbackHandler<BailCase> {

    private final BiPredicate<PreSubmitCallbackStage, Callback<BailCase>> canHandleFunction;
    private final List<? extends BailNotificationGenerator> bailNotificationGenerators;
    private final Optional<ErrorHandler<BailCase>> errorHandling;

    public BailNotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<BailCase>> canHandleFunction,
                                   List<? extends BailNotificationGenerator> notificationGenerator
    ) {
        this.canHandleFunction = canHandleFunction;
        this.bailNotificationGenerators = notificationGenerator;
        this.errorHandling = Optional.empty();
    }

    public BailNotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<BailCase>> canHandleFunction,
                                   List<? extends BailNotificationGenerator> bailNotificationGenerators,
                                   ErrorHandler<BailCase> errorHandling
    ) {
        this.canHandleFunction = canHandleFunction;
        this.bailNotificationGenerators = bailNotificationGenerators;
        this.errorHandling = Optional.ofNullable(errorHandling);
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return canHandleFunction.test(callbackStage, callback);
    }

    @Override
    public PreSubmitCallbackResponse<BailCase> handle(PreSubmitCallbackStage callbackStage, Callback<BailCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        try {
            bailNotificationGenerators.forEach(bailNotificationGenerator -> bailNotificationGenerator.generate(callback));
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
