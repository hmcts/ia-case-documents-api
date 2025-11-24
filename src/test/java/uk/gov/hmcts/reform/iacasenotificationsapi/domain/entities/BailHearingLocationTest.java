package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BailHearingLocationTest {

    @Test
    void has_correct_bail_hearing_location() {
        assertThat(BailHearingLocation.from("manchester").get()).isEqualByComparingTo(BailHearingLocation.MANCHESTER);
        assertThat(BailHearingLocation.from("birmingham").get()).isEqualByComparingTo(BailHearingLocation.BIRMINGHAM);
        assertThat(BailHearingLocation.from("bradford").get()).isEqualByComparingTo(BailHearingLocation.BRADFORD);
        assertThat(BailHearingLocation.from("coventry").get()).isEqualByComparingTo(BailHearingLocation.COVENTRY);
        assertThat(BailHearingLocation.from("glasgowTribunalsCentre").get()).isEqualByComparingTo(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE);
        assertThat(BailHearingLocation.from("harmondsworth").get()).isEqualByComparingTo(BailHearingLocation.HARMONDSWORTH);
        assertThat(BailHearingLocation.from("hattonCross").get()).isEqualByComparingTo(BailHearingLocation.HATTON_CROSS);
        assertThat(BailHearingLocation.from("hendon").get()).isEqualByComparingTo(BailHearingLocation.HENDON);
        assertThat(BailHearingLocation.from("newcastle").get()).isEqualByComparingTo(BailHearingLocation.NEWCASTLE);
        assertThat(BailHearingLocation.from("newport").get()).isEqualByComparingTo(BailHearingLocation.NEWPORT);
        assertThat(BailHearingLocation.from("taylorHouse").get()).isEqualByComparingTo(BailHearingLocation.TAYLOR_HOUSE);
        assertThat(BailHearingLocation.from("yarlsWood").get()).isEqualByComparingTo(BailHearingLocation.YARLS_WOOD);
        assertThat(BailHearingLocation.from("bradfordKeighley").get()).isEqualByComparingTo(BailHearingLocation.BRADFORD_KEIGHLEY);
        assertThat(BailHearingLocation.from("mccMinshull").get()).isEqualByComparingTo(BailHearingLocation.MCC_MINSHULL);
        assertThat(BailHearingLocation.from("mccCrownSquare").get()).isEqualByComparingTo(BailHearingLocation.MCC_CROWN_SQUARE);
        assertThat(BailHearingLocation.from("nottingham").get()).isEqualByComparingTo(BailHearingLocation.NOTTINGHAM);
        assertThat(BailHearingLocation.from("manchesterMags").get()).isEqualByComparingTo(BailHearingLocation.MANCHESTER_MAGS);
        assertThat(BailHearingLocation.from("nthTyneMags").get()).isEqualByComparingTo(BailHearingLocation.NTH_TYNE_MAGS);
        assertThat(BailHearingLocation.from("leedsMags").get()).isEqualByComparingTo(BailHearingLocation.LEEDS_MAGS);
        assertThat(BailHearingLocation.from("belfast").get()).isEqualByComparingTo(BailHearingLocation.BELFAST);
        assertThat(BailHearingLocation.from("alloaSherrif").get()).isEqualByComparingTo(BailHearingLocation.ALLOA_SHERRIF);
        assertThat(BailHearingLocation.from("remoteHearing").get()).isEqualByComparingTo(BailHearingLocation.REMOTE_HEARING);
        assertThat(BailHearingLocation.from("decisionWithoutHearing").get()).isEqualByComparingTo(BailHearingLocation.DECISION_WITHOUT_HEARING);
    }

    @Test
    void has_correct_bail_hearing_location_description() {
        assertEquals("Manchester", BailHearingLocation.MANCHESTER.getDescription());
        assertEquals("Birmingham", BailHearingLocation.BIRMINGHAM.getDescription());
        assertEquals("Bradford", BailHearingLocation.BRADFORD.getDescription());
        assertEquals("Coventry Magistrates Court", BailHearingLocation.COVENTRY.getDescription());
        assertEquals("Glasgow", BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE.getDescription());
        assertEquals("Harmondsworth", BailHearingLocation.HARMONDSWORTH.getDescription());
        assertEquals("Hatton Cross", BailHearingLocation.HATTON_CROSS.getDescription());
        assertEquals("Hendon", BailHearingLocation.HENDON.getDescription());
        assertEquals("Newcastle Civil & Family Courts and Tribunals Centre", BailHearingLocation.NEWCASTLE.getDescription());
        assertEquals("Newport", BailHearingLocation.NEWPORT.getDescription());
        assertEquals("Taylor House", BailHearingLocation.TAYLOR_HOUSE.getDescription());
        assertEquals("Yarl's Wood", BailHearingLocation.YARLS_WOOD.getDescription());
        assertEquals("Bradford & Keighley", BailHearingLocation.BRADFORD_KEIGHLEY.getDescription());
        assertEquals("MCC Minshull st", BailHearingLocation.MCC_MINSHULL.getDescription());
        assertEquals("MCC Crown Square", BailHearingLocation.MCC_CROWN_SQUARE.getDescription());
        assertEquals("Nottingham Justice Centre", BailHearingLocation.NOTTINGHAM.getDescription());
        assertEquals("Manchester Mags", BailHearingLocation.MANCHESTER_MAGS.getDescription());
        assertEquals("NTH Tyne Mags", BailHearingLocation.NTH_TYNE_MAGS.getDescription());
        assertEquals("Leeds Mags", BailHearingLocation.LEEDS_MAGS.getDescription());
        assertEquals("Belfast", BailHearingLocation.BELFAST.getDescription());
        assertEquals("Alloa Sherrif Court", BailHearingLocation.ALLOA_SHERRIF.getDescription());
        assertEquals("Remote", BailHearingLocation.REMOTE_HEARING.getDescription());
        assertEquals("Decision without hearing", BailHearingLocation.DECISION_WITHOUT_HEARING.getDescription());
    }

    @Test
    void returns_optional_for_unknown_bail_hearing_location() {
        assertThat(BailHearingLocation.from("unknown")).isEmpty();
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(23, BailHearingLocation.values().length);
    }
}
