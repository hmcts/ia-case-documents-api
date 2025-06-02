package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.oauth2;

public class IdentityManagerResponseException extends RuntimeException {

    public IdentityManagerResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
