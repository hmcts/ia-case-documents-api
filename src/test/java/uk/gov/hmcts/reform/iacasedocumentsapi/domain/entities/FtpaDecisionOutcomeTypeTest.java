package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FtpaDecisionOutcomeTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("granted", FtpaDecisionOutcomeType.FTPA_GRANTED.toString());
        assertEquals("partiallyGranted", FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString());
        assertEquals("refused", FtpaDecisionOutcomeType.FTPA_REFUSED.toString());
        assertEquals("notAdmitted", FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.toString());
        assertEquals("reheardRule35", FtpaDecisionOutcomeType.FTPA_REHEARD35.toString());
        assertEquals("reheardRule32", FtpaDecisionOutcomeType.FTPA_REHEARD32.toString());
        assertEquals("remadeRule32", FtpaDecisionOutcomeType.FTPA_REMADE32.toString());
        assertEquals("remadeRule31", FtpaDecisionOutcomeType.FTPA_REMADE31.toString());
    }

    @Test
    public void should_have_all_expected_enum_values() {
        assertThat(FtpaDecisionOutcomeType.values()).containsExactlyInAnyOrder(
            FtpaDecisionOutcomeType.FTPA_GRANTED,
            FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED,
            FtpaDecisionOutcomeType.FTPA_REFUSED,
            FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED,
            FtpaDecisionOutcomeType.FTPA_REHEARD35,
            FtpaDecisionOutcomeType.FTPA_REHEARD32,
            FtpaDecisionOutcomeType.FTPA_REMADE32,
            FtpaDecisionOutcomeType.FTPA_REMADE31
        );
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, FtpaDecisionOutcomeType.values().length);
    }
}