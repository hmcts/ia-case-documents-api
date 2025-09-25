package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
