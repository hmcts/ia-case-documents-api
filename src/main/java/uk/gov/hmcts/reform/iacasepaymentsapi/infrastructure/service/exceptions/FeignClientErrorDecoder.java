package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
public class FeignClientErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
            case 404: {
                log.error("Error in calling Feign client. Status code "
                              + response.status() + ", methodKey = " + methodKey);
                log.error("Error details: {}", response.body().toString());
                ExceptionUtils.printRootCauseStackTrace(new BadRequestException(response.reason()));
                return new ResponseStatusException(HttpStatus.valueOf(response.status()),
                                                   "Error in calling the client method:" + methodKey);
            }

            default:
                return new Exception(response.reason());
        }
    }
}
