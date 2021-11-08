package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum JourneyType {
    REP("rep"),
    AIP("aip");

    @JsonValue
    private final String value;

    JourneyType(String value) {
        this.value = value;
    }

    public static Optional<JourneyType> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
