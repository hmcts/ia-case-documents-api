package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import static java.util.Arrays.stream;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;

public enum OutOfCountryDecisionType {

    REFUSAL_OF_HUMAN_RIGHTS("refusalOfHumanRights", "A decision either 1) to refuse a human rights claim made following an application for entry clearance or 2) to refuse a permit to enter the UK under the Immigration (European Economic Area) Regulation 2016"),
    REFUSAL_OF_PROTECTION("refusalOfProtection", "A decision to refuse a protection or human rights claim where your client may only apply after leaving the UK"),
    REMOVAL_OF_CLIENT("removalOfClient", "A decision either 1) to remove your client from the UK under the Immigration (European Economic Area) Regulations 2016, where they are currently outside the UK or 2) to deprive your client of British citizenship, where they are currently outside the UK"),
    REFUSE_PERMIT("refusePermit", "A decision to refuse a permit to enter the UK or entry clearance under the immigration rules and/or the EU Settlement Scheme.");

    @JsonValue
    private String value;

    private String description;

    OutOfCountryDecisionType(String id, String description) {
        this.value = id;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<OutOfCountryDecisionType> from(
        String value
    ) {
        return stream(values())
            .filter(v -> v.getValue().equals(value))
            .findFirst();
    }

    @Override
    public String toString() {
        return value + ": " + description;
    }
}
