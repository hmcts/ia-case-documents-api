package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions;

public class PaymentServiceRequestException extends RuntimeException {

    public PaymentServiceRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
