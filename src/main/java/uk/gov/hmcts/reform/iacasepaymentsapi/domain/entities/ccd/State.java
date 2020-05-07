package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum State {

    APPEAL_STARTED("appealStarted"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String id;

    State(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
