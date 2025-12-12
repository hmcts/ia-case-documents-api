package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;

public interface PreSubmitPaymentsCallbackHandler<T extends CaseData> extends PreSubmitCallbackHandler<T> {
    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.PAYMENTS;
    }
}
