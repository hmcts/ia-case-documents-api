package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.*;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
public class HearingDetailsFinderTest {

    @Mock AsylumCase asylumCase;
    @Mock StringProvider stringProvider;

    private HearingDetailsFinder hearingDetailsFinder;

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreName = "some hearing centre name";
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingDateTime = "2019-08-27T14:25:15.000";

    @Before
    public void setUp() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreAddress));
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreName));

        hearingDetailsFinder = new HearingDetailsFinder(
            stringProvider
        );
    }

    @Test
    public void should_return_given_hearing_centre_address() {
        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_when_hearing_centre_address_is_empty() {
        when(stringProvider.get(HEARING_CENTRE_ADDRESS, hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_return_given_hearing_centre_name() {
        assertEquals(hearingCentreName, hearingDetailsFinder.getHearingCentreName(asylumCase));
    }

    @Test
    public void should_throw_exception_when_hearing_centre_name_is_empty() {
        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreName(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentreName is not present");
    }

    @Test
    public void should_return_given_hearing_date_time() {
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
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_return_hearing_centres_urls() {

        String birminghamHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/birmingham-immigration-and-asylum-chamber-first-tier-tribunal";
        String bradfordHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/bradford-tribunal-hearing-centre";
        String glasgowHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/glasgow-employment-and-immigration-tribunals-eagle-building";
        String hattonCrossHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/hatton-cross-tribunal-hearing-centre";
        String taylorHouseHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/taylor-house-tribunal-hearing-centre";
        String manchesterHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/manchester-tribunal-hearing-centre";
        String newportHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/newport-south-wales-immigration-and-asylum-tribunal";
        String nottinghamHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/nottingham-magistrates-court";
        String northShieldsHearingCentreUrl = "https://courttribunalfinder.service.gov.uk/courts/newcastle-civil-family-courts-and-tribunals-centre";

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
}
