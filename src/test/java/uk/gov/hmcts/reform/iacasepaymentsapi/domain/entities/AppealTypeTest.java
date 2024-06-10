package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AppealTypeTest {

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
        assertThat(AppealType.from(input).orElseThrow(() -> new AssertionError("Expected value not found"))).isEqualTo(expected);
    }

    @Test
    void returns_optional_for_unknown_appeal_type() {
        assertThat(AppealType.from("some_unknown_type")).isNotPresent();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(7, AppealType.values().length);
    }
}
