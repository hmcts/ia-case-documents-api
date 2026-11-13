package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NationalityFieldValue {
    private String code;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public NationalityFieldValue(@JsonProperty("code") String code) {
        this.code = code;
    }

    private NationalityFieldValue() {
    }

    public String getCode() {
        return code;
    }
}
