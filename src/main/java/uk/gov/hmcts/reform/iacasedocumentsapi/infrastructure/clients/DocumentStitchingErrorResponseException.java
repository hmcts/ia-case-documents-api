package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;

public class DocumentStitchingErrorResponseException extends DocumentServiceResponseException {

    private PreSubmitCallbackResponse<BundleCaseData> stitchingServiceResponse;

    public DocumentStitchingErrorResponseException(String message, PreSubmitCallbackResponse<BundleCaseData> stitchingServiceResponse) {
        super(message);
        this.stitchingServiceResponse = stitchingServiceResponse;
    }

    public PreSubmitCallbackResponse<BundleCaseData> getStitchingServiceResponse() {
        return stitchingServiceResponse;
    }
}
