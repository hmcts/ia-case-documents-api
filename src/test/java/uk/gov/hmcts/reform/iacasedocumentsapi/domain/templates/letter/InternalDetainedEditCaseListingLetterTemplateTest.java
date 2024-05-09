package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
public class InternalDetainedEditCaseListingLetterTemplateTest {

    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock private AsylumCase asylumCase;
    @Mock private AsylumCase asylumCaseBefore;
    @Mock private CustomerServicesProvider customerServicesProvider;
    @Mock private StringProvider stringProvider;
    private InternalDetainedEditCaseListingLetterTemplate internalDetainedEditCaseListingLetterTemplate;
    private final String templateName = "TB-IAC-HNO-ENG-00700.docx";
    private final String appealReferenceNumber = "HU/22222/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final LocalDate now = LocalDate.now();
    private final String listCaseHearingDate = "2023-08-14T14:30:00.000";
    private final String formattedListCaseHearingDate = "14082023";
    private String formattedListCaseHearingTime = "1430";
    private String manchesterHearingCentreAddress = "Manchester, 123 Somewhere, North";
    private String formattedManchesterHearingCentreAddress = "Manchester\n123 Somewhere\nNorth";
    private String hearingDateBefore = "2023-08-10T10:15:00";
    private final String formattedListCaseHearingDateBefore = "10082023";
    private String formattedListCaseHearingTimeBefore = "1015";
    private String expectedFormattedTaylorHouseHearingCentreName = "Taylor House";
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";

    @BeforeEach
    void setUp() {
        internalDetainedEditCaseListingLetterTemplate =
            new InternalDetainedEditCaseListingLetterTemplate(
                templateName,
                customerServicesProvider,
                stringProvider
            );
    }

    @Test
    void should_return_template_name() {

        assertEquals(templateName, internalDetainedEditCaseListingLetterTemplate.getName());
    }

    void dataSetUp() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.MANCHESTER));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(listCaseHearingDate));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateBefore));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.TAYLOR_HOUSE));
        when(stringProvider.get("hearingCentreName", "taylorHouse")).thenReturn(Optional.of(expectedFormattedTaylorHouseHearingCentreName));
        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of(manchesterHearingCentreAddress));
        when(customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(asylumCase)).thenReturn(customerServicesEmail);
    }

    @Test
    void should_map_case_data_to_template_field_values() {
        dataSetUp();

        Map<String, Object> templateFieldValues = internalDetainedEditCaseListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore);

        assertEquals(14, templateFieldValues.size());
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
        assertEquals(formattedListCaseHearingDate, templateFieldValues.get("hearingDate"));
        assertEquals(formattedListCaseHearingTime, templateFieldValues.get("hearingTime"));
        assertEquals(formattedListCaseHearingDateBefore, templateFieldValues.get("oldHearingDate"));
        assertEquals(formattedListCaseHearingTimeBefore, templateFieldValues.get("oldHearingTime"));
        assertEquals(expectedFormattedTaylorHouseHearingCentreName, templateFieldValues.get("oldHearingCentre"));
        assertEquals(formattedManchesterHearingCentreAddress, templateFieldValues.get("hearingCentreAddress"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
    }

    @Test
    void should_throw_when_list_case_hearing_centre_not_present() {
        dataSetUp();
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedEditCaseListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingCentre is not present");

    }

    @Test
    void should_throw_when_list_case_hearing_centre_before_not_present() {
        dataSetUp();
        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedEditCaseListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingCentre (before) is not present");

    }

    @Test
    public void should_throw_exception_when_hearing_centre_name_cannot_be_found() {
        dataSetUp();
        when(stringProvider.get("hearingCentreName", HearingCentre.TAYLOR_HOUSE.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedEditCaseListingLetterTemplate.mapFieldValues(caseDetails, caseDetailsBefore))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingCentre (before) is not present");
    }
}
