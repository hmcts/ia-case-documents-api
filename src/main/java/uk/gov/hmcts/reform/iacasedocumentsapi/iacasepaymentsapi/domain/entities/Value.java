package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
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

}
