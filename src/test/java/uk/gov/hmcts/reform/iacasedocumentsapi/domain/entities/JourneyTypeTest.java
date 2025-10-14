package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType;

class JourneyTypeTest {

    @Test
    public void has_correct_values() {
        assertEquals("rep", JourneyType.REP.toString());
        assertEquals("aip", JourneyType.AIP.toString());
    }

    @Test
    void has_correct_journey_types() {
        assertSame(JourneyType.AIP, JourneyType.from("aip").get());
        assertSame(JourneyType.REP, JourneyType.from("rep").get());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertSame(2, JourneyType.values().length);
    }
}
