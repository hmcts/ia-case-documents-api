package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FeeUpdateReason;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedManageFeeUpdateLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private SystemDateProvider systemDateProvider;

    private InternalDetainedManageFeeUpdateLetterTemplate internalDetainedManageFeeUpdateLetterTemplate;

    private final String templateName = "TB-IAC-LET-ENG-00808.docx";
    private final int afterManageFeeEvent = 14;

    private final String originalFeeTotal = "10000";
    private final String newFeeTotal = "15000";
    private final String formattedFeeDifference = "50.00";
    private final String appealReferenceNumber = "HU/12345/2023";
    private final String customerServicesTelephone = "0300 123 4567";
    private final String customerServicesEmail = "contact@service.gov.uk";
    private final String dueDate = "2023-09-15";

    @BeforeEach
    void setUp() {
        internalDetainedManageFeeUpdateLetterTemplate = new InternalDetainedManageFeeUpdateLetterTemplate(
                templateName,
                afterManageFeeEvent,
                customerServicesProvider,
                systemDateProvider
        );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetainedManageFeeUpdateLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(originalFeeTotal));
        when(asylumCase.read(NEW_FEE_AMOUNT, String.class)).thenReturn(Optional.of(newFeeTotal));
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.of(FeeUpdateReason.DECISION_TYPE_CHANGED));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appealReferenceNumber));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);

        when(systemDateProvider.dueDate(afterManageFeeEvent)).thenReturn(dueDate);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetainedManageFeeUpdateLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(14, templateFieldValues.size());
        assertEquals("100.00", templateFieldValues.get("originalFeeTotal"));
        assertEquals("150.00", templateFieldValues.get("newFeeTotal"));
        assertEquals(formattedFeeDifference, templateFieldValues.get("feeDifference"));
        assertEquals("Decision Type Changed", templateFieldValues.get("feeUpdateReasonSelected"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("onlineCaseRefNumber"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(dueDate, templateFieldValues.get("dueDate14Days"));
        assertEquals("20 Aug 2024", templateFieldValues.get("dateLetterSent"));
    }

    @Test
    void should_throw_when_fee_update_reason_not_present() {
        dataSetUp();
        when(asylumCase.read(FEE_UPDATE_REASON, FeeUpdateReason.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedManageFeeUpdateLetterTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("FeeUpdateReason is not present");
    }

    @Test
    void should_format_fee_update_reason_correctly() {
        String formattedReason = InternalDetainedManageFeeUpdateLetterTemplate.formatFeeUpdateReason(FeeUpdateReason.DECISION_TYPE_CHANGED);
        assertEquals("Decision Type Changed", formattedReason);
    }
}
