package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
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
