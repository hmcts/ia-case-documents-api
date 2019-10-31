package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;

public class DocumentBundlingErrorResponseException extends DocumentServiceResponseException {

    private PreSubmitCallbackResponse<BundleCaseData> bundlingServiceResponse;

    public DocumentBundlingErrorResponseException(String message, PreSubmitCallbackResponse<BundleCaseData> bundlingServiceResponse) {
        super(message);
        this.bundlingServiceResponse = bundlingServiceResponse;
    }

    public PreSubmitCallbackResponse<BundleCaseData> getBundlingServiceResponse() {
        return bundlingServiceResponse;
    }
}
