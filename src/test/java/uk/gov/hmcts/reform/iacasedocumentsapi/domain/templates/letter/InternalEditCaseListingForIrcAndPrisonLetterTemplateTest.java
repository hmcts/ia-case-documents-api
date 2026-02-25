package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.HearingNoticeUpdatedTemplateProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class InternalEditCaseListingForIrcAndPrisonLetterTemplateTest {

    private final String templateName = "INTERNAL_EDIT_CASE_LISTING_IRC_PRISON_TEMPLATE.docx";
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private HearingNoticeUpdatedTemplateProvider hearingNoticeUpdatedTemplateProvider;
    @Mock private DynamicList hearingChannelDynamicList;
    @Mock private DynamicList oldHearingChannelDynamicList;
    @Mock private Value hearingChannelValue;
    @Mock private Value oldHearingChannelValue;

    private String appealReferenceNumber = "RP/11111/2020";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String customerServicesEmail = "customer.services@example.com";
    private final String customerServicesTelephone = "0300 123 1711";
    private String hearingDate = "2024-12-20T12:34:56";
    private String oldHearingDate = "2024-11-15T10:30:00";
    private String hearingChannelLabel = "In person";
    private String oldHearingChannelLabel = "Video call";

    private InternalEditCaseListingForIrcAndPrisonLetterTemplate internalEditCaseListingForIrcAndPrisonLetterTemplate;

    @BeforeEach
    void setUp() {
        internalEditCaseListingForIrcAndPrisonLetterTemplate =
            new InternalEditCaseListingForIrcAndPrisonLetterTemplate(
                templateName,
                customerServicesProvider,
                hearingNoticeUpdatedTemplateProvider
            );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(asylumCaseBefore.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(oldHearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(hearingChannelValue);
        when(oldHearingChannelDynamicList.getValue()).thenReturn(oldHearingChannelValue);
        when(hearingChannelValue.getLabel()).thenReturn(hearingChannelLabel);
        when(oldHearingChannelValue.getLabel()).thenReturn(oldHearingChannelLabel);

        Map<String, Object> templateProviderFields = new HashMap<>();
        templateProviderFields.put("hmcts", "[userImage:hmcts.png]");
        templateProviderFields.put("appealReferenceNumber", appealReferenceNumber);
        templateProviderFields.put("appellantGivenNames", appellantGivenNames);
        templateProviderFields.put("appellantFamilyName", appellantFamilyName);
        templateProviderFields.put("homeOfficeReferenceNumber", homeOfficeReferenceNumber);
        when(hearingNoticeUpdatedTemplateProvider.mapFieldValues(caseDetails, caseDetailsBefore)).thenReturn(templateProviderFields);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalEditCaseListingForIrcAndPrisonLetterTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        Map<String, Object> templateFieldValues = internalEditCaseListingForIrcAndPrisonLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy")), templateFieldValues.get("dateLetterSent"));
        assertEquals("20 December 2024", templateFieldValues.get("hearingDate"));
        assertTrue("12:34 PM".equalsIgnoreCase(templateFieldValues.get("hearingTime").toString()));
        assertEquals("15 November 2024", templateFieldValues.get("oldHearingDate"));
        assertTrue("10:30 AM".equalsIgnoreCase(templateFieldValues.get("oldHearingTime").toString()));
        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));
        assertEquals(oldHearingChannelLabel, templateFieldValues.get("oldHearingChannel"));

        verify(hearingNoticeUpdatedTemplateProvider, times(1)).mapFieldValues(caseDetails, caseDetailsBefore);
        verify(customerServicesProvider, times(1)).getInternalCustomerServicesTelephone(asylumCase);
        verify(customerServicesProvider, times(1)).getInternalCustomerServicesEmail(asylumCase);
    }

    @Test
    void should_use_default_hearing_channel_when_missing() {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());
        when(asylumCaseBefore.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = internalEditCaseListingForIrcAndPrisonLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("Unknown", templateFieldValues.get("hearingChannel"));
        assertEquals("Unknown", templateFieldValues.get("oldHearingChannel"));
    }

    @Test
    void should_handle_missing_hearing_dates() {
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = internalEditCaseListingForIrcAndPrisonLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("", templateFieldValues.get("hearingDate"));
        assertEquals("", templateFieldValues.get("hearingTime"));
        assertEquals("", templateFieldValues.get("oldHearingDate"));
        assertEquals("", templateFieldValues.get("oldHearingTime"));
    }

    @Test
    void should_handle_null_hearing_channel_value() {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());
        when(asylumCaseBefore.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = internalEditCaseListingForIrcAndPrisonLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("Unknown", templateFieldValues.get("hearingChannel"));
        assertEquals("Unknown", templateFieldValues.get("oldHearingChannel"));
    }

    @Test
    void should_merge_template_provider_fields_with_specific_fields() {
        Map<String, Object> additionalFields = new HashMap<>();
        additionalFields.put("additionalField1", "value1");
        additionalFields.put("additionalField2", "value2");
        when(hearingNoticeUpdatedTemplateProvider.mapFieldValues(caseDetails, caseDetailsBefore)).thenReturn(additionalFields);

        Map<String, Object> templateFieldValues = internalEditCaseListingForIrcAndPrisonLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("value1", templateFieldValues.get("additionalField1"));
        assertEquals("value2", templateFieldValues.get("additionalField2"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));
        assertEquals(oldHearingChannelLabel, templateFieldValues.get("oldHearingChannel"));
    }
}