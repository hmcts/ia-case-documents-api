package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum ContactPreference {

    WANTS_EMAIL("wantsEmail"),
    WANTS_SMS("wantsSms");

    @JsonValue
    private String value;

    ContactPreference(String value) {
        this.value = value;
    }

    public static Optional<ContactPreference> from(
        String value
    ) {
        return Arrays.stream(values())
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
