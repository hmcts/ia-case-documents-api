package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.CONDITIONAL_GRANT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.GRANTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType.REFUSED;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RecordDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisationTest {

    @Mock
    BailCase bailCase;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String bailReferenceNumber = "someReferenceNumber";
    private String legalRepReference = "someLegalRepReference";
    private String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private String applicantGivenNames = "someApplicantGivenNames";
    private String applicantFamilyName = "someApplicantFamilyName";

    private LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));

        legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation = new LegalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation(
            templateId
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_UPLOADED_SIGNED_DECISION_NOTICE_LEGAL_REPRESENTATIVE",
            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getRecipientsList(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

    @Test
    public void should_return_personalisation_with_decision_granted() {
        when(bailCase.read(BailCaseFieldDefinition.RECORD_DECISION_TYPE, RecordDecisionType.class))
            .thenReturn(Optional.of(GRANTED));

        Map<String, String> personalisation =
            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals("Granted", personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_with_decision_Refused() {
        when(bailCase.read(BailCaseFieldDefinition.RECORD_DECISION_TYPE, RecordDecisionType.class))
            .thenReturn(Optional.of(REFUSED));

        Map<String, String> personalisation =
            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals("Refused", personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_with_decision_conditionally_granted() {
        when(bailCase.read(BailCaseFieldDefinition.RECORD_DECISION_TYPE, RecordDecisionType.class))
            .thenReturn(Optional.of(CONDITIONAL_GRANT));

        Map<String, String> personalisation =
            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertEquals("Granted", personalisation.get("decision"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.RECORD_DECISION_TYPE, RecordDecisionType.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeBailSignedDecisionNoticeUploadedPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

}
