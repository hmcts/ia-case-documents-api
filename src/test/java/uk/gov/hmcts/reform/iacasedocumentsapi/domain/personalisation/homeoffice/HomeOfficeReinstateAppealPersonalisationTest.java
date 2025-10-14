package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATED_DECISION_MAKER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATE_APPEAL_DATE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REINSTATE_APPEAL_REASON;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.STATE_BEFORE_END_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.ADJOURNED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.DECISION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.FTPA_DECIDED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State.FTPA_SUBMITTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HomeOfficeReinstateAppealPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    AppealService appealService;
    @Mock
    EmailAddressFinder emailAddressFinder;


    private Long caseId = 12345L;
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";

    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String ariaListingReference = "someAriaListingReference";

    private String reinstateAppealDate = "2020-10-08";
    private String reinstateAppealReason = "someReason";
    private String reinstatedDecisionMaker = "someDecisionMaker";

    private String homeOfficeReinstateAppealBeforeListingTemplateId = "SomeTemplate";
    private String homeOfficeReinstateAppealAfterListingTemplateId = "SomeTemplate";

    private String apcPrivateBetaInboxHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private String lartHomeOfficeEmailAddress = "homeoffice-respondent@example.com";
    private String endAppealHomeOfficeEmailAddress = "ho-end-appeal@example.com";
    private String homeOfficeHearingCentreEmail = "ho-taylorhouse@example.com";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private HomeOfficeReinstateAppealPersonalisation homeOfficeReinstateAppealPersonalisation;

    @BeforeEach
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.of(reinstateAppealDate));
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.of(reinstateAppealReason));
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.of(reinstatedDecisionMaker));

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)))
            .thenReturn(homeOfficeHearingCentreEmail);

        homeOfficeReinstateAppealPersonalisation = new HomeOfficeReinstateAppealPersonalisation(
            homeOfficeReinstateAppealBeforeListingTemplateId,
            homeOfficeReinstateAppealAfterListingTemplateId,
            apcPrivateBetaInboxHomeOfficeEmailAddress,
            lartHomeOfficeEmailAddress,
            endAppealHomeOfficeEmailAddress,
            iaExUiFrontendUrl,
            customerServicesProvider,
            appealService,
            emailAddressFinder);
    }

    @Test
    public void should_return_given_email_address_when_appeal_before_listing_state() {

        List<State> apcEmail = newArrayList(
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.PENDING_PAYMENT,
            State.REASONS_FOR_APPEAL_SUBMITTED,
            State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED,
            State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS
        );

        List<State> lartEmail = newArrayList(
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.RESPONDENT_REVIEW,
            State.LISTING,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.AWAITING_REASONS_FOR_APPEAL
        );

        Map<String, List<State>> states = new HashMap<>();

        states.put(apcPrivateBetaInboxHomeOfficeEmailAddress, apcEmail);
        states.put(lartHomeOfficeEmailAddress, lartEmail);

        Set<String> emailAddresses = states.keySet();

        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.empty());
        for (String emailAddress : emailAddresses) {
            List<State> statesList = states.get(emailAddress);
            for (State state : statesList) {
                when(asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)).thenReturn(Optional.of(state));
                assertTrue(
                    homeOfficeReinstateAppealPersonalisation.getRecipientsList(asylumCase).contains(emailAddress));
            }
        }
    }

    @Test
    public void should_return_hearing_centre_homeOffice_email_address_when_appeal_is_listed() {
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.BIRMINGHAM));

        for (State state : State.values()) {
            when(asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)).thenReturn(Optional.of(state));
            Set<String> recipientsList = homeOfficeReinstateAppealPersonalisation.getRecipientsList(asylumCase);
            assertTrue(recipientsList.contains(homeOfficeHearingCentreEmail));
        }
    }

    @Test
    public void should_return_default_email_address_when_appeal_hearing_centre_is_not_found() {
        when(asylumCase.read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.empty());

        for (State state : List.of(ADJOURNED, FTPA_SUBMITTED, FTPA_DECIDED, DECISION)) {
            when(asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)).thenReturn(Optional.of(state));
            Set<String> recipientsList = homeOfficeReinstateAppealPersonalisation.getRecipientsList(asylumCase);
            assertTrue(recipientsList.contains(endAppealHomeOfficeEmailAddress));
        }
    }

    @Test
    public void should_return_given_template_id() {

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(homeOfficeReinstateAppealBeforeListingTemplateId,
            homeOfficeReinstateAppealPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(homeOfficeReinstateAppealAfterListingTemplateId,
            homeOfficeReinstateAppealPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_REINSTATE_APPEAL_HOME_OFFICE",
            homeOfficeReinstateAppealPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_null_on_personalisation_when_case_is_state_before_reinstate_is_not_present() {
        when(asylumCase.read(STATE_BEFORE_END_APPEAL, State.class)).thenReturn(Optional.empty());
        assertEquals(Collections.emptySet(), homeOfficeReinstateAppealPersonalisation.getRecipientsList(asylumCase));
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(homeOfficeReinstateAppealPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        Map<String, String> personalisation = homeOfficeReinstateAppealPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals("8 Oct 2020", personalisation.get("reinstateAppealDate"));
        assertEquals(reinstateAppealReason, personalisation.get("reinstateAppealReason"));
        assertEquals(reinstatedDecisionMaker, personalisation.get("reinstatedDecisionMaker"));
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

        initializePrefixes(homeOfficeReinstateAppealPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATE_APPEAL_REASON, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REINSTATED_DECISION_MAKER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeReinstateAppealPersonalisation.getPersonalisation(asylumCase);


        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("reinstateAppealDate"));
        assertEquals("No reason given", personalisation.get("reinstateAppealReason"));
        assertEquals("", personalisation.get("reinstatedDecisionMaker"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }


}
