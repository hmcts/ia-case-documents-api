package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class RemissionDecisionTest {

    @Test
    void has_correct_values() {
        assertEquals("approved", RemissionDecision.APPROVED.toString());
        assertEquals("partiallyApproved", RemissionDecision.PARTIALLY_APPROVED.toString());
        assertEquals("rejected", RemissionDecision.REJECTED.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, RemissionDecision.values().length);
    }
}