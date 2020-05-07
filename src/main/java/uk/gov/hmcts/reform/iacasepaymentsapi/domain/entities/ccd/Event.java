package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Event {

    START_APPEAL("startAppeal"),
    SUBMIT_APPEAL("submitAppeal"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
