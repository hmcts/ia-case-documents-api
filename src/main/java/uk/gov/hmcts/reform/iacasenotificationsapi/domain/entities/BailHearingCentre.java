package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum BailHearingCentre {

    BIRMINGHAM("birmingham"),
    BRADFORD("bradford"),
    GLASGOW("glasgow"),
    HATTON_CROSS("hattonCross"),
    MANCHESTER("manchester"),
    NEWPORT("newport"),
    TAYLOR_HOUSE("taylorHouse"),
    YARLS_WOOD("yarlsWood");

    @JsonValue
    private final String value;

    BailHearingCentre(String value) {
        this.value = value;
    }

    public static Optional<BailHearingCentre> from(
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
