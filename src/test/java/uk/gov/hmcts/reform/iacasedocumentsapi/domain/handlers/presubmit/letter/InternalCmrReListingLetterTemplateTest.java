package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.letter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalCmrReListingLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private StringProvider stringProvider;
    @Mock private DynamicList hearingChannelDynamicList;
    @Mock private Value hearingChannelValue;
    @Mock private DynamicList oldHearingChannelDynamicList;
    @Mock private Value oldHearingChannelValue;

    private InternalCmrReListingLetterTemplate internalCmrReListingLetterTemplate;

    private final String templateName = "TB-IAC-LET-ENG-Internal-Edit-Case-Listing.docx";
    private final String appealReferenceNumber = "HU/11111/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String cmrHearingDate = "2023-08-14T14:30:00.000";
    private final String formattedCmrHearingDate = "14 August 2023";
    private final String formattedCmrHearingTime =
        LocalDateTime.parse(cmrHearingDate).format(DateTimeFormatter.ofPattern("hh:mm a"));
    private final String oldCmrHearingDate = "2023-07-01T10:00:00.000";
    private final String formattedOldCmrHearingDate = "1 July 2023";
    private final String formattedOldCmrHearingTime =
        LocalDateTime.parse(oldCmrHearingDate).format(DateTimeFormatter.ofPattern("hh:mm a"));
    private final String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private final String formattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private final String taylorHouseHearingCentreName = "Taylor House";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final String hearingChannelLabel = "In person";
    private final String oldHearingChannelLabel = "Video";
    private final AddressUk appellantAddress =
        new AddressUk("123 Street", null, null, "London", null, "W1 1AA", null);

    @BeforeEach
    void setUp() {
        internalCmrReListingLetterTemplate =
            new InternalCmrReListingLetterTemplate(
                templateName,
                customerServicesProvider,
                stringProvider);
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, internalCmrReListingLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.of(cmrHearingDate));
        when(asylumCase.read(CMR_HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(hearingChannelDynamicList));
        when(hearingChannelDynamicList.getValue()).thenReturn(hearingChannelValue);
        when(hearingChannelValue.getLabel()).thenReturn(hearingChannelLabel);
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)).thenReturn(Optional.of(appellantAddress));

        when(asylumCaseBefore.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.of(oldCmrHearingDate));
        when(asylumCaseBefore.read(CMR_HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.of(oldHearingChannelDynamicList));
        when(oldHearingChannelDynamicList.getValue()).thenReturn(oldHearingChannelValue);
        when(oldHearingChannelValue.getLabel()).thenReturn(oldHearingChannelLabel);
        when(stringProvider.get("hearingCentreName", "taylorHouse")).thenReturn(Optional.of(taylorHouseHearingCentreName));
    }

    @Test
    void should_map_case_data_to_template_field_values_including_old_hearing_details() {
        dataSetUp();

        Map<String, Object> templateFieldValues =
            internalCmrReListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        assertEquals(formatDateForRendering(LocalDate.now().toString(), DateTimeFormatter.ofPattern("d MMMM yyyy")),
            templateFieldValues.get("dateLetterSent"));

        assertEquals(formattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(formattedCmrHearingDate, templateFieldValues.get("hearingDate"));
        assertEquals(formattedCmrHearingTime, templateFieldValues.get("hearingTime"));
        assertEquals(hearingChannelLabel, templateFieldValues.get("hearingChannel"));

        assertEquals(taylorHouseHearingCentreName, templateFieldValues.get("oldHearingCentre"));
        assertEquals(formattedOldCmrHearingDate, templateFieldValues.get("oldHearingDate"));
        assertEquals(formattedOldCmrHearingTime, templateFieldValues.get("oldHearingTime"));
        assertEquals(oldHearingChannelLabel, templateFieldValues.get("oldHearingChannel"));

        assertEquals("John Doe", templateFieldValues.get("address_line_1"));
        assertEquals("123 Street", templateFieldValues.get("address_line_2"));
        assertEquals("London", templateFieldValues.get("address_line_3"));
        assertEquals("W1 1AA", templateFieldValues.get("address_line_4"));
    }

    @Test
    void should_use_default_hearing_channel_when_missing() {
        dataSetUp();

        when(asylumCase.read(CMR_HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());
        when(asylumCaseBefore.read(CMR_HEARING_CHANNEL, DynamicList.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues =
            internalCmrReListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("Unknown", templateFieldValues.get("hearingChannel"));
        assertEquals("Unknown", templateFieldValues.get("oldHearingChannel"));
    }

    @Test
    void should_throw_when_cmr_hearing_centre_is_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
            () -> internalCmrReListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore));
    }

    @Test
    void should_throw_when_before_cmr_hearing_centre_is_missing() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCaseBefore.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
            () -> internalCmrReListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore));
    }
}
