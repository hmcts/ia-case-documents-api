package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_APPLICANT_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedIrcPrisonUpdateFtpaDecisionLetterTemplateTest {

    private final String templateName = "TB-IAC-LET-ENG-Internal-detained-update-ftpa-decision-rule-31-irc-prison.docx";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "IAC-DetainedTeam@justice.gov.uk";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String appellantReason = "Correct decision error under rule 31";
    private final String respondentReason = "Correct decision error under rule 31";

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private InternalDetainedIrcPrisonUpdateFtpaDecisionLetterTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedIrcPrisonUpdateFtpaDecisionLetterTemplate(templateName, customerServicesProvider);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.APPELLANT));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, template.getName());
    }

    @Test
    void should_map_field_values_when_appellant_applied() {
        Map<String, Object> fieldValues = template.mapFieldValues(caseDetails);

        assertThat(fieldValues)
            .containsEntry("hmcts", "[userImage:hmcts.png]")
            .containsEntry("dateLetterSent", formatDateForNotificationAttachmentDocument(LocalDate.now()))
            .containsEntry("appealReferenceNumber", appealReferenceNumber)
            .containsEntry("homeOfficeReferenceNumber", homeOfficeReferenceNumber)
            .containsEntry("appellantGivenNames", appellantGivenNames)
            .containsEntry("appellantFamilyName", appellantFamilyName)
            .containsEntry("customerServicesTelephone", customerServicesTelephone)
            .containsEntry("customerServicesEmail", customerServicesEmail)
            .containsEntry("applicant", "your")
            .containsEntry("ftpaDisposedReason", appellantReason)
            .hasSize(10);
    }

    @Test
    void should_use_home_office_wording_and_reason_when_respondent_applied() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.of(ApplicantType.RESPONDENT));

        assertThat(template.mapFieldValues(caseDetails))
            .containsEntry("applicant", "the Home Office's")
            .containsEntry("ftpaDisposedReason", respondentReason);
    }

    @Test
    void should_throw_when_ftpa_applicant_type_missing() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> template.mapFieldValues(caseDetails))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaApplicantType is not present");
    }
}
