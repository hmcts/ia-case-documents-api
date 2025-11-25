package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION_REASON;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter.InternalDetainedLateRemissionRefusedTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedLateRemissionRefusedTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private SystemDateProvider systemDateProvider;

    private InternalDetainedLateRemissionRefusedTemplate template;

    private final String templateName = "TB-IAC-LET-ENG-00046.docx";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String remissionDecisionReason = "The appellant does not meet the eligibility criteria for fee remission.";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final LocalDate now = LocalDate.now();

    @BeforeEach
    void setUp() {
        template = new InternalDetainedLateRemissionRefusedTemplate(
                templateName,
                customerServicesProvider,
                systemDateProvider
        );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, template.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(remissionDecisionReason));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(Optional.of(remissionDecisionReason), templateFieldValues.get("remissionDecisionReason"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_missing_remission_decision_reason() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(Optional.empty(), templateFieldValues.get("remissionDecisionReason"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(customerServicesEmail);

        // Mock empty values for optional fields
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(Optional.empty(), templateFieldValues.get("remissionDecisionReason"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_null_customer_services() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(remissionDecisionReason));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(null);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(null);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(Optional.of(remissionDecisionReason), templateFieldValues.get("remissionDecisionReason"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(null, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(null, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_blank_appellant_names() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(customerServicesEmail);

        // Mock blank names
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(remissionDecisionReason));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
    }

    @Test
    void should_handle_blank_reference_numbers() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(customerServicesEmail);

        // Mock blank reference numbers
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(remissionDecisionReason));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
    }

    @Test
    void should_handle_long_remission_decision_reason() {
        dataSetUp();
        String longReason = "This is a very long remission decision reason that explains in detail why the appellant's application for fee remission has been refused. The decision takes into account various factors including financial circumstances, supporting evidence provided, and eligibility criteria as set out in the relevant regulations.";
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(longReason));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(Optional.of(longReason), templateFieldValues.get("remissionDecisionReason"));
    }

    @Test
    void should_handle_all_fields_present_with_valid_data() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(remissionDecisionReason));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        // Verify all expected fields are present
        assertEquals(9, templateFieldValues.size());

        // Verify each field has the expected value
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(Optional.of(remissionDecisionReason), templateFieldValues.get("remissionDecisionReason"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_special_characters_in_reason() {
        dataSetUp();
        String reasonWithSpecialChars = "The appellant's application contains special characters: £, €, &, <, >, \"quotes\", and 'apostrophes'.";
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(reasonWithSpecialChars));

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(9, templateFieldValues.size());
        assertEquals(Optional.of(reasonWithSpecialChars), templateFieldValues.get("remissionDecisionReason"));
    }

    private void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }
}
