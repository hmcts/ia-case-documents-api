package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeNocRequestDecisionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    @Mock
    EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;

    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicationType = "withdraw";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private String homeOfficeApplyNocDecisionBeforeListingTemplateId = "SomeTemplate";
    private String homeOfficeApplyNocDecisionAfterListingTemplateId = "SomeTemplate";


    private String apcPrivateBetaInboxHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private String respondentReviewDirectionEmail = "homeoffice-respondent@example.com";
    private String homeOfficeHearingCentreEmail = "hc-taylorhouse@example.com";
    private String homeOfficeEmail = "ho-taylorhouse@example.com";



    private HomeOfficeNocRequestDecisionPersonalisation homeOfficeNocRequestDecisionPersonalisation;

    @BeforeEach
    void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)))
            .thenReturn(homeOfficeHearingCentreEmail);
        when((emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeEmail);

        homeOfficeNocRequestDecisionPersonalisation = new HomeOfficeNocRequestDecisionPersonalisation(
            homeOfficeApplyNocDecisionBeforeListingTemplateId,
            homeOfficeApplyNocDecisionAfterListingTemplateId,
            apcPrivateBetaInboxHomeOfficeEmailAddress,
            respondentReviewDirectionEmail,
            iaExUiFrontendUrl,
            customerServicesProvider,
            appealService,
            emailAddressFinder
        );
    }

    @Test
    void should_return_given_template_id() {

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(homeOfficeApplyNocDecisionBeforeListingTemplateId,
            homeOfficeNocRequestDecisionPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(homeOfficeApplyNocDecisionAfterListingTemplateId,
            homeOfficeNocRequestDecisionPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(homeOfficeApplyNocDecisionBeforeListingTemplateId,
            homeOfficeNocRequestDecisionPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(homeOfficeApplyNocDecisionAfterListingTemplateId,
            homeOfficeNocRequestDecisionPersonalisation.getTemplateId(asylumCase));

    }

    @Test
    void should_return_null_on_personalisation_when_case_is_state_before_reinstate_is_not_present() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_NOC_REQUEST_DECISION_HOME_OFFICE",
            homeOfficeNocRequestDecisionPersonalisation.getReferenceId(caseId));
    }


    @Test
    void test_email_address_for_home_office_when_legal_rep_applied() {


        List<State> apcEmail = newArrayList(
            State.APPEAL_STARTED,
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.PENDING_PAYMENT,
            State.ENDED

        );

        List<State> lartEmail = newArrayList(
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING
        );

        List<State> pouEmail = newArrayList(
            State.ADJOURNED,
            State.PREPARE_FOR_HEARING,
            State.FINAL_BUNDLING,
            State.PRE_HEARING,
            State.DECISION,
            State.DECIDED,
            State.FTPA_SUBMITTED,
            State.FTPA_DECIDED
        );

        Map<String, List<State>> states = new HashMap<>();

        states.put(apcPrivateBetaInboxHomeOfficeEmailAddress, apcEmail);
        states.put(respondentReviewDirectionEmail, lartEmail);
        states.put(homeOfficeHearingCentreEmail, pouEmail);

        Set<String> emailAddresses = states.keySet();

        for (String emailAddress : emailAddresses) {
            List<State> statesList = states.get(emailAddress);
            for (State state : statesList) {
                if (emailAddress.equals(homeOfficeHearingCentreEmail)) {
                    when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));
                    when(appealService.isAppealListed(asylumCase)).thenReturn(true);
                    when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
                    assertTrue(homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                } else {
                    when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));
                    assertTrue(homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                }
            }
        }
    }

    @Test
    void test_email_address_for_home_office_when_generic_ho_applied() {


        List<State> apcEmail = newArrayList(
            State.APPEAL_STARTED,
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.PENDING_PAYMENT,
            State.ENDED

        );

        List<State> lartEmail = newArrayList(
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING
        );

        List<State> pouEmail = newArrayList(
            State.ADJOURNED,
            State.PREPARE_FOR_HEARING,
            State.FINAL_BUNDLING,
            State.PRE_HEARING,
            State.DECISION,
            State.DECIDED,
            State.FTPA_SUBMITTED,
            State.FTPA_DECIDED
        );

        Map<String, List<State>> states = new HashMap<>();

        states.put(apcPrivateBetaInboxHomeOfficeEmailAddress, apcEmail);
        states.put(respondentReviewDirectionEmail, lartEmail);
        states.put(homeOfficeHearingCentreEmail, pouEmail);

        Set<String> emailAddresses = states.keySet();

        for (String emailAddress : emailAddresses) {
            List<State> statesList = states.get(emailAddress);
            for (State state : statesList) {
                if (emailAddress.equals(homeOfficeHearingCentreEmail)) {
                    when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));
                    when(appealService.isAppealListed(asylumCase)).thenReturn(true);
                    when(asylumCase.read(HEARING_CENTRE)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
                    assertTrue(homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                } else {
                    when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));
                    assertTrue(homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                }
            }
        }
    }

    @Test
    void should_throw_exception_when_cannot_find_email_address_for_home_office() {


        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.UNKNOWN));

        assertThatThrownBy(() -> homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("homeOffice email Address cannot be found");
    }

    @Test
    void should_return_null_for_home_office_email_when_state_not_defined() {
        assertEquals(Collections.emptySet(), homeOfficeNocRequestDecisionPersonalisation.getRecipientsList(asylumCase));
    }


    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeNocRequestDecisionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(homeOfficeNocRequestDecisionPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation = homeOfficeNocRequestDecisionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(homeOfficeNocRequestDecisionPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeNocRequestDecisionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
