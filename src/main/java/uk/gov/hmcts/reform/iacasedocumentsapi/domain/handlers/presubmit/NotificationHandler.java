package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.NotificationGenerator;

public class NotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction;
    private final List<? extends NotificationGenerator> notificationGenerators;
    private final Optional<ErrorHandler<AsylumCase>> errorHandling;

    public NotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                               List<? extends NotificationGenerator> notificationGenerator
    ) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerators = notificationGenerator;
        this.errorHandling = Optional.empty();
    }

    public NotificationHandler(BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> canHandleFunction,
                               List<? extends NotificationGenerator> notificationGenerator,
                               ErrorHandler<AsylumCase> errorHandling
    ) {
        this.canHandleFunction = canHandleFunction;
        this.notificationGenerators = notificationGenerator;
        this.errorHandling = Optional.ofNullable(errorHandling);
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        if (getEventsToSkip().contains(callback.getEvent())) {
            return false;
        }
        return canHandleFunction.test(callbackStage, callback);
    }

    public static List<Event> getEventsToSkip() {
        return List.of(
            Event.SUBMIT_APPLICATION,
            Event.RECORD_THE_DECISION,
            Event.END_APPLICATION,
            Event.MAKE_NEW_APPLICATION,
            Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
            Event.UPLOAD_SIGNED_DECISION_NOTICE,
            Event.CASE_LISTING,
            Event.UPLOAD_BAIL_SUMMARY,
            Event.UPLOAD_DOCUMENTS,
            Event.SEND_BAIL_DIRECTION,
            Event.EDIT_BAIL_DOCUMENTS,
            Event.CHANGE_BAIL_DIRECTION_DUE_DATE,
            Event.STOP_LEGAL_REPRESENTING,
            Event.NOC_REQUEST_BAIL,
            Event.CREATE_BAIL_CASE_LINK,
            Event.MAINTAIN_BAIL_CASE_LINKS,
            Event.SEND_UPLOAD_BAIL_SUMMARY_DIRECTION,
            Event.FORCE_CASE_TO_HEARING,
            Event.CHANGE_TRIBUNAL_CENTRE,
            Event.START_APPLICATION,
            Event.EDIT_BAIL_APPLICATION
        );
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        try {
            notificationGenerators.forEach(
                notificationGenerator -> notificationGenerator.generate(callback));
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
