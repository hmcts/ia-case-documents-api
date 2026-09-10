package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class CmrRelistedHearingNoticeFieldMapperTest {

    @Mock private StringProvider stringProvider;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private HearingDetailsFinder hearingDetailsFinder;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;

    private final String appealReferenceNumber = "RP/11111/2020";
    private final String appellantGivenNames = "Talha";
    private final String appellantFamilyName = "Awan";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String legalRepReferenceNumber = "OUR-REF";
    private final String legalRepRefNumberPaperJ = "PAPER-J-REF";
    private final String ccdReferenceNumber = "1234-5678-9012-3456";
    private final String hearingDate = "2020-12-25T12:34:56";
    private final String oldHearingDate = "2020-11-24T09:15:00";
    private final String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private final String manchesterHearingCentreName = "Manchester";
    private final String taylorHouseHearingCentreName = "Taylor House";
    private final String ariaListingReference = "AA/12345/1234";

    private final String vulnerabilities = "Vulnerabilities";
    private final String multimedia = "Multimedia";
    private final String singleSexCourt = "Single sex court";
    private final String inCamera = "In camera";
    private final String otherHearingRequest = "Other";

    private final String caseOfficerReviewedVulnerabilities = "someCaseOfficerReviewedVulnerabilities";
    private final String caseOfficerReviewedMultimedia = "someCaseOfficerReviewedMultimedia";
    private final String caseOfficerReviewedSingleSexCourt = "someCaseOfficerReviewedSingleSexCourt";
    private final String caseOfficerReviewedInCamera = "someCaseOfficerReviewedInCamera";
    private final String caseOfficerReviewedOther = "someCaseOfficerReviewedOther";

    private final String expectedFormattedHearingDatePart = "25122020";
    private final String expectedFormattedHearingTimePart = "1234";
    private final String expectedFormattedOldHearingDatePart = "24112020";
    private final String expectedFormattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private final String manchesterRefDataAddress =
        "Manchester Tribunal Hearing Centre - Piccadilly Exchange, Piccadilly Plaza, M1 4AH";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "customer.services@example.com";

    private CmrRelistedHearingNoticeFieldMapper cmrRelistedHearingNoticeFieldMapper;

    @BeforeEach
    public void setUp() {

        cmrRelistedHearingNoticeFieldMapper =
            new CmrRelistedHearingNoticeFieldMapper(stringProvider, customerServicesProvider, hearingDetailsFinder);

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

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
        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of(manchesterHearingCentreName));
        when(stringProvider.get("hearingCentreName", "taylorHouse")).thenReturn(Optional.of(taylorHouseHearingCentreName));
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.of(vulnerabilities));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.of(multimedia));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.of(inCamera));
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.of(otherHearingRequest));

        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        when(asylumCaseBefore.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(asylumCaseBefore.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.of(oldHearingDate));

        when(customerServicesProvider.getCustomerServicesTelephone()).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getCustomerServicesEmail()).thenReturn(customerServicesEmail);
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(23, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(ccdReferenceNumber, templateFieldValues.get("ccdReferenceNumberForDisplay"));
        assertEquals(taylorHouseHearingCentreName, templateFieldValues.get("oldHearingCentre"));
        assertEquals(expectedFormattedOldHearingDatePart, templateFieldValues.get("oldHearingDate"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("hearingDate"));
        assertEquals(expectedFormattedHearingTimePart, templateFieldValues.get("hearingTime"));
        assertEquals(LocalDate.now().format(DateTimeFormatter.ofPattern("d MMM yyyy")),
            templateFieldValues.get("dateLetterSent"));
        assertEquals("Interim Hearing", templateFieldValues.get("hearingType"));
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
        assertNull(templateFieldValues.get("remoteHearing"));
    }

    @Test
    void should_use_current_case_data_for_old_hearing_details_when_no_case_details_before() {

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails);

        assertEquals(23, templateFieldValues.size());
        assertEquals(manchesterHearingCentreName, templateFieldValues.get("oldHearingCentre"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("oldHearingDate"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("hearingDate"));
    }

    @Test
    void should_use_paper_j_legal_rep_reference_when_legal_rep_reference_is_missing() {

        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.of(legalRepRefNumberPaperJ));

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(legalRepRefNumberPaperJ, templateFieldValues.get("legalRepReferenceNumber"));
    }

    @Test
    void should_map_case_officer_reviewed_requirements_when_submit_hearing_requirements_available() {

        when(asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(VULNERABILITIES_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedVulnerabilities));
        when(asylumCase.read(MULTIMEDIA_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedMultimedia));
        when(asylumCase.read(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedSingleSexCourt));
        when(asylumCase.read(IN_CAMERA_COURT_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedInCamera));
        when(asylumCase.read(ADDITIONAL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of(caseOfficerReviewedOther));

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(23, templateFieldValues.size());
        assertEquals(caseOfficerReviewedVulnerabilities, templateFieldValues.get("vulnerabilities"));
        assertEquals(caseOfficerReviewedMultimedia, templateFieldValues.get("multimedia"));
        assertEquals(caseOfficerReviewedSingleSexCourt, templateFieldValues.get("singleSexCourt"));
        assertEquals(caseOfficerReviewedInCamera, templateFieldValues.get("inCamera"));
        assertEquals(caseOfficerReviewedOther, templateFieldValues.get("otherHearingRequest"));
    }

    @Test
    void should_use_remote_hearing_centre() {

        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.REMOTE_HEARING));
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Remote hearing agreed"));

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(25, templateFieldValues.size());
        assertEquals("Remote hearing", templateFieldValues.get("remoteHearing"));
        assertEquals("Remote hearing agreed", templateFieldValues.get("remoteVideoCallTribunalResponse"));
    }

    @Test
    void should_use_remote_hearing_centre_when_ref_data_feature_is_on() {

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("agreed for remote hearing"));
        when(hearingDetailsFinder.getCmrHearingCentreAddress(asylumCase)).thenReturn(manchesterRefDataAddress);

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("Remote hearing", templateFieldValues.get("remoteHearing"));
        assertEquals("agreed for remote hearing", templateFieldValues.get("remoteVideoCallTribunalResponse"));
        assertEquals(manchesterRefDataAddress, templateFieldValues.get("hearingCentreAddress"));
    }

    @Test
    void should_use_virtual_hearing_centre() {

        when(asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE, String.class)).thenReturn(Optional.of("Remote hearing agreed"));

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals("IAC National (Virtual)", templateFieldValues.get("remoteHearing"));
        assertEquals("Remote hearing agreed", templateFieldValues.get("remoteVideoCallTribunalResponse"));
    }

    @Test
    void should_be_tolerant_of_missing_data() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REF_NUMBER_PAPER_J, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_VULNERABILITIES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_MULTIMEDIA, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_REQUIREMENTS_OTHER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCaseBefore.read(CMR_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues =
            cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(23, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("ccdReferenceNumberForDisplay"));
        assertEquals("", templateFieldValues.get("hearingDate"));
        assertEquals("", templateFieldValues.get("hearingTime"));
        assertEquals("", templateFieldValues.get("oldHearingDate"));
        assertEquals("", templateFieldValues.get("ariaListingReference"));
        assertEquals("No special adjustments are being made to accommodate vulnerabilities", templateFieldValues.get("vulnerabilities"));
        assertEquals("No multimedia equipment is being provided", templateFieldValues.get("multimedia"));
        assertEquals("The court will not be single sex", templateFieldValues.get("singleSexCourt"));
        assertEquals("The hearing will be held in public court", templateFieldValues.get("inCamera"));
        assertEquals("No other adjustments are being made", templateFieldValues.get("otherHearingRequest"));
        assertEquals(YesOrNo.NO, templateFieldValues.get("isIntegrated"));
        assertNull(templateFieldValues.get("remoteHearing"));
    }

    @Test
    void should_throw_if_cmr_hearing_centre_before_not_present() {

        when(asylumCaseBefore.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore))
            .hasMessage("listCaseHearingCentre (before) is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_hearing_centre_name_before_cannot_be_resolved() {

        when(stringProvider.get("hearingCentreName", "taylorHouse")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore))
            .hasMessage("listCaseHearingCentre (before) is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_throw_if_cmr_hearing_centre_not_present() {

        when(asylumCase.read(CMR_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cmrRelistedHearingNoticeFieldMapper.mapFieldValues(caseDetails, caseDetailsBefore))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
