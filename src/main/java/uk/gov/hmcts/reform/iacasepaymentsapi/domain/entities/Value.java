package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class Value {

    private String code;
    private String label;

    private Value() {
        //no op constructor
    }

    public Value(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
