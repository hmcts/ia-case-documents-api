package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;

import java.util.function.BiConsumer;

public interface ErrorHandler<T extends CaseData> extends BiConsumer<Callback<T>, Throwable> {
}
