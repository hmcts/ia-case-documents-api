package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.payments;

import java.util.function.BiConsumer;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;

public interface ErrorHandler<T extends CaseData> extends BiConsumer<Callback<T>, Throwable> {
}
