package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.service.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
