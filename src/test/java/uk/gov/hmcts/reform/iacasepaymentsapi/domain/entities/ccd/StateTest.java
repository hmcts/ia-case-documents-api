package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class StateTest {

    @Test
    void has_correct_values() {

        assertEquals("appealStarted", State.APPEAL_STARTED.toString());
        assertEquals("appealStartedByAdmin", State.APPEAL_STARTED_BY_ADMIN.toString());
        assertEquals("appealSubmitted", State.APPEAL_SUBMITTED.toString());
        assertEquals("pendingPayment", State.PENDING_PAYMENT.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(5, State.values().length);
    }
}
