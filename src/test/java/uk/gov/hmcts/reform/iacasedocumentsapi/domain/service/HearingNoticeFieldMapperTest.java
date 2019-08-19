package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.HearingNoticeTemplate;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class HearingNoticeFieldMapperTest {

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
    private String ariaListingReference = "AA/12345/1234";

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

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDate));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(stringProvider.get("hearingCentreAddress", "taylorHouse")).thenReturn(Optional.of(taylorHouseHearingCentreAddress));

        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, hearingNoticeTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        Map<String, Object> templateFieldValues = hearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(15, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(legalRepReferenceNumber, templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals(expectedFormattedHearingDatePart, templateFieldValues.get("hearingDate"));
        assertEquals(expectedFormattedHearingTimePart, templateFieldValues.get("hearingTime"));
        assertEquals(expectedFormattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(ariaListingReference, templateFieldValues.get("ariaListingReference"));
    }

    @Test
    public void should_use_correct_hearing_centre_address() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));

        Map<String, Object> templateFieldValues = hearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(expectedFormattedTaylorHouseHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());

        Map<String, Object> templateFieldValues = hearingNoticeTemplate.mapFieldValues(caseDetails);

        assertEquals(15, templateFieldValues.size());
        assertEquals("[userImage:hmcts.png]", templateFieldValues.get("hmcts"));
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals("", templateFieldValues.get("legalRepReferenceNumber"));
        assertEquals("", templateFieldValues.get("ariaListingReference"));
    }

    @Test
    public void handling_should_throw_if_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> hearingNoticeTemplate.mapFieldValues(caseDetails))
            .hasMessage("listCaseHearingCentre is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
