package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CurrencyTest {

    @Test
    void should_have_correct_values() {
        Assertions.assertEquals("GBP", Currency.GBP.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        Assertions.assertEquals(1, Currency.values().length);
    }
}
