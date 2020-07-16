package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(MockitoJUnitRunner.class)
public class RespondentEditAppealAfterSubmitPersonalisationTest {

    @Mock AsylumCase asylumCase;

    @Mock EmailAddressFinder emailAddressFinder;
    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private final String templateIdBeforeListing = "someTemplateBeforeListing";
    private final String templateIdAfterListing = "someTemplateAfterListing";

    private final String homeOfficeApcEmailAddress = "homeOfficeAPC@example.com";
    private final String homeOfficeLartEmailAddress = "homeOfficeLART@example.com";
    private final String ariaListingReference = "someAriaListingReference";
    private final String appealReferenceNumber = "hmctsReference";
    private final String homeOfficeReference = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";

    private RespondentEditAppealAfterSubmitPersonalisation personalisation;

    @Before
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        personalisation = new RespondentEditAppealAfterSubmitPersonalisation(
            templateIdBeforeListing,
            templateIdAfterListing,
            homeOfficeApcEmailAddress,
            homeOfficeLartEmailAddress,
            emailAddressFinder,
            iaExUiFrontendUrl,
            customerServicesProvider);
    }

    @Test
    public void should_return_the_given_template_id() {
        assertEquals(templateIdBeforeListing, personalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));

        assertEquals(templateIdAfterListing, personalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_ho_apc_email_address_until_case_under_review() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.CASE_UNDER_REVIEW));

        assertEquals(Collections.singleton(homeOfficeApcEmailAddress), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_lart_email_address_until_listing() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.LISTING));

        assertEquals(Collections.singleton(homeOfficeLartEmailAddress), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ho_hearing_centre_email_address_after_listing() {
        String homeOfficeBhamEmailAddress = "ho-birmingham@example.com";
        when(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeBhamEmailAddress);
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.PRE_HEARING));

        assertEquals(Collections.singleton(homeOfficeBhamEmailAddress), personalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_throw_exception_when_home_office_is_missing_in_the_case_data() {
        when(asylumCase.read(AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("currentCaseStateVisibleToHomeOfficeAll flag is not present");
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_EDIT_APPEAL_AFTER_SUBMIT_RESPONDENT", personalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = this.personalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReference, personalisation.get("homeOfficeReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(personalisation.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertTrue(personalisation.isAppealListed(asylumCase));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

}
