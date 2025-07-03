package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.stream;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonValue;


public enum SubscriberType {
    APPELLANT("appellant"),
    SUPPORTER("supporter");

    @JsonValue
    private final String value;

    SubscriberType(String value) {
        this.value = value;
    }

    public static Optional<SubscriberType> from(
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
