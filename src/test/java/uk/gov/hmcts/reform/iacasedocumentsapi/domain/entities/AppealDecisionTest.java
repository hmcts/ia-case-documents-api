package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AppealDecisionTest {

    @Test
    public void correct_values_mapped_to_enum() {

        assertEquals(AppealDecision.ALLOWED.getValue(), "allowed");
        assertEquals(AppealDecision.DISMISSED.getValue(), "dismissed");
    }

    @Test
    public void correct_enum_returned_for_valid_values() {

        assertEquals(AppealDecision.ALLOWED, AppealDecision.from("allowed"));
        assertEquals(AppealDecision.DISMISSED, AppealDecision.from("dismissed"));
    }

    @Test
    public void throws_when_trying_to_create_from_invalid_value() {

        assertThatThrownBy(() -> AppealDecision.from("invalid-value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("invalid-value not an AppealDecision");
    }

    @Test
    public void correct_string_values_mapped_to_enum() {
        assertEquals(AppealDecision.ALLOWED.toString(), "allowed");
        assertEquals(AppealDecision.DISMISSED.toString(), "dismissed");
    }
}
