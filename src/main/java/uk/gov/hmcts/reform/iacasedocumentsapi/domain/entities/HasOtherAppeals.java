package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Optional;

public enum HasOtherAppeals {

    YES("Yes"),
    YES_WITHOUT_APPEAL_NUMBER("YesWithoutAppealNumber"),
    NO("No"),
    NOT_SURE("NotSure");

    @JsonValue
    private String value;

    HasOtherAppeals(String value) {
        this.value = value;
    }

    public static Optional<HasOtherAppeals> from(
            String value
    ) {
        return Arrays.stream(values())
                .filter(v -> v.getValue().equals(value))
                .findFirst();
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
