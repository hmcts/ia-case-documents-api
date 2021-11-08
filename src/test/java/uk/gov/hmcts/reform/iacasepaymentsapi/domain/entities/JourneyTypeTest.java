package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class JourneyTypeTest {

    @Test
    void has_correct_journey_types() {

        assertSame(JourneyType.from("aip").get(), JourneyType.AIP);
        assertSame(JourneyType.from("rep").get(), JourneyType.REP);
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertSame(2, JourneyType.values().length);
    }
}
