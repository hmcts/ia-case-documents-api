package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security;

public class IdentityManagerResponseException extends RuntimeException {

    public IdentityManagerResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
