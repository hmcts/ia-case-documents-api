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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.CaseOfficerPersonalisationFactory;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class CaseOfficerCaseListedNotifierTest {

    private static final String MANCHESTER_EMAIL_ADDRESS = "manchester@example.com";
    private static final String TAYLOR_HOUSE_EMAIL_ADDRESS = "taylorHouse@example.com";

    @Mock private CaseOfficerPersonalisationFactory caseOfficerPersonalisationFactory;
    @Mock private Map<HearingCentre, String> hearingCentreEmailAddresses;
    @Mock private AsylumCase asylumCase;

    private CaseOfficerCaseListedNotifier caseOfficerCaseListedNotifier;

    @Before
    public void setUp() {

        caseOfficerCaseListedNotifier =
            new CaseOfficerCaseListedNotifier(
                caseOfficerPersonalisationFactory,
                hearingCentreEmailAddresses
            );

        when(hearingCentreEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(MANCHESTER_EMAIL_ADDRESS);
        when(hearingCentreEmailAddresses.get(HearingCentre.TAYLOR_HOUSE)).thenReturn(TAYLOR_HOUSE_EMAIL_ADDRESS);
    }

    @Test
    public void should_throw_when_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> caseOfficerCaseListedNotifier.getEmailAddress(asylumCase))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_throw_when_hearing_centre_email_address_not_found() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(hearingCentreEmailAddresses.get(HearingCentre.MANCHESTER)).thenReturn(null);

        assertThatThrownBy(() -> caseOfficerCaseListedNotifier.getEmailAddress(asylumCase))
            .hasMessage("Hearing centre email address not found: manchester")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void should_return_email_address_for_manchester_hearing_centre() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));

        final String actualEmailAddress = caseOfficerCaseListedNotifier.getEmailAddress(asylumCase);

        assertEquals(MANCHESTER_EMAIL_ADDRESS, actualEmailAddress);
    }

    @Test
    public void should_return_email_address_for_taylor_house_hearing_centre() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        final String actualEmailAddress = caseOfficerCaseListedNotifier.getEmailAddress(asylumCase);

        assertEquals(TAYLOR_HOUSE_EMAIL_ADDRESS, actualEmailAddress);
    }

    @Test
    public void should_return_personalisation_for_case_officer() {

        caseOfficerCaseListedNotifier.getPersonalisation(asylumCase);

        verify(caseOfficerPersonalisationFactory, times(1)).createListedCase(asylumCase);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new CaseOfficerCaseListedNotifier(null, hearingCentreEmailAddresses))
            .hasMessage("caseOfficerPersonalisationFactory must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new CaseOfficerCaseListedNotifier(caseOfficerPersonalisationFactory, null))
            .hasMessage("hearingCentreEmailAddresses must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}

