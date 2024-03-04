package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailHearingLocation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HearingDetailsFinderTest {

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";
    @Mock
    AsylumCase asylumCase;
    @Mock
    BailCase bailCase;
    @Mock
    StringProvider stringProvider;
    private HearingDetailsFinder hearingDetailsFinder;
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String hearingCentreName = "some hearing centre name";
    private String bailHearingLocationName = "Glasgow";
    private String hearingCentreAddress = "some hearing centre address";
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

        hearingDetailsFinder = new HearingDetailsFinder(
            stringProvider
        );
    }

    @Test
    void should_return_given_hearing_centre_address() {
        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
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
    void should_return_given_bail_hearing_location_name() {
        when(bailCase.read(LISTING_LOCATION, BailHearingLocation.class)).thenReturn(Optional.of(BailHearingLocation.GLASGOW_TRIBUNAL_CENTRE));
        assertEquals(bailHearingLocationName, hearingDetailsFinder.getBailHearingCentreLocation(bailCase));
    }

    @Test
    void should_throw_exception_when_bail_hearing_location_name_is_empty() {
        when(bailCase.read(LISTING_LOCATION, String.class)).thenReturn(Optional.empty());

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
}
