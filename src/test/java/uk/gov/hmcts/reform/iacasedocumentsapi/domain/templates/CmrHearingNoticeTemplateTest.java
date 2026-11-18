package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;

import java.time.LocalDate;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class CmrHearingNoticeTemplateTest {

    private final String templateName = "CMR_HEARING_NOTICE_TEMPLATE.docx";

    @Mock private StringProvider stringProvider;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private HearingDetailsFinder hearingDetailsFinder;

    private final String appealReferenceNumber = "RP/11111/2020";
    private final String appellantGivenNames = "Talha";
    private final String dateLetterSent = "25122020";
    private final String appellantFamilyName = "Awan";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String legalRepReferenceNumber = "OUR-REF";
    private final String legalRepReferenceNumberPaperJ = "PAPER-J-REF";
    private final String ccdReferenceNumber = "1234-5678-9012-3456";
    private final String hearingDate = "2020-12-25T12:34:56";
    private final String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private final String ariaListingReference = "AA/12345/1234";

    private final String vulnerabilities = "Vulnerabilities";
    private final String multimedia = "Multimedia";
    private final String singleSexCourt = "Single sex court";
    private final String inCamera = "In camera";
    private final String otherHearingRequest = "Other";

    private final String expectedFormattedHearingDatePart = "25122020";
    private final String expectedFormattedHearingTimePart = "1234";
    private final String expectedFormattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";

    private CmrHearingNoticeTemplate cmrHearingNoticeTemplate;

    @BeforeEach
    public void setUp() {

        cmrHearingNoticeTemplate =
            new CmrHearingNoticeTemplate(
                templateName,
                stringProvider,
                customerServicesProvider,
                hearingDetailsFinder
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, cmrHearingNoticeTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.of(ccdReferenceNumber));
        when(asylumCase.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));

        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.of(vulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.of(multimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.of(inCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(otherHearingRequest));

        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);

        Map<String, Object> templateFieldValues = cmrHearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(21, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(formatDateForRendering(LocalDate.now().toString(), DateTimeFormatter.ofPattern("d MMM yyyy")),
                templateFieldValues.get("dateLetterSent"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(ccdReferenceNumber, templateFieldValues.get("ccdReferenceNumberForDisplay"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("hearingDate"));
        assertEquals(expectedFormattedHearingTimePart, templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(ariaListingReference, templateFieldValues.get("ariaListingReference"));
        assertEquals(vulnerabilities, templateFieldValues.get("vulnerabilities"));
        assertEquals(multimedia, templateFieldValues.get("multimedia"));
        assertEquals(singleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(inCamera, templateFieldValues.get("inCamera"));
        assertEquals(otherHearingRequest, templateFieldValues.get("otherHearingRequest"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isIntegrated"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_fall_back_to_paper_j_legal_rep_reference_when_primary_is_missing() {

        legalRepReferenceDataSetUp();
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(legalRepReferenceNumberPaperJ));

        Map<String, Object> templateFieldValues = cmrHearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(legalRepReferenceNumberPaperJ, templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_prefer_primary_legal_rep_reference_over_paper_j() {

        legalRepReferenceDataSetUp();
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(legalRepReferenceNumberPaperJ));

        Map<String, Object> templateFieldValues = cmrHearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_be_empty_when_both_legal_rep_references_are_missing() {

        legalRepReferenceDataSetUp();
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = cmrHearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
    }

    private void legalRepReferenceDataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
    }
}
