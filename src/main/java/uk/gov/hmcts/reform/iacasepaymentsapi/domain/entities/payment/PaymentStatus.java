package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import com.fasterxml.jackson.annotation.JsonValue;

public enum PaymentStatus {

    PAID("Paid"),
    PAYMENT_PENDING("Payment pending"),
    FAILED("Failed"),
    TIMEOUT("Timeout"),
    NOT_PAID("Not paid");

    @JsonValue
    private final String id;

    PaymentStatus(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
