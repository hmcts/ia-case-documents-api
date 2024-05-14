package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetMarkAsPaidLetterTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock private DateProvider dateProvider;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private final String templateName = "Some template name";
    private final String internalAdaCustomerServicesTelephoneNumber = "0300 123 1711";
    private final String internalAdaCustomerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final LocalDate now = LocalDate.now();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String feeAmountInPence = "14000";
    private final String totalAmountPaid = "8000";
    private final double feeAmountInPounds = Double.parseDouble(feeAmountInPence) / 100;
    private final double feeRemission = 80;
    private final double totalAmountPaidDouble = Double.parseDouble(totalAmountPaid) / 100;
    private InternalDetMarkAsPaidLetterTemplate internalDetMarkAsPaidLetterTemplate;

    @BeforeEach
    void setUp() {
        internalDetMarkAsPaidLetterTemplate =
                new InternalDetMarkAsPaidLetterTemplate(
                        templateName,
                        dateProvider,
                        customerServicesProvider
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetMarkAsPaidLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(dateProvider.now()).thenReturn(now);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(PAID_AMOUNT, String.class)).thenReturn(Optional.of(feeAmountInPence));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalAdaCustomerServicesEmailAddress);

        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountInPence));
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetMarkAsPaidLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));

        assertEquals(String.valueOf(feeAmountInPounds), templateFieldValues.get("feeBeforeRemission"));
        assertEquals("0.0", templateFieldValues.get("feeRemission"));
        assertEquals(String.valueOf(feeAmountInPounds), templateFieldValues.get("totalAmountToPay"));
    }

    @Test
    void should_map_case_data_to_template_field_values_with_remission_fee() {
        dataSetUp();
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(PAID_AMOUNT, String.class)).thenReturn(Optional.of(totalAmountPaid));
        when(asylumCase.read(AMOUNT_REMITTED, String.class)).thenReturn(Optional.of("8000"));
        Map<String, Object> templateFieldValues = internalDetMarkAsPaidLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(12, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));

        assertEquals(String.valueOf(feeAmountInPounds), templateFieldValues.get("feeBeforeRemission"));
        assertEquals(String.valueOf(feeRemission), templateFieldValues.get("feeRemission"));
        assertEquals(String.valueOf(totalAmountPaidDouble), templateFieldValues.get("totalAmountToPay"));
    }

    @Test
    void should_throw_when_paid_amount_not_present() {
        dataSetUp();
        when(asylumCase.read(PAID_AMOUNT, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetMarkAsPaidLetterTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Paid amount is not present");
    }
}
