package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedAppealRemissionPartiallyGrantedOrRefusedTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private SystemDateProvider systemDateProvider;

    private final String templateName = "Some template name";
    private final int daysAfterRemissionDecision = 14;
    private final String internalCustomerServicesTelephoneNumber = "0300 123 1711";
    private final String internalCustomerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final LocalDate now = LocalDate.now();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String amountLeftToPay = "7000";
    private final String remissionDecisionReason = "Partially granted due to insufficient evidence";
    private final String dueDate = "01 January 2025";

    private InternalDetainedAppealRemissionPartiallyGrantedOrRefusedTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedAppealRemissionPartiallyGrantedOrRefusedTemplate(
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

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of(amountLeftToPay));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(REMISSION_DECISION_REASON)).thenReturn(Optional.of(remissionDecisionReason));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase))
                .thenReturn(internalCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase))
                .thenReturn(internalCustomerServicesEmailAddress);
        when(systemDateProvider.dueDate(daysAfterRemissionDecision)).thenReturn(dueDate);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals("70.00", templateFieldValues.get("feeAmount"));
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(Optional.of(appealReferenceNumber), templateFieldValues.get("onlineCaseRefNumber"));
        assertEquals(Optional.of(remissionDecisionReason), templateFieldValues.get("remissionReason"));
        assertEquals(dueDate, templateFieldValues.get("daysAfterRemissionDecision"));
        assertEquals(internalCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_handle_empty_amount_left_to_pay() {
        dataSetUp();
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("feeAmount"));
    }

    @Test
    void should_handle_invalid_amount_left_to_pay() {
        dataSetUp();
        when(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class)).thenReturn(Optional.of("invalid"));

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(NumberFormatException.class, () ->
            template.mapFieldValues(caseDetails)
        )).hasMessageContaining("For input string: \"invalid\"");
    }

    @Test
    void should_include_appellant_personalisation() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertThat(templateFieldValues).containsKeys(
                "appealReferenceNumber",
                "homeOfficeReferenceNumber",
                "appellantGivenNames",
                "appellantFamilyName"
        );
    }

    @Test
    void should_include_customer_services_information() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(internalCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_include_remission_information() {
        dataSetUp();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(Optional.of(remissionDecisionReason), templateFieldValues.get("remissionReason"));
        assertEquals(dueDate, templateFieldValues.get("daysAfterRemissionDecision"));
    }

    @Test
    void should_throw_when_remission_decision_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class)).thenReturn(Optional.empty());

        assertThat(org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
            template.mapFieldValues(caseDetails)
        )).hasMessage("Remission decision is not set for case");
    }

}