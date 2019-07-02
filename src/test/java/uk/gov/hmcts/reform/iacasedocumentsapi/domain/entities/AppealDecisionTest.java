package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

public class AppealDecisionTest {

    @Test
    public void correct_values_mapped_to_enum() {

        assertThat(AppealDecision.ALLOWED.getValue()).isEqualTo("allowed");
        assertThat(AppealDecision.DISMISSED.getValue()).isEqualTo("dismissed");
    }

    @Test
    public void throws_when_trying_to_create_from_invalid_value() {

        assertThatThrownBy(() -> AppealDecision.from("invalid-value"))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("invalid-value not an AppealDecision");
    }
}