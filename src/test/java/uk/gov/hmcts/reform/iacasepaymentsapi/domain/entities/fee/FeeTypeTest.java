package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FeeTypeTest {

    @Test
    void should_have_correct_values() {
        Assertions.assertEquals("feeWithHearing", FeeType.FEE_WITH_HEARING.toString());
        Assertions.assertEquals("feeWithoutHearing", FeeType.FEE_WITHOUT_HEARING.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        Assertions.assertEquals(2, FeeType.values().length);
    }
}
