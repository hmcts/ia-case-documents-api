package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients;

import feign.Response;
import feign.codec.ErrorDecoder;
import java.io.IOException;
import java.nio.charset.Charset;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
            case 404:
                try {
                    log.error("StatusCode: {}, methodKey: {}, reason: {}, message: {}",
                        response.status(),
                        methodKey,
                        response.reason(),
                        IOUtils.toString(response.body().asReader(Charset.defaultCharset())));
                } catch (IOException ex) {
                    log.error("Error in reading response body {}", ex.getMessage());
                }
                return new ResponseStatusException(HttpStatus.valueOf(response.status()), response.reason());

            default:
                return new Exception(response.reason());

        }
    }
}
