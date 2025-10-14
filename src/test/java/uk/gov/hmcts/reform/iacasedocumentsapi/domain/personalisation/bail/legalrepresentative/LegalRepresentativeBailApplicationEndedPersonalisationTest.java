package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailApplicationEndedPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailApplicationEndedPersonalisationTest {

    @Mock
    BailCase bailCase;

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String legalRepEmailAddress = "legalRep@example.com";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";
    private final String outcomeOfApplication = "someOutcome";
    private final String reasonsOfOutcome = "someReasons";
    private final String endApplicationDate = "2022-05-13";

    private LegalRepresentativeBailApplicationEndedPersonalisation legalRepresentativeBailApplicationEndedPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when(bailCase.read(BailCaseFieldDefinition.END_APPLICATION_REASONS, String.class)).thenReturn(Optional.of(reasonsOfOutcome));
        when(bailCase.read(BailCaseFieldDefinition.END_APPLICATION_OUTCOME, String.class)).thenReturn(Optional.of(outcomeOfApplication));
        when(bailCase.read(BailCaseFieldDefinition.END_APPLICATION_DATE, String.class)).thenReturn(Optional.of(endApplicationDate));

        legalRepresentativeBailApplicationEndedPersonalisation = new LegalRepresentativeBailApplicationEndedPersonalisation(
            templateId
        );
    }


    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeBailApplicationEndedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_APPLICATION_ENDED_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailApplicationEndedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(legalRepresentativeBailApplicationEndedPersonalisation.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeBailApplicationEndedPersonalisation.getRecipientsList(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeBailApplicationEndedPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeBailApplicationEndedPersonalisation.getPersonalisation(bailCase);

        assertEquals(bailReferenceNumber, personalisation.get("bailReferenceNumber"));
        assertEquals(legalRepReference, personalisation.get("legalRepReference"));
        assertEquals(applicantGivenNames, personalisation.get("applicantGivenNames"));
        assertEquals(applicantFamilyName, personalisation.get("applicantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(outcomeOfApplication, personalisation.get("endApplicationOutcome"));
        assertEquals(reasonsOfOutcome, personalisation.get("endApplicationReasons"));
        assertEquals("13 May 2022", personalisation.get("endApplicationDate"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.END_APPLICATION_REASONS, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.END_APPLICATION_OUTCOME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.END_APPLICATION_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailApplicationEndedPersonalisation.getPersonalisation(bailCase);

        assertEquals("", personalisation.get("bailReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReference"));
        assertEquals("", personalisation.get("applicantGivenNames"));
        assertEquals("", personalisation.get("applicantFamilyName"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("endApplicationOutcome"));
        assertEquals("No reason given", personalisation.get("endApplicationReasons"));
        assertEquals("", personalisation.get("endApplicationDate"));
    }

}
