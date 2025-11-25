package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BailHearingCentreTest {

    @Test
    void has_correct_values() {
        assertEquals("birmingham", BailHearingCentre.BIRMINGHAM.toString());
        assertEquals("bradford", BailHearingCentre.BRADFORD.toString());
        assertEquals("glasgow", BailHearingCentre.GLASGOW.toString());
        assertEquals("hattonCross", BailHearingCentre.HATTON_CROSS.toString());
        assertEquals("manchester", BailHearingCentre.MANCHESTER.toString());
        assertEquals("newport", BailHearingCentre.NEWPORT.toString());
        assertEquals("taylorHouse", BailHearingCentre.TAYLOR_HOUSE.toString());
        assertEquals("yarlsWood", BailHearingCentre.YARLS_WOOD.toString());
    }

    @Test
    void can_be_created_from() {
        assertEquals(BailHearingCentre.BRADFORD, BailHearingCentre.from("bradford").get());
        assertEquals(BailHearingCentre.BIRMINGHAM, BailHearingCentre.from("birmingham").get());
        assertEquals(BailHearingCentre.GLASGOW, BailHearingCentre.from("glasgow").get());
        assertEquals(BailHearingCentre.HATTON_CROSS, BailHearingCentre.from("hattonCross").get());
        assertEquals(BailHearingCentre.MANCHESTER, BailHearingCentre.from("manchester").get());
        assertEquals(BailHearingCentre.NEWPORT, BailHearingCentre.from("newport").get());
        assertEquals(BailHearingCentre.TAYLOR_HOUSE, BailHearingCentre.from("taylorHouse").get());
        assertEquals(BailHearingCentre.YARLS_WOOD, BailHearingCentre.from("yarlsWood").get());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(8, BailHearingCentre.values().length);
    }
}
