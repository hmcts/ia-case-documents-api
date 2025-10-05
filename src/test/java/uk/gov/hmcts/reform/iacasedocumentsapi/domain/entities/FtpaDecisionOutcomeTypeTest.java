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
    public void should_have_correct_enum_constants() {
        assertEquals("FTPA_GRANTED", FtpaDecisionOutcomeType.FTPA_GRANTED.name());
        assertEquals("FTPA_PARTIALLY_GRANTED", FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.name());
        assertEquals("FTPA_REFUSED", FtpaDecisionOutcomeType.FTPA_REFUSED.name());
        assertEquals("FTPA_NOT_ADMITTED", FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.name());
        assertEquals("FTPA_REHEARD35", FtpaDecisionOutcomeType.FTPA_REHEARD35.name());
        assertEquals("FTPA_REHEARD32", FtpaDecisionOutcomeType.FTPA_REHEARD32.name());
        assertEquals("FTPA_REMADE32", FtpaDecisionOutcomeType.FTPA_REMADE32.name());
        assertEquals("FTPA_REMADE31", FtpaDecisionOutcomeType.FTPA_REMADE31.name());
    }

    @Test
    public void should_be_usable_in_switch_statements() {
        FtpaDecisionOutcomeType outcome = FtpaDecisionOutcomeType.FTPA_GRANTED;
        String result;

        switch (outcome) {
            case FTPA_GRANTED:
                result = "granted";
                break;
            case FTPA_PARTIALLY_GRANTED:
                result = "partially granted";
                break;
            case FTPA_REFUSED:
                result = "refused";
                break;
            case FTPA_NOT_ADMITTED:
                result = "not admitted";
                break;
            case FTPA_REHEARD35:
                result = "reheard rule 35";
                break;
            case FTPA_REHEARD32:
                result = "reheard rule 32";
                break;
            case FTPA_REMADE32:
                result = "remade rule 32";
                break;
            case FTPA_REMADE31:
                result = "remade rule 31";
                break;
            default:
                result = "unknown";
        }

        assertEquals("granted", result);
    }

    @Test
    public void should_maintain_consistent_valueOf_behavior() {
        assertEquals(FtpaDecisionOutcomeType.FTPA_GRANTED,
                    FtpaDecisionOutcomeType.valueOf("FTPA_GRANTED"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED,
                    FtpaDecisionOutcomeType.valueOf("FTPA_PARTIALLY_GRANTED"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_REFUSED,
                    FtpaDecisionOutcomeType.valueOf("FTPA_REFUSED"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED,
                    FtpaDecisionOutcomeType.valueOf("FTPA_NOT_ADMITTED"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_REHEARD35,
                    FtpaDecisionOutcomeType.valueOf("FTPA_REHEARD35"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_REHEARD32,
                    FtpaDecisionOutcomeType.valueOf("FTPA_REHEARD32"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_REMADE32,
                    FtpaDecisionOutcomeType.valueOf("FTPA_REMADE32"));
        assertEquals(FtpaDecisionOutcomeType.FTPA_REMADE31,
                    FtpaDecisionOutcomeType.valueOf("FTPA_REMADE31"));
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, FtpaDecisionOutcomeType.values().length);
    }
}