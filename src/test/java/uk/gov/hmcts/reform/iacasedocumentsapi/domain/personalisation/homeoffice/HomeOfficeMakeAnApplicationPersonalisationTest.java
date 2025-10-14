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

import java.util.Arrays;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeMakeAnApplicationPersonalisationTest {

    private static final String ADMIN_OFFICER = "ctsc";
    private static final String LEGAL_REP_USER = "caseworker-ia-legalrep-solicitor";
    private static final String CITIZEN_USER = "citizen";
    private static final String HOME_OFFICE_LART = "caseworker-ia-homeofficelart";
    private static final String HOME_OFFICE_APC = "caseworker-ia-homeofficeapc";
    private static final String HOME_OFFICE_POU = "caseworker-ia-homeofficepou";
    private static final String HOME_OFFICE_RESPONDENT = "caseworker-ia-respondentofficer";

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    UserDetailsProvider userDetailsProvider;
    @Mock
    UserDetails userDetails;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    MakeAnApplication makeAnApplication;

    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someAriaListingReference";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private final String homeOfficeMakeAnApplicationBeforeListingTemplateId = "SomeTemplate";
    private final String homeOfficeMakeAnApplicationAfterListingTemplateId = "SomeTemplate";
    private final String homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId = "SomeTemplate";
    private final String homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId = "SomeTemplate";

    private final String apcPrivateBetaInboxHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private final String respondentReviewDirectionEmail = "homeoffice-respondent@example.com";
    private final String homeOfficeHearingCentreEmail = "hc-taylorhouse@example.com";
    private final String homeOfficeEmail = "ho-taylorhouse@example.com";


    private HomeOfficeMakeAnApplicationPersonalisation homeOfficeMakeAnApplicationPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(makeAnApplicationService.getMakeAnApplication(asylumCase, false)).thenReturn(Optional.of(makeAnApplication));
        String applicationType = "withdraw";
        when(makeAnApplication.getType()).thenReturn(applicationType);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)))
            .thenReturn(homeOfficeHearingCentreEmail);
        when((emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeEmail);

        homeOfficeMakeAnApplicationPersonalisation = new HomeOfficeMakeAnApplicationPersonalisation(
            homeOfficeMakeAnApplicationBeforeListingTemplateId,
            homeOfficeMakeAnApplicationAfterListingTemplateId,
            homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId,
            homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId,
            apcPrivateBetaInboxHomeOfficeEmailAddress,
            respondentReviewDirectionEmail,
            iaExUiFrontendUrl,
            customerServicesProvider,
            appealService,
            userDetailsProvider,
            emailAddressFinder,
            makeAnApplicationService
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(userDetails.getRoles()).thenReturn(
            List.of(LEGAL_REP_USER)
        );
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(homeOfficeMakeAnApplicationBeforeListingTemplateId,
            homeOfficeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(userDetails.getRoles()).thenReturn(
            List.of(CITIZEN_USER)
        );
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(homeOfficeMakeAnApplicationAfterListingTemplateId,
            homeOfficeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));


        List<String> roles = Arrays.asList(HOME_OFFICE_APC, HOME_OFFICE_LART, HOME_OFFICE_RESPONDENT, HOME_OFFICE_POU);
        for (String role : roles) {
            when(userDetails.getRoles()).thenReturn(
                Collections.singletonList(role)
            );
            when(appealService.isAppealListed(asylumCase)).thenReturn(false);
            assertEquals(homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId,
                homeOfficeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

            when(appealService.isAppealListed(asylumCase)).thenReturn(true);
            assertEquals(homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId,
                homeOfficeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));
        }
    }

    @Test
    public void should_return_null_on_personalisation_when_case_is_state_before_reinstate_is_not_present() {
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        Long caseId = 12345L;
        assertEquals(caseId + "_MAKE_AN_APPLICATION_HOME_OFFICE",
            homeOfficeMakeAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void test_email_address_for_roles() {

        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.APPEAL_SUBMITTED));

        when(userDetails.getRoles()).thenReturn(
            List.of(HOME_OFFICE_APC)
        );
        assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(apcPrivateBetaInboxHomeOfficeEmailAddress));

        when(userDetails.getRoles()).thenReturn(
            List.of(HOME_OFFICE_LART)
        );
        assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(respondentReviewDirectionEmail));

        when(userDetails.getRoles()).thenReturn(
            List.of(HOME_OFFICE_POU)
        );
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(homeOfficeHearingCentreEmail));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmail));

    }

    @ParameterizedTest
    @ValueSource(strings = { LEGAL_REP_USER, ADMIN_OFFICER })
    public void test_email_address_for_home_office_when_legal_rep_or_admin_applied(String role) {

        when(userDetails.getRoles()).thenReturn(
            Collections.singletonList(role)
        );

        List<State> apcEmail = newArrayList(
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.REASONS_FOR_APPEAL_SUBMITTED,
            State.AWAITING_REASONS_FOR_APPEAL,
            State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
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
            State.FTPA_DECIDED,
            State.REMITTED
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
                    assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                } else {
                    when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));
                    assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                }
            }
        }
    }

    @Test
    public void test_email_address_for_home_office_when_generic_ho_applied() {

        when(userDetails.getRoles()).thenReturn(
            List.of(HOME_OFFICE_RESPONDENT)
        );

        List<State> apcEmail = newArrayList(
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.REASONS_FOR_APPEAL_SUBMITTED,
            State.AWAITING_REASONS_FOR_APPEAL,
            State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
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
            State.FTPA_DECIDED,
            State.REMITTED
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
                    assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                } else {
                    when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
                        .thenReturn(Optional.of(state));
                    assertTrue(homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase)
                        .contains(emailAddress));
                }
            }
        }
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_home_office() {

        when(userDetails.getRoles()).thenReturn(List.of(""));
        when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class))
            .thenReturn(Optional.of(State.FTPA_DECIDED));

        assertThatThrownBy(() -> homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("homeOffice email Address cannot be found");
    }

    @Test
    public void should_return_null_for_home_office_email_when_state_not_defined() {
        assertEquals(Collections.emptySet(), homeOfficeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase));
    }


    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeMakeAnApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(homeOfficeMakeAnApplicationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation = homeOfficeMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(homeOfficeMakeAnApplicationPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
