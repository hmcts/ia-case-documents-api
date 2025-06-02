package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PaymentStatusTest {

    @Test
    void has_correct_values() {
        Assertions.assertEquals("Paid", PaymentStatus.PAID.toString());
        Assertions.assertEquals("Payment pending", PaymentStatus.PAYMENT_PENDING.toString());
        Assertions.assertEquals("Failed", PaymentStatus.FAILED.toString());
        Assertions.assertEquals("Timeout", PaymentStatus.TIMEOUT.toString());
        Assertions.assertEquals("Not paid", PaymentStatus.NOT_PAID.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        Assertions.assertEquals(5, PaymentStatus.values().length);
    }
}
