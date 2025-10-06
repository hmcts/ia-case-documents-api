package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ApplicationDecision {

    GRANTED("Granted"),
    REFUSED("Refused"),
    REFUSED_UNDER_IMA("RefusedUnderIma");

    @JsonValue
    private final String value;

    ApplicationDecision(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
