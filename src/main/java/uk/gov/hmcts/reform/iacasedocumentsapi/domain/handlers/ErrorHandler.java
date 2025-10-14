package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers;

import java.util.function.BiConsumer;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

public interface ErrorHandler<T> extends BiConsumer<Callback<T>, Throwable> {
}
