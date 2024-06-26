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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetainedTransferOutOfAdaTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    private InternalDetainedTransferOutOfAdaTemplate internalDetainedTransferOutOfAdaTemplate;
    private final String templateName = "TB-IAC-DEC-ENG-00014.docx";
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String transferOutOfAdaReason = "Test reason";
    private final String transferOutOfAdaMoreDetails = "Test reason more details";
    private final LocalDate now = LocalDate.now();
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";

    @BeforeEach
    void setUp() {
        internalDetainedTransferOutOfAdaTemplate =
            new InternalDetainedTransferOutOfAdaTemplate(
                templateName,
                customerServicesProvider
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, internalDetainedTransferOutOfAdaTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(TRANSFER_OUT_OF_ADA_REASON, String.class)).thenReturn(Optional.of(transferOutOfAdaReason));
        when(asylumCase.read(TRANSFER_OUT_OF_ADA_MORE_DETAILS, String.class)).thenReturn(Optional.of(transferOutOfAdaMoreDetails));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetainedTransferOutOfAdaTemplate.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(transferOutOfAdaReason, templateFieldValues.get("transferOutOfAdaReason"));
        assertEquals(transferOutOfAdaMoreDetails, templateFieldValues.get("transferOutOfAdaMoreDetails"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_throw_when_transfer_out_of_ada_reason_present() {
        dataSetUp();
        when(asylumCase.read(TRANSFER_OUT_OF_ADA_REASON, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedTransferOutOfAdaTemplate.mapFieldValues(caseDetails))
                .isExactlyInstanceOf(RequiredFieldMissingException.class)
                .hasMessage("Transfer out of ADA reason is not present");

    }
}
