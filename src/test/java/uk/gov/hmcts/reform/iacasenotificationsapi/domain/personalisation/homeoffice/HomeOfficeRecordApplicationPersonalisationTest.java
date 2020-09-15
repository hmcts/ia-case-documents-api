package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecordApplicationRespondentFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeRecordApplicationPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock Map<HearingCentre, String> homeOfficeEmailAddressMap;
    @Mock CustomerServicesProvider customerServicesProvider;
    RecordApplicationRespondentFinder recordApplicationRespondentFinder;

    private Long caseId = 12345L;
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String afterListingTemplateId = "afterListingTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String homeOfficeEmailAddress = "homeoffice@example.com";
    private String defaultHomeOfficeEmailAddress = "defaulthomeoffice@example.com";
    private String respondentReviewEmailAddress = "respondentReview@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicationType = "someApplicationType";
    private String expectedApplicationType = "someapplicationtype";
    private String applicationDecisionReason = "someApplicationDecisionReason";
    private String applicationSupplier = "someApplicationSupplier";
    private String expectedApplicationSupplier = "someapplicationsupplier";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private HomeOfficeRecordApplicationPersonalisation homeOfficeRecordApplicationPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.of(applicationType));
        when(asylumCase.read(APPLICATION_DECISION_REASON, String.class)).thenReturn(Optional.of(applicationDecisionReason));
        when(asylumCase.read(APPLICATION_SUPPLIER, String.class)).thenReturn(Optional.of(applicationSupplier));
        when(homeOfficeEmailAddressMap.get(hearingCentre)).thenReturn(homeOfficeEmailAddress);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        recordApplicationRespondentFinder = new RecordApplicationRespondentFinder(defaultHomeOfficeEmailAddress, respondentReviewEmailAddress,
                homeOfficeEmailAddressMap);

        homeOfficeRecordApplicationPersonalisation = new HomeOfficeRecordApplicationPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            defaultHomeOfficeEmailAddress,
            respondentReviewEmailAddress,
            iaExUiFrontendUrl,
            homeOfficeEmailAddressMap,
            customerServicesProvider,
            recordApplicationRespondentFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId, homeOfficeRecordApplicationPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertEquals(afterListingTemplateId, homeOfficeRecordApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_RECORD_APPLICATION_HOME_OFFICE", homeOfficeRecordApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeRecordApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = homeOfficeRecordApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(expectedApplicationType, personalisation.get("applicationType"));
        assertEquals(applicationDecisionReason, personalisation.get("applicationDecisionReason"));
        assertEquals(expectedApplicationSupplier, personalisation.get("applicationSupplier"));
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
        when(asylumCase.read(APPLICATION_TYPE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_DECISION_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPLICATION_SUPPLIER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeRecordApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("applicationType"));
        assertEquals("No reason given", personalisation.get("applicationDecisionReason"));
        assertEquals("", personalisation.get("applicationSupplier"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(homeOfficeRecordApplicationPersonalisation.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertTrue(homeOfficeRecordApplicationPersonalisation.isAppealListed(asylumCase));
    }

    @Test
    public void test_email_address_for_states() {
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        List<State> noEmailStates = newArrayList(
                State.APPEAL_STARTED,
                State.FTPA_SUBMITTED,
                State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS
        );

        List<State> apcEmail = newArrayList(
                State.APPEAL_SUBMITTED,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.AWAITING_REASONS_FOR_APPEAL,
                State.REASONS_FOR_APPEAL_SUBMITTED,
                State.SUBMIT_HEARING_REQUIREMENTS,
                State.LISTING
        );

        List<State> lartEmail = newArrayList(
                State.RESPONDENT_REVIEW
        );

        List<State> pouEmail = newArrayList(
                State.PREPARE_FOR_HEARING,
                State.FINAL_BUNDLING,
                State.PRE_HEARING,
                State.DECISION,
                State.DECIDED
        );

        Map<String, List<State>> states = new HashMap<>();

        states.put("defaulthomeoffice@example.com", apcEmail);
        states.put("respondentReview@example.com", lartEmail);
        states.put("homeoffice@example.com", pouEmail);
        states.put(null, noEmailStates);

        Set<String> emailAddresses = states.keySet();

        for (String emailAddress: emailAddresses) {
            List<State> statesList = states.get(emailAddress);
            for (State state: statesList) {
                when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
                assertEquals(emailAddress, homeOfficeRecordApplicationPersonalisation.getRespondentEmailAddress(asylumCase));
            }
        }

    }
}
