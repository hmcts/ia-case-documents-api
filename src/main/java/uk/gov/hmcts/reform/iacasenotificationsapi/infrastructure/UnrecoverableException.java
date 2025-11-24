package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

public class UnrecoverableException extends RuntimeException {

    public UnrecoverableException(String message, Throwable cause) {
        super(message, cause);
    }
}
