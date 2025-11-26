package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;
import lombok.Getter;

@Getter
public enum JourneyType {
    AIP("aip"),
    REP("rep");

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

    @Override
    public String toString() {
        return getValue();
    }
}
