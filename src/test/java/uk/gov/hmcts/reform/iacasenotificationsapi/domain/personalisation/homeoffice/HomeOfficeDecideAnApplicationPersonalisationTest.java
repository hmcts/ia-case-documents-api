package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeDecideAnApplicationPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock CustomerServicesProvider customerServicesProvider;
    @Mock MakeAnApplicationService makeAnApplicationService;
    @Mock EmailAddressFinder  emailAddressFinder;
    @Mock MakeAnApplication makeAnApplication;

    private Long caseId = 12345L;
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private final String homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId = "someTemplateGrantedBeforeListing";
    private final String homeOfficeDecideAnApplicationGrantedAfterListingTemplateId = "SomeTemplateGrantedAfterListing";
    private final String homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId = "SomeTemplateOtherGrantedBeforeListing";
    private final String homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId = "SomeTemplateOtherGrantedAfterListing";
    private final String homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId = "SomeTemplateRefusedBeforeListing";
    private final String homeOfficeDecideAnApplicationRefusedAfterListingTemplateId = "SomeTemplateRefusedAfterListing";
    private final String homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId = "SomeTemplateOtherRefusedBeforeListing";
    private final String homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId = "SomeTemplateOtherRefusedAfterListing";

    private String apcHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private String lartHomeOfficeEmailAddress = "homeoffice-respondent@example.com";
    private String homeOfficeHearingCentreEmail = "ho-taylorhouse@example.com";

    private String legalRepUser = "caseworker-ia-legalrep-solicitor";
    private String homeOfficeLart = "caseworker-ia-homeofficelart";
    private String homeOfficeApc = "caseworker-ia-homeofficeapc";
    private String homeOfficePou = "caseworker-ia-homeofficepou";
    private String homeOfficeRespondent = "caseworker-ia-respondentofficer";


    private HomeOfficeDecideAnApplicationPersonalisation homeOfficeDecideAnApplicationPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeHearingCentreEmail);
        when((makeAnApplicationService.getMakeAnApplication(asylumCase))).thenReturn(Optional.of(makeAnApplication));

        homeOfficeDecideAnApplicationPersonalisation = new HomeOfficeDecideAnApplicationPersonalisation(
                homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId,
                homeOfficeDecideAnApplicationGrantedAfterListingTemplateId,
                homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId,
                homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
                homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId,
                homeOfficeDecideAnApplicationRefusedAfterListingTemplateId,
                homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId,
                homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
                apcHomeOfficeEmailAddress,
                lartHomeOfficeEmailAddress,
                iaExUiFrontendUrl,
                customerServicesProvider,
                makeAnApplicationService,
                emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeApc);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertEquals(homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));


        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        assertEquals(homeOfficeDecideAnApplicationGrantedAfterListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplication.getApplicantRole()).thenReturn(legalRepUser);
        when(makeAnApplication.getState()).thenReturn("listing");
        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        assertEquals(homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));


        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        assertEquals(homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeApc);
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertEquals(homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));


        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        assertEquals(homeOfficeDecideAnApplicationRefusedAfterListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplication.getApplicantRole()).thenReturn(legalRepUser);
        when(makeAnApplication.getState()).thenReturn("listing");
        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        assertEquals(homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));


        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        assertEquals(homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId, homeOfficeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_HOME_OFFICE", homeOfficeDecideAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void test_email_address_for_roles() {

        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeApc);
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertTrue(homeOfficeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(apcHomeOfficeEmailAddress));

        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeLart);
        assertTrue(homeOfficeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(lartHomeOfficeEmailAddress));

        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficePou);
        assertTrue(homeOfficeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeHearingCentreEmail));
    }

    @Test
    public void test_email_address_for_home_office_when_legal_rep_applied() {

        when(makeAnApplication.getApplicantRole()).thenReturn(legalRepUser);
        List<String> apcEmail = newArrayList(
                "appealSubmitted",
                "pendingPayment",
                "awaitingRespondentEvidence",
                "caseBuilding",
                "caseUnderReview",
                "ended"


        );

        List<String> lartEmail = newArrayList(
                "respondentReview",
                "listing",
                "submitHearingRequirements"
        );

        List<String> pouEmail = newArrayList(
                "prepareForHearing",
                "finalBundling",
                "preHearing",
                "decided",
                "ftpaSubmitted",
                "ftpaDecided",
                "adjourned",
                "decision"

        );

        Map<String, List<String>> states = new HashMap<>();

        states.put(apcHomeOfficeEmailAddress, apcEmail);
        states.put(lartHomeOfficeEmailAddress, lartEmail);
        states.put(homeOfficeHearingCentreEmail, pouEmail);

        Set<String> emailAddresses = states.keySet();

        for (String emailAddress: emailAddresses) {
            List<String> statesList = states.get(emailAddress);
            for (String state: statesList) {

                if (emailAddress != null && emailAddress.equals(homeOfficeHearingCentreEmail)) {
                    when(makeAnApplicationService.isApplicationListed(State.get(state))).thenReturn(true);
                }

                when(makeAnApplication.getState()).thenReturn(state);
                assertTrue(homeOfficeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(emailAddress));
            }
        }

    }

    @Test
    public void test_email_address_for_home_office_when_generic_ho_applied() {

        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeRespondent);

        List<String> apcEmail = newArrayList(
                "appealSubmitted",
                "pendingPayment",
                "awaitingRespondentEvidence",
                "caseBuilding",
                "caseUnderReview",
                "ended"


        );


        List<String> lartEmail = newArrayList(
                "respondentReview",
                "listing",
                "submitHearingRequirements"
        );

        List<String> pouEmail = newArrayList(
                "prepareForHearing",
                "finalBundling",
                "preHearing",
                "decided",
                "ftpaSubmitted",
                "ftpaDecided",
                "adjourned",
                "decision"
        );

        Map<String, List<String>> states = new HashMap<>();

        states.put(apcHomeOfficeEmailAddress, apcEmail);
        states.put(lartHomeOfficeEmailAddress, lartEmail);
        states.put(homeOfficeHearingCentreEmail, pouEmail);

        Set<String> emailAddresses = states.keySet();

        for (String emailAddress: emailAddresses) {
            List<String> statesList = states.get(emailAddress);
            for (String state: statesList) {

                if (emailAddress != null && emailAddress.equals(homeOfficeHearingCentreEmail)) {
                    when(makeAnApplicationService.isApplicationListed(State.get(state))).thenReturn(true);
                }

                when(makeAnApplication.getState()).thenReturn(state);
                assertTrue(homeOfficeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(emailAddress));
            }
        }

    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_home_office() {

        when(makeAnApplication.getApplicantRole()).thenReturn("");
        when(makeAnApplication.getState()).thenReturn("appealTakenOffline");

        assertThatThrownBy(() -> homeOfficeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("homeOffice email Address cannot be found");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeDecideAnApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = homeOfficeDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        when(makeAnApplication.getDecisionReason()).thenReturn("No Reason Given");
        when(makeAnApplication.getDecisionMaker()).thenReturn("Judge");
        when(makeAnApplication.getType()).thenReturn("Other");

        Map<String, String> personalisation = homeOfficeDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);


        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("Other",personalisation.get("applicationType"));
        assertEquals("No Reason Given",personalisation.get("applicationDecisionReason"));
        assertEquals("Judge",personalisation.get("decisionMaker"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
