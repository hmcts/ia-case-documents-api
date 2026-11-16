package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonCreator;

import static java.util.Objects.requireNonNull;

public class IdValue<T> {

    private String id = "";
    private T value;

    @JsonCreator
    private IdValue() {
        // noop -- for deserializer
    }

    public IdValue(
        String id,
        T value
    ) {
        requireNonNull(value);

        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public T getValue() {
        requireNonNull(value);
        return value;
    }
}
