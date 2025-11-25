package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum ApplicantType {
    APPELLANT("appellant"),
    RESPONDENT("respondent");

    @JsonValue
    private final String value;

    ApplicantType(String value) {
        this.value = value;
    }

    public static Optional<ApplicantType> from(
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
