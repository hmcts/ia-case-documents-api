package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class DetainedCmrReListingLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private StringProvider stringProvider;
    @Mock private DynamicList hearingChannelDynamicList;
    @Mock private DynamicList oldHearingChannelDynamicList;
    @Mock private Value hearingChannelValue;
    @Mock private Value oldHearingChannelValue;

    private DetainedCmrReListingLetterTemplate detainedCmrReListingLetterTemplate;

    private final String templateName = "TB-IAC-LET-ENG-00005.docx";
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String ccdReferenceNumber = "1234-5678-9012-3456";
    private final LocalDate now = LocalDate.now();
    private final String cmrHearingDate = "2023-08-14T14:30:00.000";
    private final String formattedCmrHearingDate = "14082023";
    private final String formattedCmrHearingTime = "1430";
    private final String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private final String formattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final String hearingChannelLabel = "In person";
    private final String oldHearingChannelLabel = "Video call";

    @BeforeEach
    void setUp() {
        detainedCmrReListingLetterTemplate =
            new DetainedCmrReListingLetterTemplate(
                templateName,
                customerServicesProvider,
                stringProvider
            );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, detainedCmrReListingLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.of(cmrHearingDate));
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(hearingChannelValue);
        when(hearingChannelValue.getLabel()).thenReturn(hearingChannelLabel);
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = detainedCmrReListingLetterTemplate.mapFieldValues(caseDetails);

        assertEquals(16, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(ccdReferenceNumber, templateFieldValues.get("ccdReferenceNumberForDisplay"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(formattedCmrHearingDate, templateFieldValues.get("hearingDate"));
        assertEquals(formattedCmrHearingTime, templateFieldValues.get("hearingTime"));
        assertEquals(formattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        // single-arg overload uses caseDetails as the "before" case, so both channels are the same
        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));
        assertEquals(hearingChannelLabel, templateFieldValues.get("oldHearingChannel"));
    }

    @Test
    void should_map_old_hearing_channel_from_before_case_details() {
        dataSetUp();

        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(asylumCaseBefore.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(oldHearingChannelDynamicList));
        when(asylumCaseBefore.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(oldHearingChannelDynamicList.getValue()).thenReturn(oldHearingChannelValue);
        when(oldHearingChannelValue.getLabel()).thenReturn(oldHearingChannelLabel);

        Map<String, Object> templateFieldValues =
            detainedCmrReListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));
        assertEquals(oldHearingChannelLabel, templateFieldValues.get("oldHearingChannel"));
    }

    @Test
    void should_use_default_hearing_channel_when_missing() {
        dataSetUp();

        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = detainedCmrReListingLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("Unknown", templateFieldValues.get("hearingChannel"));
        assertEquals("Unknown", templateFieldValues.get("oldHearingChannel"));
    }

    @Test
    void should_handle_missing_hearing_date() {
        dataSetUp();

        when(asylumCase.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = detainedCmrReListingLetterTemplate.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("hearingDate"));
        assertEquals("", templateFieldValues.get("hearingTime"));
    }

    @Test
    void should_throw_when_cmr_hearing_centre_is_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
            () -> detainedCmrReListingLetterTemplate.mapFieldValues(caseDetails));
    }
}
