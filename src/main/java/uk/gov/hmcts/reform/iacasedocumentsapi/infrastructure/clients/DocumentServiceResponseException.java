package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import org.springframework.web.client.RestClientResponseException;

public class DocumentServiceResponseException extends RuntimeException {

    public DocumentServiceResponseException(String message) {
        super(message);
    }

    public DocumentServiceResponseException(String message, RestClientResponseException cause) {
        super(message, cause);

    }
}
