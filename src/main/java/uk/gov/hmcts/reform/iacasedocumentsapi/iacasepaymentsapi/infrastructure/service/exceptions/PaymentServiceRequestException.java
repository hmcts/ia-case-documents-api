package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.service.exceptions;

public class PaymentServiceRequestException extends RuntimeException {

    public PaymentServiceRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
