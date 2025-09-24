package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ServiceTest {

    @Test
    void should_have_correct_values() {
        Assertions.assertEquals("IAC", Service.IAC.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        Assertions.assertEquals(1, Service.values().length);
    }
}
