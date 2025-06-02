package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RemissionDecisionTest {

    @Test
    void has_correct_values() {
        Assertions.assertEquals("approved", RemissionDecision.APPROVED.toString());
        Assertions.assertEquals("partiallyApproved", RemissionDecision.PARTIALLY_APPROVED.toString());
        Assertions.assertEquals("rejected", RemissionDecision.REJECTED.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        Assertions.assertEquals(3, RemissionDecision.values().length);
    }
}
