package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AppealTypeTest {
    @ParameterizedTest
    @CsvSource({
        "revocationOfProtection, RP",
        "protection, PA",
        "refusalOfEu, EA",
        "refusalOfHumanRights, HU",
        "deprivation, DC",
        "euSettlementScheme, EU",
        "ageAssessment, AG"
    })
    void has_correct_asylum_appeal_types(String input, AppealType expected) {
        assertEquals(expected, AppealType.from(input).orElse(null));
    }

    @Test
    public void returns_optional_for_unknown_appeal_type() {
        assertEquals(AppealType.from("some_unknown_type"), Optional.empty());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, AppealType.values().length);
    }
}
