package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Arrays.stream;

import java.util.Optional;

public enum ApplicationApplicant {

    ADMIN_OFFICER("Admin Officer"),
    RESPONDENT("Respondent");

    private final String value;

    ApplicationApplicant(String value) {
        this.value = value;
    }

    public static Optional<ApplicationApplicant> from(
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
