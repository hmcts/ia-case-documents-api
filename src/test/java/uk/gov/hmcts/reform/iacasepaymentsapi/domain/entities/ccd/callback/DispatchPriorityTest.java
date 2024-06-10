package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DispatchPriorityTest {

    @Test
    void has_correct_values() {

        assertEquals("early", DispatchPriority.EARLY.toString());
        assertEquals("late", DispatchPriority.LATE.toString());
        assertEquals("earliest", DispatchPriority.EARLIEST.toString());
        assertEquals("latest", DispatchPriority.LATEST.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(4, DispatchPriority.values().length);
    }
}
