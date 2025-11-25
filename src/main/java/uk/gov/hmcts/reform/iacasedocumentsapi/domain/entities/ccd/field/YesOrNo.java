package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum YesOrNo {

    NO("No"),
    YES("Yes");

    @JsonValue
    private final String id;

    YesOrNo(String id) {
        this.id = id;
    }

    @JsonCreator
    public static Optional<YesOrNo> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getId().equalsIgnoreCase(value))
            .findFirst();
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }
}
