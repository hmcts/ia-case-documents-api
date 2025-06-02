package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;
import lombok.Getter;


@Getter
public enum AppealType {

    RP("revocationOfProtection"),
    PA("protection"),
    EA("refusalOfEu"),
    HU("refusalOfHumanRights"),
    DC("deprivation"),
    EU("euSettlementScheme"),
    AG("ageAssessment");

    @JsonValue
    private final String value;

    AppealType(String value) {
        this.value = value;
    }

    public static Optional<AppealType> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    @Override
    public String toString() {
        return value;
    }
}
