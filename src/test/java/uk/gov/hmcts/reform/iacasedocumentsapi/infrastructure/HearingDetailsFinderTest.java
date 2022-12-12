package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingDetailsFinderTest {

    @Mock AsylumCase asylumCase;
    @Mock StringProvider stringProvider;

    private HearingDetailsFinder hearingDetailsFinder;

    private static final String HEARING_CENTRE_ADDRESS = "hearingCentreAddress";

    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreName = "some hearing centre name";
    private String hearingCentreAddress = "some hearing centre address";
    private String hearingDateTime = "2019-08-27T14:25:15.000";

    @BeforeEach
    public void setUp() {

        hearingDetailsFinder = new HearingDetailsFinder(
            stringProvider
        );
    }

    @Test
    public void should_return_given_hearing_centre_address() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreAddress));
        assertEquals(hearingCentreAddress, hearingDetailsFinder.getHearingCentreAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_when_hearing_centre_address_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreAddress));

        when(stringProvider.get(HEARING_CENTRE_ADDRESS, hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_return_given_hearing_centre_name() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        when(stringProvider.get("hearingCentreName", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreName));

        assertEquals(hearingCentreName, hearingDetailsFinder.getHearingCentreName(asylumCase));
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
}
