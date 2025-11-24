package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum SourceOfRemittal {

    UPPER_TRIBUNAL("Upper Tribunal"),
    COURT_OF_APPEAL("Court of Appeal");

    @JsonValue
    private String value;

    SourceOfRemittal(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return getValue();
    }
}
