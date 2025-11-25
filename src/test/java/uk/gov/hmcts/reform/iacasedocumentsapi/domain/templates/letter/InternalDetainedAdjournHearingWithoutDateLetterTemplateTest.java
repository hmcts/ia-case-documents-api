package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.letter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
public class InternalDetainedAdjournHearingWithoutDateLetterTemplateTest {
    @Mock
    Callback<AsylumCase> mockCallback;
    @Mock
    CaseDetails<AsylumCase> mockCaseDetails;
    @Mock
    CaseDetails<AsylumCase> mockCaseDetailsBefore;
    @Mock
    AsylumCase mockAsylumCase;
    @Mock
    AsylumCase mockAsylumCaseBefore;
    private static InternalDetainedAdjournHearingWithoutDateLetterTemplate internalDetainedAdjournHearingWithoutDateLetterTemplate;
    private final String templateName = "someTemplateId";
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    StringProvider stringProvider;
    private final String customerServicesTelephone = "0300 123 1711";
    private final String customerServicesEmail = "email@example.com";
    private final LocalDate now = LocalDate.now();
    private final String appealReferenceNumber = "HU/22222/2023";
    private final String homeOfficeReferenceNumber = "A1234567/001";
    private final String appellantGivenNames = "John";
    private final String appellantFamilyName = "Doe";
    private final String hearingDateBefore = "2023-08-10T10:15:00";
    private final String formattedListCaseHearingDateBefore = "10082023";
    private final String hearingWithoutDateReasons = "Sample reasons";

    @BeforeEach
    void setUp() {
        internalDetainedAdjournHearingWithoutDateLetterTemplate = new InternalDetainedAdjournHearingWithoutDateLetterTemplate(templateName, customerServicesProvider, stringProvider);
        when(mockCallback.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockCaseDetails.getCaseData()).thenReturn(mockAsylumCase);
        when(mockCallback.getCaseDetailsBefore()).thenReturn(Optional.of(mockCaseDetailsBefore));
        when(mockCaseDetailsBefore.getCaseData()).thenReturn(mockAsylumCaseBefore);
        when(customerServicesProvider.getInternalCustomerServicesTelephone(mockAsylumCase)).thenReturn(customerServicesTelephone);
        when(customerServicesProvider.getInternalCustomerServicesEmail(mockAsylumCase)).thenReturn(customerServicesEmail);
        when(mockAsylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(mockAsylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));
        when(mockAsylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(mockAsylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(mockAsylumCase.read(ADJOURN_HEARING_WITHOUT_DATE_REASONS, String.class)).thenReturn(Optional.of(hearingWithoutDateReasons));
        when(mockAsylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                .thenReturn(Optional.of(HearingCentre.BIRMINGHAM));
        when(mockAsylumCaseBefore.read(AsylumCaseDefinition.LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(hearingDateBefore));
        when(stringProvider.get("hearingCentreName", HearingCentre.BIRMINGHAM.toString())).thenReturn(Optional.of("Birmingham"));
    }

    @Test
    void testGetTemplateMethod() {
        assertEquals(templateName, internalDetainedAdjournHearingWithoutDateLetterTemplate.getName());
    }

    @Test
    void testMapFieldValues() {
        Map<String, Object> templateFieldValues = internalDetainedAdjournHearingWithoutDateLetterTemplate.mapFieldValues(mockCaseDetails, mockCaseDetailsBefore);
        assertEquals("Birmingham", templateFieldValues.get("oldHearingCentre"));
        assertEquals(formattedListCaseHearingDateBefore, templateFieldValues.get("oldHearingDate"));
        assertEquals(hearingWithoutDateReasons, templateFieldValues.get("adjournHearingWithoutDateReasons"));
        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(formatDateForNotificationAttachmentDocument(now), templateFieldValues.get("dateLetterSent"));
    }

    @Test
    void should_throw_when_list_case_hearing_centre_before_not_present() {
        when(mockAsylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedAdjournHearingWithoutDateLetterTemplate.mapFieldValues(mockCaseDetails, mockCaseDetailsBefore))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingCentre (before) is not present");

    }

    @Test
    public void should_throw_exception_when_hearing_centre_name_cannot_be_found() {
        when(stringProvider.get("hearingCentreName", HearingCentre.BIRMINGHAM.toString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedAdjournHearingWithoutDateLetterTemplate.mapFieldValues(mockCaseDetails, mockCaseDetailsBefore))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingCentre (before) is not present");
    }

    @Test
    void should_throw_when_list_case_hearing_date_before_not_present() {
        when(mockAsylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internalDetainedAdjournHearingWithoutDateLetterTemplate.mapFieldValues(mockCaseDetails, mockCaseDetailsBefore))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("listCaseHearingDate (before) is not present");

    }

}
