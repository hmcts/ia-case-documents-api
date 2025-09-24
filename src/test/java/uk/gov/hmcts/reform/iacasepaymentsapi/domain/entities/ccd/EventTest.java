package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void has_correct_values() {

        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("paymentAppeal", Event.PAYMENT_APPEAL.toString());
        assertEquals("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString());
        assertEquals("payForAppeal", Event.PAY_FOR_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("recordRemissionDecision", Event.RECORD_REMISSION_DECISION.toString());
        assertEquals("updatePaymentStatus", Event.UPDATE_PAYMENT_STATUS.toString());
        assertEquals("generateServiceRequest", Event.GENERATE_SERVICE_REQUEST.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(10, Event.values().length);
    }
}
