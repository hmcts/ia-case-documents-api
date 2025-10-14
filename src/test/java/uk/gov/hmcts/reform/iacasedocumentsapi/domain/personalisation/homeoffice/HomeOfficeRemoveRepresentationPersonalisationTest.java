package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeRemoveRepresentationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;

    private final Long caseId = 12345L;
    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private String apcPrivateBetaInboxHomeOfficeEmailAddress = "homeoffice-apc@example.com";
    private String respondentReviewDirectionEmail = "homeoffice-respondent@example.com";
    private String homeOfficeHearingCentreEmail = "hc-taylorhouse@example.com";
    private String homeOfficeEmail = "ho-taylorhouse@example.com";
    private String legalRepName = "legalRepName";
    private String legalRepCompanyAddress = "legalRepCompanyAddress";
    private String legalRepEmailAddress = "legalRepEmailAddress";

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String postTown = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private AddressUk addressUk = new AddressUk(
        addressLine1,
        addressLine2,
        addressLine3,
        postTown,
        county,
        postCode,
        country
    );

    private HomeOfficeRemoveRepresentationPersonalisation
        homeOfficeRemoveRepresentationPersonalisation;

    @BeforeEach
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        when((emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeHearingCentreEmail);
        when((emailAddressFinder.getHomeOfficeEmailAddress(asylumCase))).thenReturn(homeOfficeEmail);

        homeOfficeRemoveRepresentationPersonalisation =
            new HomeOfficeRemoveRepresentationPersonalisation(
                beforeListingTemplateId,
                afterListingTemplateId,
                apcPrivateBetaInboxHomeOfficeEmailAddress,
                respondentReviewDirectionEmail,
                iaExUiFrontendUrl,
                appealService,
                emailAddressFinder,
                customerServicesProvider
            );
    }

    @Test
    void should_return_apc_email_address_for_given_states() {

        List<State> apcStates = newArrayList(
            State.APPEAL_STARTED,
            State.APPEAL_SUBMITTED,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.CASE_UNDER_REVIEW,
            State.PENDING_PAYMENT,
            State.ENDED
        );

        for (State state : apcStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(apcPrivateBetaInboxHomeOfficeEmailAddress));
        }
    }

    @Test
    void should_return_lart_email_address_for_given_states() {

        List<State> lartStates = newArrayList(
            State.RESPONDENT_REVIEW,
            State.SUBMIT_HEARING_REQUIREMENTS,
            State.LISTING
        );

        for (State state : lartStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(respondentReviewDirectionEmail));
        }
    }

    @Test
    void should_return_pou_email_address_for_given_states() {

        List<State> pouStates = newArrayList(
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

        for (State state : pouStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            when(appealService.isAppealListed(asylumCase)).thenReturn(true);
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeHearingCentreEmail));
        }

        for (State state : pouStates) {
            when(asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)).thenReturn(Optional.of(state));
            when(appealService.isAppealListed(asylumCase)).thenReturn(false);
            assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmail));
        }
    }

    @Test
    void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId, homeOfficeRemoveRepresentationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingTemplateId, homeOfficeRemoveRepresentationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REMOVE_REPRESENTATION_HOME_OFFICE",
            homeOfficeRemoveRepresentationPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(homeOfficeRemoveRepresentationPersonalisation);

        Map<String, String> personalisation =
            homeOfficeRemoveRepresentationPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_correctly_formatted_company_address() {

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("A, B, C, D, E, F, G", homeOfficeRemoveRepresentationPersonalisation.formatCompanyAddress(asylumCase));
    }

    @Test
    void should_return_correctly_formatted_company_address_for_missing_fields() {

        AddressUk addressUk = new AddressUk(
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("", homeOfficeRemoveRepresentationPersonalisation.formatCompanyAddress(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_case_listed(YesOrNo isAda) {
        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(homeOfficeRemoveRepresentationPersonalisation);

        Map<String, String> personalisation = homeOfficeRemoveRepresentationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(legalRepName, personalisation.get("legalRepresentativeName"));
        assertEquals(legalRepEmailAddress, personalisation.get("legalRepresentativeEmailAddress"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_and_case_not_listed(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(homeOfficeRemoveRepresentationPersonalisation);
        Map<String, String> personalisation = homeOfficeRemoveRepresentationPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(legalRepName, personalisation.get("legalRepresentativeName"));
        assertEquals(legalRepEmailAddress, personalisation.get("legalRepresentativeEmailAddress"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
