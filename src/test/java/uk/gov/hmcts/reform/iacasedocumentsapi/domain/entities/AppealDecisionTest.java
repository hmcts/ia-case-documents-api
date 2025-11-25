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


    @Test
    public void has_correct_values() {
        assertEquals(AppealDecision.DISMISSED.toString(), AppealDecision.DISMISSED.getValue());
        assertEquals(AppealDecision.ALLOWED.toString(), AppealDecision.ALLOWED.getValue());

        assertEquals("dismissed", AppealDecision.DISMISSED.toString());
        assertEquals("allowed", AppealDecision.ALLOWED.toString());

        assertEquals(AppealDecision.DISMISSED, AppealDecision.from("dismissed"));
        assertEquals(AppealDecision.ALLOWED, AppealDecision.from("allowed"));
    }

    @Test
    public void should_throw_exception_when_name_unrecognised() {
        assertThatThrownBy(() -> AppealDecision.from("unknown"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("unknown not an AppealDecision")
            .hasNoCause();
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, AppealDecision.values().length);
    }

}
