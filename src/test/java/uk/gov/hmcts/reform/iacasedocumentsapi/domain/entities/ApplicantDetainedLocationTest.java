package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantDetainedLocation.IMIGRATION_REMOVAL_CENTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantDetainedLocation.PRISON;

import org.junit.jupiter.api.Test;

class ApplicantDetainedLocationTest {

    @Test
    void has_correct_locations() {
        assertEquals("Immigration removal centre", IMIGRATION_REMOVAL_CENTER.toString());
        assertEquals("Prison", PRISON.toString());
    }

    @Test
    void has_correct_codes() {
        assertEquals("immigrationRemovalCentre", IMIGRATION_REMOVAL_CENTER.getCode());
        assertEquals("prison", PRISON.getCode());
    }

    @Test
    void can_be_created_from() {
        assertEquals(IMIGRATION_REMOVAL_CENTER, ApplicantDetainedLocation.from("immigrationRemovalCentre").get());
        assertEquals(PRISON, ApplicantDetainedLocation.from("prison").get());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, ApplicantDetainedLocation.values().length);
    }
}
