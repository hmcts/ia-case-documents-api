package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeListCasePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    StringProvider stringProvider;
    @Mock
    DateTimeExtractor dateTimeExtractor;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    HearingDetailsFinder hearingDetailsFinder;

    private Long caseId = 12345L;
    private String nonAdaTemplateId = "nonAdaTemplateId";
    private String adaTemplateId = "adaTemplateId";
    private String outOfCountryTemplateId = "someOocTemplateId";
    private String listAssistHearingTemplateId = "listAssistHearingTemplateId";
    private String listAssistHearingOutOfCountryTemplateId = "listAssistHearingOocTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String legalRepEmailAddress = "legalRepEmailAddress@example.com";
    private String hearingCentreAddress = "some hearing centre address";

    //Remote hearing
    private HearingCentre remoteHearingCentre = HearingCentre.REMOTE_HEARING;
    private String remoteHearingCentreAddress = "remoteHearing Remote Hearing";

    private String hearingDateTime = "2019-08-27T14:25:15.000";
    private String hearingDate = "2019-08-27";
    private String hearingTime = "14:25";

    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String legalRepRefNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String requirementsVulnerabilities = "someRequirementsVulnerabilities";
    private String requirementsMultimedia = "someRequirementsMultimedia";
    private String requirementsSingleSexCourt = "someRequirementsSingleSexCourt";
    private String requirementsInCamera = "someRequirementsInCamera";
    private String requirementsOther = "someRequirementsOther";
    private String videoHearingSuitability = "someVideoHearingSuitability";

    private String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";

    private int appellantProvidingAppealArgumentDeadline = 13;
    private int respondentResponseToAppealArgumentDeadline = 15;

    private LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation;

    @BeforeEach
    void setup() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class))
            .thenReturn(Optional.of(requirementsVulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class))
            .thenReturn(Optional.of(requirementsMultimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class))
            .thenReturn(Optional.of(requirementsSingleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class))
            .thenReturn(Optional.of(requirementsInCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(requirementsOther));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class))
            .thenReturn(Optional.of(caseOfficerReviewedOther));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        when(hearingDetailsFinder.getHearingDateTime(asylumCase)).thenReturn(hearingDateTime);
        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(hearingCentre.toString());
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase)).thenReturn(hearingCentreAddress);
        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString()))
            .thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(hearingDetailsFinder.getHearingCentreLocation(asylumCase)).thenReturn(hearingCentreAddress);

        legalRepresentativeListCasePersonalisation = new LegalRepresentativeListCasePersonalisation(
            nonAdaTemplateId,
            adaTemplateId,
            outOfCountryTemplateId,
            listAssistHearingTemplateId,
            listAssistHearingOutOfCountryTemplateId,
            iaExUiFrontendUrl,
            appellantProvidingAppealArgumentDeadline,
            respondentResponseToAppealArgumentDeadline,
            dateTimeExtractor,
            customerServicesProvider,
            hearingDetailsFinder
        );
    }

    @Test
    void should_return_given_template_id() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(adaTemplateId, legalRepresentativeListCasePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(nonAdaTemplateId, legalRepresentativeListCasePersonalisation.getTemplateId(asylumCase));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        assertEquals(outOfCountryTemplateId, legalRepresentativeListCasePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_LISTED_LEGAL_REPRESENTATIVE",
            legalRepresentativeListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_given_email_address_from_asylum_case() {
        assertTrue(
            legalRepresentativeListCasePersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeListCasePersonalisation);
        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(requirementsVulnerabilities, personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals(requirementsMultimedia, personalisation.get("hearingRequirementMultimedia"));
        assertEquals(requirementsSingleSexCourt, personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals(requirementsInCamera, personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals(requirementsOther, personalisation.get("hearingRequirementOther"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_information_given_in_remote_hearing_case(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeListCasePersonalisation);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(remoteHearingCentre));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(hearingDetailsFinder.getHearingCentreAddress(asylumCase))
            .thenReturn(remoteHearingCentreAddress);

        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_co_records_hearing_response(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeListCasePersonalisation);
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(caseOfficerReviewedVulnerabilities, personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals(caseOfficerReviewedMultimedia, personalisation.get("hearingRequirementMultimedia"));
        assertEquals(caseOfficerReviewedSingleSexCourt, personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals(caseOfficerReviewedInCamera, personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals(caseOfficerReviewedOther, personalisation.get("hearingRequirementOther"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());

        if (isAda.equals(YesOrNo.YES)) {
            String appealArgumentDeadlineDate = LocalDate.now().plusDays(appellantProvidingAppealArgumentDeadline)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            assertEquals(appealArgumentDeadlineDate, personalisation.get("appellantProvidingAppealArgumentDeadline"));

            String respondentResponseDeadlineDate = LocalDate.now().plusDays(respondentResponseToAppealArgumentDeadline)
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            assertEquals(respondentResponseDeadlineDate, personalisation.get("respondentResponseToAppealArgumentDeadline"));
        } else {
            assertFalse(personalisation.containsKey("appellantProvidingAppealArgumentDeadline"));
            assertFalse(personalisation.containsKey("respondentResponseToAppealArgumentDeadline"));
        }
    }

    @Test
    public void should_return_personalisation_with_requested_and_granted_hearing_requirement() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));

        when(asylumCase.read(VULNERABILITIES_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Granted - Granted to vulnerabilities"));
        when(asylumCase.read(MULTIMEDIA_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Granted - Granted to multimedia"));
        when(asylumCase.read(SINGLE_SEX_COURT_DECISION_FOR_DISPLAY, String.class)).thenReturn(Optional.of("Refused - Refused to single sex court"));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(videoHearingSuitability));
        initializePrefixes(legalRepresentativeListCasePersonalisation);
        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals("Request Granted - Granted to vulnerabilities", personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals("Request Granted - Granted to multimedia", personalisation.get("hearingRequirementMultimedia"));
        assertEquals("Request Refused - Refused to single sex court", personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals("The hearing will be held in public court", personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals("No other adjustments are being made", personalisation.get("hearingRequirementOther"));
        assertEquals(videoHearingSuitability, personalisation.get("remoteVideoCallTribunalResponse"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeListCasePersonalisation);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals("", personalisation.get("legalRepReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals("No special adjustments are being made to accommodate vulnerabilities",
            personalisation.get("hearingRequirementVulnerabilities"));
        assertEquals("No multimedia equipment is being provided", personalisation.get("hearingRequirementMultimedia"));
        assertEquals("The court will not be single sex", personalisation.get("hearingRequirementSingleSexCourt"));
        assertEquals("The hearing will be held in public court",
            personalisation.get("hearingRequirementInCameraCourt"));
        assertEquals("No other adjustments are being made", personalisation.get("hearingRequirementOther"));
        assertEquals(hearingDate, personalisation.get("hearingDate"));
        assertEquals(hearingTime, personalisation.get("hearingTime"));
        assertEquals(hearingCentreAddress, personalisation.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }
}
