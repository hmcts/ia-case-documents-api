package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType;

public class JourneyTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("rep", JourneyType.REP.toString());
        assertEquals("aip", JourneyType.AIP.toString());
    }

    @Test
    public void has_correct_journey_types() {
        assertThat(JourneyType.from("rep").get()).isEqualByComparingTo(JourneyType.REP);
        assertThat(JourneyType.from("aip").get()).isEqualByComparingTo(JourneyType.AIP);
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, JourneyType.values().length);
    }

}
