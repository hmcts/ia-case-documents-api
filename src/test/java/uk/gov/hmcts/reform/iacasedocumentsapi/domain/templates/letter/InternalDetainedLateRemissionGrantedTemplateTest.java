package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.AMOUNT_REMITTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedLateRemissionGrantedTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private SystemDateProvider systemDateProvider;

    private InternalDetainedLateRemissionGrantedTemplate template;

    private final String templateName = "TB-IAC-LET-ENG-00045.docx";
    private final int daysAfterRemissionDecision = 14;
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String onlineCaseRefNumber = "1234-5678-9012-3456";
    private final String amountRemitted = "15400";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final LocalDate now = LocalDate.now();
    private final String dueDate = "2023-12-15";

    @BeforeEach
    void setUp() {
        template = new InternalDetainedLateRemissionGrantedTemplate(
                templateName,
                daysAfterRemissionDecision,
                customerServicesProvider,
                systemDateProvider
        );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, template.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values_with_approved_remission() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals("154.00", templateFieldValues.get("refundAmount"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineCaseRefNumber"));
        assertEquals(dueDate, templateFieldValues.get("daysAfterRemissionDecision"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_map_case_data_to_template_field_values_with_partially_approved_remission() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals("", templateFieldValues.get("refundAmount")); // Empty for non-approved decisions
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineCaseRefNumber"));
        assertEquals(dueDate, templateFieldValues.get("daysAfterRemissionDecision"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_map_case_data_to_template_field_values_with_rejected_remission() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("refundAmount")); // Empty for non-approved decisions
    }

    @Test
    void should_handle_empty_amount_remitted() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.empty());
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("refundAmount"));
    }

    @Test
    void should_handle_null_amount_remitted() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(""));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("refundAmount"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(customerServicesEmail);

        // Mock empty values for optional fields
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.empty());
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals("", templateFieldValues.get("refundAmount"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals(Optional.empty(), templateFieldValues.get("onlineCaseRefNumber"));
        assertEquals(dueDate, templateFieldValues.get("daysAfterRemissionDecision"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_null_customer_services() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of(amountRemitted));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(null);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(null);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals(null, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(null, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_throw_exception_when_remission_decision_is_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> template.mapFieldValues(caseDetails))
                .hasMessage("Remission decision is not set for case")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_format_amount_correctly() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("5000"));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals("50.00", templateFieldValues.get("refundAmount"));
    }

    @Test
    void should_format_large_amount_correctly() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("123456"));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals("1234.56", templateFieldValues.get("refundAmount"));
    }

    @Test
    void should_format_zero_amount_correctly() {
        dataSetUp();
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
                .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("0"));
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals("0.00", templateFieldValues.get("refundAmount"));
    }

    private void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }
}
