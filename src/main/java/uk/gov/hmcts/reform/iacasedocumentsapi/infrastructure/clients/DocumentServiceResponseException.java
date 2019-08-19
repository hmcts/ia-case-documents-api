package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

public class DocumentServiceResponseException extends RuntimeException {

    public DocumentServiceResponseException(String message) {
        super(message);
    }

    public DocumentServiceResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
