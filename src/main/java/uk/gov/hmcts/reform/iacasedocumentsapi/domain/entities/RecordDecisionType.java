package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecordDecisionType {

    GRANTED("granted"),
    REFUSED("refused"),
    CONDITIONAL_GRANT("conditionalGrant"),
    REFUSED_UNDER_IMA("refusedUnderIma");

    @JsonValue
    private final String value;

    RecordDecisionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
