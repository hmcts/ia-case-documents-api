package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MaleOrFemale {

    MALE("All male"),
    FEMALE("All female"),
    NONE("");

    @JsonValue
    private final String id;

    MaleOrFemale(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
