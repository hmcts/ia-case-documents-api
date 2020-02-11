package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@RunWith(MockitoJUnitRunner.class)
public class EmailAddressFinderTest {

    @Mock AsylumCase asylumCase;
    @Mock Map<HearingCentre, String> hearingCentreEmailAddresses;
    @Mock Map<HearingCentre, String> homeOfficeEmailAddresses;

    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreEmailAddress = "hearingCentre@example.com";

    private HearingCentre listCaseHearingCentre = HearingCentre.BRADFORD;
    private String listCaseHearingCenterEmailAddress = "listCaseHearingCentre@example.com";

    private EmailAddressFinder emailAddressFinder;


    @Before
    public void setup() {

        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(listCaseHearingCentre));
        when(hearingCentreEmailAddresses.get(hearingCentre)).thenReturn(hearingCentreEmailAddress);
        when(homeOfficeEmailAddresses.get(listCaseHearingCentre)).thenReturn(listCaseHearingCenterEmailAddress);

        emailAddressFinder = new EmailAddressFinder(hearingCentreEmailAddresses, homeOfficeEmailAddresses);
    }

    @Test
    public void should_return_given_email_address_from_lookup_map() {
        assertEquals(hearingCentreEmailAddress, emailAddressFinder.getEmailAddress(asylumCase));
    }

    @Test
    public void should_throw_exception_on_email_address_when_hearing_centre_is_empty() {
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailAddressFinder.getEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentre is not present");
    }

    @Test
    public void should_return_given_list_case_email_address_from_lookup_map() {
        assertEquals(listCaseHearingCenterEmailAddress, emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
    }

    public void should_throw_exception_on_list_case_email_address_when_hearing_centre_is_empty() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }
}