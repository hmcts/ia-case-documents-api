package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.HomeOfficePersonalisationFactory;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HomeOfficeCaseListedNotifierTest {

    private static final String HOME_OFFICE_MANCHESTER_EMAIL_ADDRESS = "ReformpbManchester@example.com";
    private static final String HOME_OFFICE_TAYLOR_HOUSE_EMAIL_ADDRESS = "ReformpbCentral@example.com";

    @Mock private HomeOfficePersonalisationFactory homeOfficePersonalisationFactory;
    @Mock private Map<HearingCentre, String> homeOfficeEmailAddresses;
    @Mock private AsylumCase asylumCase;

    private HomeOfficeCaseListedNotifier homeOfficeCaseListedNotifier;

    @Before
    public void setUp() {

        homeOfficeCaseListedNotifier =
            new HomeOfficeCaseListedNotifier(
                homeOfficePersonalisationFactory,
                homeOfficeEmailAddresses
            );

        when(homeOfficeEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(HOME_OFFICE_MANCHESTER_EMAIL_ADDRESS);
        when(homeOfficeEmailAddresses.get(HearingCentre.TAYLOR_HOUSE)).thenReturn(HOME_OFFICE_TAYLOR_HOUSE_EMAIL_ADDRESS);
    }

    @Test
    public void should_throw_when_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeCaseListedNotifier.getEmailAddress(asylumCase))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_home_office_email_address_not_found() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(homeOfficeEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(null);

        assertThatThrownBy(() -> homeOfficeCaseListedNotifier.getEmailAddress(asylumCase))
            .hasMessage("Hearing centre email address not found: manchester")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_email_address_for_manchester_home_office() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));

        final String actualEmailAddress = homeOfficeCaseListedNotifier.getEmailAddress(asylumCase);

        assertEquals(HOME_OFFICE_MANCHESTER_EMAIL_ADDRESS, actualEmailAddress);
    }

    @Test
    public void should_return_email_address_for_taylor_house_home_office() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        final String actualEmailAddress = homeOfficeCaseListedNotifier.getEmailAddress(asylumCase);

        assertEquals(HOME_OFFICE_TAYLOR_HOUSE_EMAIL_ADDRESS, actualEmailAddress);
    }

    @Test
    public void should_return_personalisation_for_home_office() {

        homeOfficeCaseListedNotifier.getPersonalisation(asylumCase);

        verify(homeOfficePersonalisationFactory, times(1)).createListedCase(asylumCase);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new HomeOfficeCaseListedNotifier(null, homeOfficeEmailAddresses))
            .hasMessage("homeOfficePersonalisationFactory must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new HomeOfficeCaseListedNotifier(homeOfficePersonalisationFactory, null))
            .hasMessage("homeOfficeEmailAddresses must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

