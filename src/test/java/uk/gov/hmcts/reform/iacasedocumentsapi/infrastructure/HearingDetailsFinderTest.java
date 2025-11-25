package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION_DETAIL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.refdata.CourtVenue;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingDetailsFinderTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    BailCase bailCase;
    @Mock
    StringProvider stringProvider;

    private HearingDetailsFinder hearingDetailsFinder;

    private CourtVenue hattonCross;
    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String hearingCentreName = "some hearing centre name";
    private String bailHearingLocationName = "Glasgow";
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingCentreRefDataAddress = "hearing centre address retrieved from ref data";
    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String bailHearingDateTime = "2024-01-01T10:29:00.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    @BeforeEach
    void setUp() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(stringProvider.get("hearingCentreName", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreName));
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class))
                .thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        when(stringProvider.get(HEARING_CENTRE_ADDRESS, BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE.getValue()))
                .thenReturn(Optional.of(
                        "IAC Glasgow, " +
                                "1st Floor, " +
                                "The Glasgow Tribunals Centre, " +
                                "Atlantic Quay, " +
                                "20 York Street, " +
                                "Glasgow, " +
                                "G2 8GT"
                ));
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        hattonCross = new CourtVenue("Hatton Cross Tribunal Hearing Centre",
                "Hatton Cross Tribunal Hearing Centre",
                "386417",
                "Open",
                "Y",
                "Y",
                "York House And Wellington House, 2-3 Dukes Green, Feltham, Middlesex",
                "TW14 0LS");

        hearingDetailsFinder = new HearingDetailsFinder(stringProvider);
    }

    @Test
    void should_return_given_hearing_centre_address() {
        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_when_hearing_centre_address_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.of(
            hearingCentreAddress));

        when(stringProvider.get(HEARING_CENTRE_ADDRESS, hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_return_given_hearing_centre_name() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        when(stringProvider.get(
            "hearingCentreName",
            hearingCentre.toString()
        )).thenReturn(Optional.of(hearingCentreName));

        assertEquals(hearingCentreName, hearingDetailsFinder.getHearingCentreName(asylumCase));
    }

    @Test
    public void should_return_hearing_centre_name_for_decision_without_hearing_appeal() {
        when(asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(
            HearingDetailsFinder.DECISION_WITHOUT_HEARING,
            hearingDetailsFinder.getHearingCentreName(asylumCase)
        );
    }

    @Test
    public void should_throw_exception_when_hearing_centre_name_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreName(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentreName is not present");
    }

    @Test
    public void should_return_given_hearing_date_time() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));

        assertEquals(hearingDateTime, hearingDetailsFinder.getHearingDateTime(asylumCase));
    }

    @Test
    public void should_throw_exception_when_hearing_date_time_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingDateTime(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingDate is not present");
    }

    @Test
    public void should_throw_exception_when_hearing_centre_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_return_hearing_centres_urls() {

        final String birminghamHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/birmingham-immigration-and-asylum-chamber-first-tier-tribunal";
        final String bradfordHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/bradford-tribunal-hearing-centre";
        final String glasgowHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/glasgow-employment-and-immigration-tribunals-eagle-building";
        final String hattonCrossHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/hatton-cross-tribunal-hearing-centre";
        final String taylorHouseHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/taylor-house-tribunal-hearing-centre";
        final String manchesterHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/manchester-tribunal-hearing-centre";
        final String newportHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/newport-south-wales-immigration-and-asylum-tribunal";
        final String nottinghamHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/nottingham-magistrates-court";
        final String northShieldsHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/newcastle-civil-family-courts-and-tribunals-centre";

        assertEquals(birminghamHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(BIRMINGHAM));
        assertEquals(bradfordHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(BRADFORD));
        assertEquals(glasgowHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(GLASGOW));
        assertEquals(hattonCrossHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(HATTON_CROSS));
        assertEquals(taylorHouseHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(TAYLOR_HOUSE));
        assertEquals(manchesterHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(MANCHESTER));
        assertEquals(newportHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(NEWPORT));
        assertEquals(nottinghamHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(NOTTINGHAM));
        assertEquals(northShieldsHearingCentreUrl, hearingDetailsFinder.getHearingCentreUrl(NORTH_SHIELDS));
    }
    @Test
    void getHearingCentreLocation_should_return_remote_hearing_if_remote_field_is_yes_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals("Remote hearing", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void getHearingCentreLocation_should_return_virtual_hearing_if_virtual_field_is_yes_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AsylumCaseDefinition.IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals("IAC National (Virtual)", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void getHearingCentreLocation_should_return_refdata_address_if_remote_field_is_no_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE_ADDRESS, String.class))
            .thenReturn(Optional.of("testAddress"));

        assertEquals("testAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void getHearingCentreLocation_should_return_virtual_hearing_if_virtual_field_is_yes() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AsylumCaseDefinition.IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals("IAC National (Virtual)", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void getHearingCentreLocation_should_return_virtual_hearing_if_virtual_field_is_yes_and_location_ref_data_no() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AsylumCaseDefinition.IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(NO));

        assertEquals("IAC National (Virtual)", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void should_return_given_hearing_centre_address_from_ref_data_if_location_ref_data_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE_ADDRESS, String.class))
            .thenReturn(Optional.of(hearingCentreRefDataAddress));

        assertEquals(hearingCentreRefDataAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
    }

    @Test
    void should_throw_exception_when_hearing_centre_address_is_empty() {
        when(stringProvider.get(HEARING_CENTRE_ADDRESS, hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    void should_return_given_hearing_centre_name() {
        assertEquals(hearingCentreName, hearingDetailsFinder.getHearingCentreName(asylumCase));
    }

    @Test
    void should_throw_exception_when_hearing_centre_name_is_empty() {
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreName(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentreName is not present");
    }

    @Test
    void getHearingCentreName_should_return_remote_hearing_if_remote_field_is_yes_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals("Remote hearing", hearingDetailsFinder.getHearingCentreName(asylumCase));
    }

    @Test
    void getHearingCentreName_should_return_refdata_address_if_remote_field_is_no_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE_ADDRESS, String.class))
            .thenReturn(Optional.of("testAddress"));

        assertEquals("testAddress", hearingDetailsFinder.getHearingCentreName(asylumCase));
    }

    @Test
    void getOldHearingCentreName_should_return_remote_location_name_if_remote_field_is_yes_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));

        assertEquals("Remote hearing", hearingDetailsFinder.getOldHearingCentreName(asylumCase));
    }

    @Test
    void getOldHearingCentreName_should_return_refdata_location_name_if_remote_field_is_no_and_refdata_enabled() {
        when(asylumCase.read(AsylumCaseDefinition.IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(asylumCase.read(AsylumCaseDefinition.LISTING_LOCATION, DynamicList.class))
            .thenReturn(Optional.of(new DynamicList("TestingLocation")));

        assertEquals("TestingLocation", hearingDetailsFinder.getOldHearingCentreName(asylumCase));
    }

    @Test
    void getOldHearingCentreName_should_return_list_case_hearing_centre_if_refdata_not_enabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        assertEquals("Manchester", hearingDetailsFinder.getOldHearingCentreName(asylumCase));
    }

    @Test
    void getOldHearingCentreName_should_throw_exception_when_refdata_not_enabled_and_listCaseHearingCentre_not_present() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getOldHearingCentreName(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    void should_return_given_bail_hearing_location_name() {
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        assertEquals(bailHearingLocationName, hearingDetailsFinder.getBailHearingCentreLocation(bailCase));
    }

    @Test
    void should_throw_exception_when_bail_hearing_location_name_is_empty() {
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getBailHearingCentreLocation(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listingLocation is not present");
    }

    @Test
    void should_return_given_hearing_date_time() {
        assertEquals(hearingDateTime, hearingDetailsFinder.getHearingDateTime(asylumCase));
    }

    @Test
    void should_throw_exception_when_hearing_date_time_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingDateTime(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingDate is not present");
    }

    @Test
    void should_return_given_bail_hearing_date_time() {
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.of(bailHearingDateTime));
        assertEquals(bailHearingDateTime, hearingDetailsFinder.getBailHearingDateTime(bailCase));
    }

    @Test
    void should_throw_exception_when_bail_hearing_date_time_is_empty() {
        when(bailCase.read(LISTING_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getBailHearingDateTime(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listHearingDate is not present");
    }

    @Test
    void should_throw_exception_when_hearing_centre_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    void should_throw_exception_when_hearing_centre_is_empty_in_remote_hearing() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentre is not present");
    }

    @Test
    void should_throw_exception_when_list_hearing_centre_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreLocation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    void should_return_remote_hearing_when_list_hearing_centre_is_remote() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));

        assertEquals("Remote hearing", hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void should_return_hearing_location_address_when_list_hearing_centre_is_not_remote() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));

        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreLocation(asylumCase));
    }

    @Test
    void should_return_listing_location_address_from_ccd_if_disabled_ref_data_flag() {


        assertEquals("Glasgow\nIAC Glasgow, " +
                         "1st Floor, " +
                         "The Glasgow Tribunals Centre, " +
                         "Atlantic Quay, " +
                         "20 York Street, " +
                         "Glasgow, " +
                         "G2 8GT",
                     hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase));
    }

    @Test
    void should_return_remote_address_with_enabled_ref_data_flag() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_REMOTE_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));

        assertEquals("Cloud Video Platform (CVP)",
                     hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase));
    }

    @Test
    void should_return_listing_location_address_from_ref_data_with_enabled_ref_data_flag() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(bailCase.read(REF_DATA_LISTING_LOCATION_DETAIL, CourtVenue.class)).thenReturn(Optional.of(hattonCross));

        assertEquals("Hatton Cross Tribunal Hearing Centre, " +
                         "York House And Wellington House, 2-3 Dukes Green, Feltham, Middlesex, " +
                         "TW14 0LS",
                     hearingDetailsFinder.getListingLocationAddressFromRefDataOrCcd(bailCase));
    }
}

