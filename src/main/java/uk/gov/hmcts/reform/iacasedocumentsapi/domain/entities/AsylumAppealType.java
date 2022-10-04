package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum AsylumAppealType {

    RP("revocationOfProtection"),
    PA("protection"),
    EA("refusalOfEu"),
    HU("refusalOfHumanRights"),
    DC("deprivation"),
    EU("euSettlementScheme");

    @JsonValue
    private String value;

    AsylumAppealType(String value) {
        this.value = value;
    }

    public static Optional<AsylumAppealType> from(String value) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
