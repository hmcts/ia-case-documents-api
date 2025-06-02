package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RemissionTypeTest {

    @Test
    void has_correct_values() {
        Assertions.assertEquals("noRemission", RemissionType.NO_REMISSION.toString());
        Assertions.assertEquals("hoWaiverRemission", RemissionType.HO_WAIVER_REMISSION.toString());
        Assertions.assertEquals("helpWithFees", RemissionType.HELP_WITH_FEES.toString());
        Assertions.assertEquals("exceptionalCircumstancesRemission", RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        Assertions.assertEquals(4, RemissionType.values().length);
    }
}
