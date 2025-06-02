package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.handlers.presubmit;

import java.util.function.BiConsumer;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.callback.Callback;

public interface ErrorHandler<T extends CaseData> extends BiConsumer<Callback<T>, Throwable> {
}
