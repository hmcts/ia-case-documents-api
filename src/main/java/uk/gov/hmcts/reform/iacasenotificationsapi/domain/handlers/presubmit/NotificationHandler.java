package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.function.BiPredicate;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

public class NotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction;
    private final NotificationGenerator notificationGenerator;

    public NotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                               NotificationGenerator notificationGenerator) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerator = notificationGenerator;
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

        notificationGenerator.generate(callback);

        return new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
    }
}
