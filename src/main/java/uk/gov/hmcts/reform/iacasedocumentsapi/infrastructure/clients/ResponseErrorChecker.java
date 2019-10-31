package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

@Service
public class ResponseErrorChecker<T extends CaseData> {

    public ResponseErrorChecker() {
    }

    public boolean checkForBundleErrors(PreSubmitCallbackResponse<T> response) {

        return response.getErrors() != null && !response.getErrors().isEmpty();

    }

}
