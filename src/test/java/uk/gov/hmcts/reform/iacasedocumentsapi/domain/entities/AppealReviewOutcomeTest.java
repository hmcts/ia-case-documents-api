package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class AppealReviewOutcomeTest {

    @Test
    public void has_correct_values() {
        assertEquals("decisionMaintained", AppealReviewOutcome.DECISION_MAINTAINED.toString());
        assertEquals("decisionWithdrawn", AppealReviewOutcome.DECISION_WITHDRAWN.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, AppealReviewOutcome.values().length);
    }
}
