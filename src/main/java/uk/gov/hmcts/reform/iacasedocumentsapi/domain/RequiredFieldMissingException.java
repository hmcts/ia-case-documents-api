package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
