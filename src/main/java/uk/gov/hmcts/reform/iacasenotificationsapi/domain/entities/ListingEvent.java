package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum ListingEvent {
    INITIAL("initialListing"),
    RELISTING("relisting");

    @JsonValue
    private final String value;

    ListingEvent(String value) {
        this.value = value;
    }

    public static Optional<ListingEvent> from(
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

