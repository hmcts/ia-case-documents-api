package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import static junit.framework.TestCase.assertEquals;

import org.junit.jupiter.api.Test;

class PaymentStatusTest {

    @Test
    void has_correct_values() {
        assertEquals("Paid", PaymentStatus.PAID.toString());
        assertEquals("Payment pending", PaymentStatus.PAYMENT_PENDING.toString());
        assertEquals("Failed", PaymentStatus.FAILED.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, PaymentStatus.values().length);
    }
}
