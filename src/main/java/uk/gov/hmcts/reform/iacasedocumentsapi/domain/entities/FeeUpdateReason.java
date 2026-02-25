package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FeeUpdateReason {
    DECISION_TYPE_CHANGED("decisionTypeChanged"),
    APPEAL_NOT_VALID("appealNotValid"),
    FEE_REMISSION_CHANGED("feeRemissionChanged"),
    APPEAL_WITHDRAWN("appealWithdrawn");

    @JsonValue
    private String value;

    FeeUpdateReason(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}

