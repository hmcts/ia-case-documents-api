package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

public class IdentityManagerResponseException extends RuntimeException {

    public IdentityManagerResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
