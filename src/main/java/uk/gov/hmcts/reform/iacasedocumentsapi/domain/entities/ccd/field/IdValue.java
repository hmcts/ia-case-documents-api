package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import lombok.EqualsAndHashCode;

import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public class IdValue<T> {

    private String id = "";
    private T value;

    private IdValue() {
        // noop -- for deserializer
    }

    public IdValue(
        String id,
        T value
    ) {
        requireNonNull(id);
        requireNonNull(value);

        this.id = id;
        this.value = value;
    }

    public String getId() {
        requireNonNull(id);
        return id;
    }

    public T getValue() {
        requireNonNull(value);
        return value;
    }
}
