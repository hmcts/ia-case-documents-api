package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

public class UnrecoverableException extends RuntimeException {

    public UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }
}
