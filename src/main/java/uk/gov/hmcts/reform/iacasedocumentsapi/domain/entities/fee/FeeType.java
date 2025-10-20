package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum FeeType {

    FEE_WITH_HEARING("feeWithHearing"),
    FEE_WITHOUT_HEARING("feeWithoutHearing");

    @JsonValue
    private final String value;

    FeeType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
