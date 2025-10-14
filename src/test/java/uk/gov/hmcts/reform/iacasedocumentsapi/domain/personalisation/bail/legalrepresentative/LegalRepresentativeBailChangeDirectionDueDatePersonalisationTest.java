package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.bail.legalrepresentative.email.LegalRepresentativeBailChangeDirectionDueDatePersonalisation;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LegalRepresentativeBailChangeDirectionDueDatePersonalisationTest {

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
    private String sendDirectionList = "someSendDirectionList";
    private String dateOfCompliance = "2022-05-24";
    private String sendDirectionDescription = "someSendDirectionDescription";

    private LegalRepresentativeBailChangeDirectionDueDatePersonalisation legalRepresentativeChangeDirectionDueDatePersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_SENT, String.class)).thenReturn(Optional.of(sendDirectionList));
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.of(dateOfCompliance));
        when(bailCase.read(BAIL_DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.of(sendDirectionDescription));

        legalRepresentativeChangeDirectionDueDatePersonalisation = new LegalRepresentativeBailChangeDirectionDueDatePersonalisation(
            templateId
        );
    }


    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, legalRepresentativeChangeDirectionDueDatePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CHANGE_BAIL_DIRECTION_DUE_DATE_LEGAL_REPRESENTATIVE",
            legalRepresentativeChangeDirectionDueDatePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_bail_case() {
        assertTrue(legalRepresentativeChangeDirectionDueDatePersonalisation.getRecipientsList(bailCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_EMAIL, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeChangeDirectionDueDatePersonalisation.getRecipientsList(bailCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation((BailCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(SEND_DIRECTION_LIST, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BAIL_DIRECTION_EDIT_DATE_DUE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BAIL_DIRECTION_EDIT_EXPLANATION, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            legalRepresentativeChangeDirectionDueDatePersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

}
