package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    PAYMENT_APPEAL("paymentAppeal"),
    SUBMIT_APPEAL("submitAppeal"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
