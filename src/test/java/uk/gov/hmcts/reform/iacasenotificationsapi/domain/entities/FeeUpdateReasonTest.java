package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FeeUpdateReasonTest {

    @Test
    public void has_correct_values() {
        assertEquals("decisionTypeChanged", FeeUpdateReason.DECISION_TYPE_CHANGED.toString());
        assertEquals("appealNotValid", FeeUpdateReason.APPEAL_NOT_VALID.toString());
        assertEquals("feeRemissionChanged", FeeUpdateReason.FEE_REMISSION_CHANGED.toString());
        assertEquals("appealWithdrawn", FeeUpdateReason.APPEAL_WITHDRAWN.toString());
    }

    @Test
    void has_correct_asylum_contact_preference_description() {
        assertEquals("Decision type changed", FeeUpdateReason.DECISION_TYPE_CHANGED.getNormalizedValue());
        assertEquals("Appeal not valid", FeeUpdateReason.APPEAL_NOT_VALID.getNormalizedValue());
        assertEquals("Fee remission changed", FeeUpdateReason.FEE_REMISSION_CHANGED.getNormalizedValue());
        assertEquals("Appeal withdrawn", FeeUpdateReason.APPEAL_WITHDRAWN.getNormalizedValue());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, FeeUpdateReason.values().length);
    }

}