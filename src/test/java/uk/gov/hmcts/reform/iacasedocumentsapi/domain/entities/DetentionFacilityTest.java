package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DetentionFacilityTest {

    @Test
    public void has_correct_values() {
        assertEquals("immigrationRemovalCentre", DetentionFacility.IRC.toString());
        assertEquals("prison", DetentionFacility.PRISON.toString());
        assertEquals("other", DetentionFacility.OTHER.toString());
    }

    @Test
    public void correct_values_mapped_to_enum() {
        assertEquals(DetentionFacility.IRC.getValue(), "immigrationRemovalCentre");
        assertEquals(DetentionFacility.PRISON.getValue(), "prison");
        assertEquals(DetentionFacility.OTHER.getValue(), "other");
    }

    @Test
    public void correct_enum_returned_for_valid_values() {
        assertEquals(DetentionFacility.IRC, DetentionFacility.from("immigrationRemovalCentre"));
        assertEquals(DetentionFacility.PRISON, DetentionFacility.from("prison"));
        assertEquals(DetentionFacility.OTHER, DetentionFacility.from("other"));
    }

    @Test
    public void throws_when_trying_to_create_from_invalid_value() {
        assertThatThrownBy(() -> DetentionFacility.from("invalid-value"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("invalid-value not a Detention Facility");
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, DetentionFacility.values().length);
    }
}
