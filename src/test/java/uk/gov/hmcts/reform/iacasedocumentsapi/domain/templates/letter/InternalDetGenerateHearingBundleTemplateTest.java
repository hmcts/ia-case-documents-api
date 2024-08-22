package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetGenerateHearingBundleTemplateTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock private HearingDetailsFinder hearingDetailsFinder;

    private final String templateName = "TB-IAC-DEC-ENG-00007.docx";
    private final String internalAdaCustomerServicesTelephoneNumber = "0300 123 1711";
    private final String internalAdaCustomerServicesEmailAddress = "IAC-ADA-HW@justice.gov.uk";
    private final LocalDate now = LocalDate.now();
    private final String appealReferenceNumber = "RP/11111/2020";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String listCaseHearingCentre = HearingCentre.BIRMINGHAM.toString();
    private InternalDetGenerateHearingBundleTemplate internalDetGenerateHearingBundleTemplate;

    @BeforeEach
    void setUp() {
        internalDetGenerateHearingBundleTemplate =
                new InternalDetGenerateHearingBundleTemplate(
                        templateName,
                        customerServicesProvider,
                        hearingDetailsFinder
                );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalDetGenerateHearingBundleTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(internalAdaCustomerServicesTelephoneNumber);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(internalAdaCustomerServicesEmailAddress);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        String listCaseHearingDateTime = "2023-07-10T20:23:35";
        String listCaseHearingDate = "2023-07-10";

        dataSetUp();
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDateTime));

        when(hearingDetailsFinder.getHearingCentreName(asylumCase)).thenReturn(listCaseHearingCentre);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(internalAdaCustomerServicesEmailAddress);
        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(internalAdaCustomerServicesTelephoneNumber);

        Map<String, Object> templateFieldValues = internalDetGenerateHearingBundleTemplate.mapFieldValues(caseDetails);

        assertEquals(11, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(formatDateForNotificationAttachmentDocument(LocalDate.parse(listCaseHearingDate)), templateFieldValues.get("hearingDate"));
        assertEquals(LocalDateTime.parse(listCaseHearingDateTime).toLocalTime(), templateFieldValues.get("hearingTime"));
        assertEquals(listCaseHearingCentre, templateFieldValues.get("hearingLocation"));
    }

    @Test
    void should_map_case_data_to_template_field_values_when_list_case_hearing_date_not_present() {
        dataSetUp();
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = internalDetGenerateHearingBundleTemplate.mapFieldValues(caseDetails);

        assertEquals(8, templateFieldValues.size());
        assertEquals(internalAdaCustomerServicesTelephoneNumber, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(internalAdaCustomerServicesEmailAddress, templateFieldValues.get("customerServicesEmail"));

        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));

        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));

        assertFalse(templateFieldValues.containsKey("hearingDate"), "hearingDate is not populated");
        assertFalse(templateFieldValues.containsKey("hearingTime"), "hearingTime is not populated");
        assertFalse(templateFieldValues.containsKey("hearingLocation"), "hearingLocation is not populated");
    }
}
