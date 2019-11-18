package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers.advice;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

@Slf4j
@Service
public class ErrorResponseLogger {

    public ErrorResponseLogger() {
        //no args
    }

    public void maybeLogException(Throwable ex) {

        if (ex instanceof RestClientResponseException) {

            RestClientResponseException cause = (RestClientResponseException) ex;

            log.error("Error returned with status: {} \nwith response body: {}",
                cause.getRawStatusCode(),
                cause.getResponseBodyAsString());
        }

    }

    public <T extends CaseData> void maybeLogErrorsListResponse(PreSubmitCallbackResponse<T> responseData) {

        Optional.ofNullable(responseData)
            .ifPresent(response -> response
                .getErrors()
                .forEach(log::error));

    }

}
