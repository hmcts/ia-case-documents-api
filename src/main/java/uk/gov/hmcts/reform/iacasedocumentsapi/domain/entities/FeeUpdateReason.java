package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

public enum FeeUpdateReason {
    DECISION_TYPE_CHANGED("decisionTypeChanged", "Decision type changed"),
    APPEAL_NOT_VALID("appealNotValid", "Appeal not valid"),
    FEE_REMISSION_CHANGED("feeRemissionChanged", "Fee remission changed"),
    APPEAL_WITHDRAWN("appealWithdrawn", "Appeal withdrawn");

    @JsonValue
    private final String value;
    @Getter
    private final String normalizedValue;

    FeeUpdateReason(String value, String normalizedValue) {
        this.value = value;
        this.normalizedValue = normalizedValue;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}

