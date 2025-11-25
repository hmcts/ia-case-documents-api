package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TimeExtensionDecisionTest {

    @Test
    public void has_correct_values() {
        assertEquals("granted", TimeExtensionDecision.GRANTED.toString());
        assertEquals("refused", TimeExtensionDecision.REFUSED.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, TimeExtensionDecision.values().length);
    }

}
