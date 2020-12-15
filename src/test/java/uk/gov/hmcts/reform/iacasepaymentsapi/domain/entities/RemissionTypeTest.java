package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

class RemissionTypeTest {

    @Test
    void has_correct_values() {
        assertEquals("noRemission", RemissionType.NO_REMISSION.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, RemissionType.values().length);
    }
}
