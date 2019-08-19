package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HearingNoticeEditedTemplateTest {

    private final String templateName = "HEARING_NOTICE_TEMPLATE.docx";

    @Mock private StringProvider stringProvider;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;

    private String appealReferenceNumber = "RP/11111/2020";
    private String appellantGivenNames = "Talha";
    private String appellantFamilyName = "Awan";
    private String homeOfficeReferenceNumber = "A1234567/001";
    private String legalRepReferenceNumber = "OUR-REF";
    private String hearingDate = "2020-12-25T12:34:56";

    private String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private String taylorHouseHearingCentreAddress = "London, 456 Somewhere, South";
    private String vulnerabilities = "Vulnerabilities";
    private String multimedia = "Multimedia";
    private String singleSexCourt = "Single sex court";
    private String inCamera = "In camera";
    private String otherHearingRequest = "Other";

    private String expectedFormattedHearingDatePart = "25122020";
    private String expectedFormattedHearingTimePart = "1234";
    private String expectedFormattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private String expectedFormattedTaylorHouseHearingCentreAddress = "London\n456 Somewhere\nSouth";

    private String ariaListingReference = "AA/12345/1234";

    private String hearingDateBefore = "2020-10-09T10:15:00";
    private String expectedFormattedHearingDatePartBefore = "09102020";
    private String expectedFormattedTaylorHouseHearingCentreName = "Taylor House";
    private String expectedFormattedManchesterHearingCentreName = "Manchester";

    private HearingNoticeEditedTemplate hearingNoticeEditedTemplate;

    @Before
    public void setUp() {

        hearingNoticeEditedTemplate =
            new HearingNoticeEditedTemplate(
                templateName,
                stringProvider
            );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.of(taylorHouseHearingCentreAddress));

        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.of(vulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.of(multimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.of(inCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(otherHearingRequest));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateBefore));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, hearingNoticeEditedTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        Map<String, Object> templateFieldValues = hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(17, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(expectedFormattedTaylorHouseHearingCentreName, templateFieldValues.get("oldHearingCentre"));
        assertEquals(expectedFormattedHearingDatePartBefore, templateFieldValues.get("oldHearingDate"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("hearingDate"));
        assertEquals(expectedFormattedHearingTimePart, templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(vulnerabilities, templateFieldValues.get("vulnerabilities"));
        assertEquals(multimedia, templateFieldValues.get("multimedia"));
        assertEquals(singleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(inCamera, templateFieldValues.get("inCamera"));
        assertEquals(otherHearingRequest, templateFieldValues.get("otherHearingRequest"));
        assertEquals(ariaListingReference, templateFieldValues.get("ariaListingReference"));
    }

    @Test
    public void should_use_correct_hearing_centre_address_for_taylor_house() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        Map<String, Object> templateFieldValues = hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(expectedFormattedTaylorHouseHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
    }

    @Test
    public void should_use_correct_hearing_centre_address_for_manchester() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));

        Map<String, Object> templateFieldValues = hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
    }

    @Test
    public void should_use_correct_previous_hearing_centre_name_for_taylor_house() {

        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        Map<String, Object> templateFieldValues = hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(expectedFormattedTaylorHouseHearingCentreName, templateFieldValues.get("oldHearingCentre"));
    }

    @Test
    public void should_use_correct_previous_hearing_centre_name_for_manchester() {

        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));

        Map<String, Object> templateFieldValues = hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(expectedFormattedManchesterHearingCentreName, templateFieldValues.get("oldHearingCentre"));
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(17, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("oldHearingDate"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("hearingDate"));
        assertEquals("", templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals("No special adjustments are being made to accommodate vulnerabilities", templateFieldValues.get("vulnerabilities"));
        assertEquals("No multimedia equipment is being provided", templateFieldValues.get("multimedia"));
        assertEquals("The court will not be single sex", templateFieldValues.get("singleSexCourt"));
        assertEquals("The hearing will be held in public court", templateFieldValues.get("inCamera"));
        assertEquals("No other adjustments are being made", templateFieldValues.get("otherHearingRequest"));
        assertEquals("", templateFieldValues.get("ariaListingReference"));
    }

    @Test
    public void handling_should_throw_if_previous_hearing_centre_not_present() {

        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingNoticeEditedTemplate.mapFieldValues(caseDetails, caseDetailsBefore))
            .hasMessage("listCaseHearingCentre (before) is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
