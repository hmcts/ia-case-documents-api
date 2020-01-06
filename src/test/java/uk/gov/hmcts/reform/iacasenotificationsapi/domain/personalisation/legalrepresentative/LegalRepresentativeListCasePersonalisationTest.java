package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeListCasePersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock StringProvider stringProvider;
    @Mock DateTimeExtractor dateTimeExtractor;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";

    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String legalRepEmailAddress = "legalRepEmailAddress@example.com";
    private String hearingCentreAddress = "some hearing centre address";

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

    private String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private LegalRepresentativeListCasePersonalisation legalRepresentativeListCasePersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateTime));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.of(requirementsVulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.of(requirementsMultimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.of(requirementsSingleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.of(requirementsInCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(requirementsOther));

        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedOther));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.of(hearingCentreAddress));
        when(dateTimeExtractor.extractHearingDate(hearingDateTime)).thenReturn(hearingDate);
        when(dateTimeExtractor.extractHearingTime(hearingDateTime)).thenReturn(hearingTime);

        legalRepresentativeListCasePersonalisation = new LegalRepresentativeListCasePersonalisation(
            templateId,
            stringProvider,
            dateTimeExtractor
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeListCasePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_LISTED_LEGAL_REPRESENTATIVE", legalRepresentativeListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeListCasePersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(ariaListingReference, personalisation.get("Listing Ref Number"));
        assertEquals(legalRepRefNumber, personalisation.get("Legal Rep Ref Number"));
        assertEquals(appellantGivenNames, personalisation.get("Appellant Given Names"));
        assertEquals(appellantFamilyName, personalisation.get("Appellant Family Name"));
        assertEquals(requirementsVulnerabilities, personalisation.get("Hearing Requirement Vulnerabilities"));
        assertEquals(requirementsMultimedia, personalisation.get("Hearing Requirement Multimedia"));
        assertEquals(requirementsSingleSexCourt, personalisation.get("Hearing Requirement Single Sex Court"));
        assertEquals(requirementsInCamera, personalisation.get("Hearing Requirement In Camera Court"));
        assertEquals(requirementsOther, personalisation.get("Hearing Requirement Other"));
        assertEquals(hearingDate, personalisation.get("Hearing Date"));
        assertEquals(hearingTime, personalisation.get("Hearing Time"));
        assertEquals(hearingCentreAddress, personalisation.get("Hearing Centre Address"));
    }

    @Test
    public void should_return_personalisation_when_co_records_hearing_response() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        Map<String, String> personalisation = legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("Appeal Ref Number"));
        assertEquals(ariaListingReference, personalisation.get("Listing Ref Number"));
        assertEquals(legalRepRefNumber, personalisation.get("Legal Rep Ref Number"));
        assertEquals(appellantGivenNames, personalisation.get("Appellant Given Names"));
        assertEquals(appellantFamilyName, personalisation.get("Appellant Family Name"));
        assertEquals(caseOfficerReviewedVulnerabilities, personalisation.get("Hearing Requirement Vulnerabilities"));
        assertEquals(caseOfficerReviewedMultimedia, personalisation.get("Hearing Requirement Multimedia"));
        assertEquals(caseOfficerReviewedSingleSexCourt, personalisation.get("Hearing Requirement Single Sex Court"));
        assertEquals(caseOfficerReviewedInCamera, personalisation.get("Hearing Requirement In Camera Court"));
        assertEquals(caseOfficerReviewedOther, personalisation.get("Hearing Requirement Other"));
        assertEquals(hearingDate, personalisation.get("Hearing Date"));
        assertEquals(hearingTime, personalisation.get("Hearing Time"));
        assertEquals(hearingCentreAddress, personalisation.get("Hearing Centre Address"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

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

        assertEquals("", personalisation.get("Appeal Ref Number"));
        assertEquals("", personalisation.get("Listing Ref Number"));
        assertEquals("", personalisation.get("Legal Rep Ref Number"));
        assertEquals("", personalisation.get("Appellant Given Names"));
        assertEquals("", personalisation.get("Appellant Family Name"));
        assertEquals("No special adjustments are being made to accommodate vulnerabilities", personalisation.get("Hearing Requirement Vulnerabilities"));
        assertEquals("No multimedia equipment is being provided", personalisation.get("Hearing Requirement Multimedia"));
        assertEquals("The court will not be single sex", personalisation.get("Hearing Requirement Single Sex Court"));
        assertEquals("The hearing will be held in public court", personalisation.get("Hearing Requirement In Camera Court"));
        assertEquals("No other adjustments are being made", personalisation.get("Hearing Requirement Other"));
        assertEquals(hearingDate, personalisation.get("Hearing Date"));
        assertEquals(hearingTime, personalisation.get("Hearing Time"));
        assertEquals(hearingCentreAddress, personalisation.get("Hearing Centre Address"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_is_empty() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("listCaseHearingCentre is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_centre_address_does_not_exist() {

        when(stringProvider.get("hearingCentreAddress", hearingCentre.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingCentreAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_hearing_date_time_does_not_exist() {

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeListCasePersonalisation.getPersonalisation(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("hearingDateTime is not present");
    }
}
