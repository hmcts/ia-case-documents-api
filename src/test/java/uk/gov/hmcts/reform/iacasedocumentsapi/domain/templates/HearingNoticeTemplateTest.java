package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

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
public class HearingNoticeTemplateTest {

    private final String templateName = "HEARING_NOTICE_TEMPLATE.docx";
    @Mock private StringProvider stringProvider;

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;


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

    private HearingNoticeTemplate hearingNoticeTemplate;

    @Before
    public void setUp() {

        hearingNoticeTemplate =
            new HearingNoticeTemplate(
                templateName,
                stringProvider
            );

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.of(legalRepReferenceNumber));

        when(asylumCase.getListCaseHearingDate()).thenReturn(Optional.of(hearingDate));

        when(asylumCase.getListCaseHearingCentre()).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.of(taylorHouseHearingCentreAddress));

        when(asylumCase.getListCaseRequirementsVulnerabilities()).thenReturn(Optional.of(vulnerabilities));
        when(asylumCase.getListCaseRequirementsMultimedia()).thenReturn(Optional.of(multimedia));
        when(asylumCase.getListCaseRequirementsSingleSexCourt()).thenReturn(Optional.of(singleSexCourt));
        when(asylumCase.getListCaseRequirementsInCameraCourt()).thenReturn(Optional.of(inCamera));
        when(asylumCase.getListCaseRequirementsOther()).thenReturn(Optional.of(otherHearingRequest));
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, hearingNoticeTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        Map<String, Object> templateFieldValues = hearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(14, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
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
    }

    @Test
    public void should_use_correct_hearing_centre_address() {

        when(asylumCase.getListCaseHearingCentre()).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        Map<String, Object> templateFieldValues = hearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(expectedFormattedTaylorHouseHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(asylumCase.getAppealReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantGivenNames()).thenReturn(Optional.empty());
        when(asylumCase.getAppellantFamilyName()).thenReturn(Optional.empty());
        when(asylumCase.getHomeOfficeReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getLegalRepReferenceNumber()).thenReturn(Optional.empty());
        when(asylumCase.getListCaseHearingDate()).thenReturn(Optional.empty());
        when(asylumCase.getListCaseRequirementsVulnerabilities()).thenReturn(Optional.empty());
        when(asylumCase.getListCaseRequirementsMultimedia()).thenReturn(Optional.empty());
        when(asylumCase.getListCaseRequirementsSingleSexCourt()).thenReturn(Optional.empty());
        when(asylumCase.getListCaseRequirementsInCameraCourt()).thenReturn(Optional.empty());
        when(asylumCase.getListCaseRequirementsOther()).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = hearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(14, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("hearingDate"));
        assertEquals("", templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals("", templateFieldValues.get("vulnerabilities"));
        assertEquals("", templateFieldValues.get("multimedia"));
        assertEquals("", templateFieldValues.get("singleSexCourt"));
        assertEquals("", templateFieldValues.get("inCamera"));
        assertEquals("", templateFieldValues.get("otherHearingRequest"));
    }

    @Test
    public void handling_should_throw_if_hearing_centre_not_present() {

        when(asylumCase.getListCaseHearingCentre()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingNoticeTemplate.mapFieldValues(caseDetails))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
