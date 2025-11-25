package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DetainedLegalRepRemovedIrcPrisonTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;

    private final String templateName = "TB-IAC-DEC-ENG-00024.docx";
    private final String internalCustomerServicesTelephone = "0300 123 1711";
    private final String internalCustomerServicesEmail = "IAC-DetainedTeam@justice.gov.uk";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String onlineCaseRefNumber = "1234-5678-9012-3456";
    private final String legalRepReferenceNumber = "LR123456";
    private final String appellantDateOfBirth = "1990-01-15";

    private DetainedLegalRepRemovedIrcPrisonTemplate template;

    @BeforeEach
    void setUp() {
        template = new DetainedLegalRepRemovedIrcPrisonTemplate(
                templateName,
                customerServicesProvider
        );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, template.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(appellantDateOfBirth)), templateFieldValues.get("dateOfBirth"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(appellantGivenNames, templateFieldValues.get("firstName"));
        assertEquals(appellantFamilyName, templateFieldValues.get("lastName"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineReferenceNumber"));
        assertEquals(Optional.of(legalRepReferenceNumber), templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        // Mock empty values for optional fields
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(appellantDateOfBirth)), templateFieldValues.get("dateOfBirth"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("firstName"));
        assertEquals("", templateFieldValues.get("lastName"));
        assertEquals(Optional.empty(), templateFieldValues.get("onlineReferenceNumber"));
        assertEquals(Optional.empty(), templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_null_values_from_providers() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(null);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(null);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        dataSetUpPersonalisation();
        dataSetUpNewFields();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(appellantDateOfBirth)), templateFieldValues.get("dateOfBirth"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(appellantGivenNames, templateFieldValues.get("firstName"));
        assertEquals(appellantFamilyName, templateFieldValues.get("lastName"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineReferenceNumber"));
        assertEquals(Optional.of(legalRepReferenceNumber), templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(null, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(null, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_blank_appellant_names() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        // Mock blank names
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("firstName"));
        assertEquals("", templateFieldValues.get("lastName"));
    }

    @Test
    void should_handle_blank_reference_numbers() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        // Mock blank reference numbers
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(""));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(""));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(Optional.of(""), templateFieldValues.get("onlineReferenceNumber"));
        assertEquals(Optional.of(""), templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_handle_all_fields_present_with_valid_data() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        // Verify all expected fields are present
        assertEquals(13, templateFieldValues.size());

        // Verify each field has the expected value
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(appellantDateOfBirth)), templateFieldValues.get("dateOfBirth"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(appellantGivenNames, templateFieldValues.get("firstName"));
        assertEquals(appellantFamilyName, templateFieldValues.get("lastName"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineReferenceNumber"));
        assertEquals(Optional.of(legalRepReferenceNumber), templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_throw_exception_when_appellant_date_of_birth_is_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> template.mapFieldValues(caseDetails))
                .hasMessage("appellantDateOfBirth is missing")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_exception_when_appellant_date_of_birth_is_invalid_format() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of("invalid-date"));

        assertThatThrownBy(() -> template.mapFieldValues(caseDetails))
                .isInstanceOf(Exception.class); // Could be DateTimeParseException or IllegalStateException
    }

    @Test
    void should_handle_different_date_of_birth_formats() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);

        String dateOfBirth = "2000-12-25";
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(dateOfBirth));

        dataSetUpPersonalisation();
        dataSetUpNewFields();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(dateOfBirth)), templateFieldValues.get("dateOfBirth"));
    }

    private void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        dataSetUpPersonalisation();
        dataSetUpNewFields();
    }

    private void dataSetUpPersonalisation() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
    }

    private void dataSetUpNewFields() {
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());
    }

    @Test
    void should_use_paper_j_reference_number_when_primary_is_empty() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        dataSetUpPersonalisation();

        // Mock primary field as empty and paper J field with value
        String paperJRefNumber = "PJ123456";
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(paperJRefNumber));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(Optional.of(paperJRefNumber), templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_use_paper_j_reference_number_when_primary_is_null() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        dataSetUpPersonalisation();

        // Mock primary field as null and paper J field with value
        String paperJRefNumber = "PJ789012";
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(paperJRefNumber));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(Optional.of(paperJRefNumber), templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_return_empty_when_both_reference_numbers_are_empty() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        dataSetUpPersonalisation();

        // Mock both fields as empty
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(Optional.empty(), templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_prefer_primary_reference_number_when_both_present() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(asylumCase.read(APPELLANT_DATE_OF_BIRTH, String.class)).thenReturn(Optional.of(appellantDateOfBirth));

        dataSetUpPersonalisation();

        // Mock both fields with values
        String paperJRefNumber = "PJ999999";
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(paperJRefNumber));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        // Should prefer the primary reference number
        assertEquals(Optional.of(legalRepReferenceNumber), templateFieldValues.get("legalRepReferenceNumber"));
    }
}
