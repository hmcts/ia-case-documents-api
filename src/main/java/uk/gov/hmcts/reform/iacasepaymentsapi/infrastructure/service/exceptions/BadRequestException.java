package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
