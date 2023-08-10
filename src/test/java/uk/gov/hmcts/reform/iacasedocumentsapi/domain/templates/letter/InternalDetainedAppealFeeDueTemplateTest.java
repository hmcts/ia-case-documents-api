package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetainedAppealFeeDueTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
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
    private final int calenderDaysToPayAppealFee = 14;
    private final String feeAmountInPence = "14000";
    private final double feeAmountInPounds = Double.parseDouble(feeAmountInPence) / 100;
    private final double feeRemission = 0;
    private final double totalAmountToPay = (feeAmountInPounds - feeRemission);
    private final String deadlineDate = formatDateForNotificationAttachmentDocument(now.plusDays(calenderDaysToPayAppealFee));
    private InternalDetainedAppealFeeDueTemplate internalDetainedAppealFeeDueTemplate;

    @BeforeEach
    void setUp() {
        internalDetainedAppealFeeDueTemplate =
                new InternalDetainedAppealFeeDueTemplate(
                        templateName,
                        customerServicesProvider
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetainedAppealFeeDueTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalAdaCustomerServicesEmailAddress);

        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.of(feeAmountInPence));
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetainedAppealFeeDueTemplate.mapFieldValues(caseDetails);

        assertEquals(13, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(deadlineDate, templateFieldValues.get("deadlineDate"));

        assertEquals(feeAmountInPounds, templateFieldValues.get("feeBeforeRemission"));
        assertEquals(feeRemission, templateFieldValues.get("feeRemission"));
        assertEquals(totalAmountToPay, templateFieldValues.get("totalAmountToPay"));
    }

    @Test
    void should_throw_when_fee_amount_not_present() {
        dataSetUp();
        when(asylumCase.read(FEE_AMOUNT_GBP, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedAppealFeeDueTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Fee amount not found");

    }

    @Test
    void should_throw_when_remission_type_not_present() {
        dataSetUp();
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedAppealFeeDueTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Remission type not found");

    }

}
