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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalDetainedOutOfTimeDecisionAllowedTemplateTest {

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private SystemDateProvider systemDateProvider;

    private final String templateName = "TB-IAC-DEC-ENG-00017.docx";
    private final int tribunalDocumentToSendTimeline = 4; // 4 weeks
    private final String internalCustomerServicesTelephone = "0300 123 1711";
    private final String internalCustomerServicesEmail = "IAC-DetainedTeam@justice.gov.uk";
    private final String fourWeeksAfterSubmissionDate = "15 Sept 2023";
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String onlineCaseRefNumber = "1234-5678-9012-3456";

    private InternalDetainedOutOfTimeDecisionAllowedTemplate template;

    @BeforeEach
    void setUp() {
        template = new InternalDetainedOutOfTimeDecisionAllowedTemplate(
                templateName,
                tribunalDocumentToSendTimeline,
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

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(fourWeeksAfterSubmissionDate, templateFieldValues.get("fourWeeksAfterSubmissionDate"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineCaseRefNumber"));
    }

    @Test
    void should_handle_missing_optional_fields() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(systemDateProvider.plusWeeks(tribunalDocumentToSendTimeline)).thenReturn(fourWeeksAfterSubmissionDate);

        // Mock empty values for optional fields
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalCustomerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(fourWeeksAfterSubmissionDate, templateFieldValues.get("fourWeeksAfterSubmissionDate"));
        assertEquals(internalCustomerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(Optional.empty(), templateFieldValues.get("onlineCaseRefNumber"));
    }

    @Test
    void should_handle_null_values_from_providers() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(null);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(null);
        when(systemDateProvider.plusWeeks(tribunalDocumentToSendTimeline)).thenReturn(null);

        dataSetUpPersonalisation();
        dataSetUpOnlineCaseRef();

        Map<String, Object> templateFieldValues = template.mapFieldValues(caseDetails);

        assertEquals(10, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(null, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.now()), templateFieldValues.get("dateLetterSent"));
        assertEquals(null, templateFieldValues.get("fourWeeksAfterSubmissionDate"));
        assertEquals(null, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(Optional.of(onlineCaseRefNumber), templateFieldValues.get("onlineCaseRefNumber"));
    }

    private void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalCustomerServicesEmail);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalCustomerServicesTelephone);
        when(systemDateProvider.plusWeeks(tribunalDocumentToSendTimeline)).thenReturn(fourWeeksAfterSubmissionDate);

        dataSetUpPersonalisation();
        dataSetUpOnlineCaseRef();
    }

    private void dataSetUpPersonalisation() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
    }

    private void dataSetUpOnlineCaseRef() {
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY)).thenReturn(Optional.of(onlineCaseRefNumber));
    }
}
