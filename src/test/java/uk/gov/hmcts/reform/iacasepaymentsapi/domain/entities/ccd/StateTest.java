package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    public void has_correct_values() {

        assertEquals("appealStarted", State.APPEAL_STARTED.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(2, State.values().length);
    }
}
