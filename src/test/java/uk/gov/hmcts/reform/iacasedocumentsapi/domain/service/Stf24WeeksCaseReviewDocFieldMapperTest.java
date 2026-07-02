package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class Stf24WeeksCaseReviewDocFieldMapperTest {
    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    private Stf24WeeksCaseReviewDocFieldMapper fieldMapper;

    @BeforeEach
    void setUp() {
        fieldMapper = new Stf24WeeksCaseReviewDocFieldMapper();
    }

    @Test
    void should_map_all_stf24_week_field_values_when_all_data_present() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(123L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Smith"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertTrue(result.containsKey("practiceDirection"));
        assertTrue(result.containsKey("14DaysFromDateOfDirection"));
        assertTrue(result.containsKey("42DaysFromDateOfDirection"));
        assertTrue(result.containsKey("56DaysFromDateOfDirection"));
        assertEquals("John", result.get("appellantGivenNames"));
        assertEquals("Smith", result.get("appellantFamilyName"));
        assertEquals("John Smith", result.get("appellantFullName"));
        assertEquals("18 Jun 2023", result.get("24WeeksDeadline"));
        assertEquals("1 Dec 2022", result.get("decisionSentDate"));
        assertTrue(result.containsKey("appealReceivedDate"));
    }

    @Test
    void should_handle_empty_appellant_names_and_trim_full_name() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(124L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertEquals("", result.get("appellantGivenNames"));
        assertEquals("", result.get("appellantFamilyName"));
        assertEquals("", result.get("appellantFullName"));
    }

    @Test
    void should_handle_only_given_names_and_trim_full_name() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(125L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertEquals("John", result.get("appellantFullName"));
    }

    @Test
    void should_handle_only_family_names_and_trim_full_name() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(126L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Smith"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        assertEquals("Smith", result.get("appellantFullName"));
    }

    @Test
    void should_calculate_correct_deadline_dates_from_today() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(127L);
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("Jane"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Doe"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2022-12-01"));

        Map<String, Object> result = fieldMapper.mapFieldValues(caseDetails);

        LocalDate today = LocalDate.now();
        String expected14Days = today.plusDays(14).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));
        String expected42Days = today.plusDays(42).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));
        String expected56Days = today.plusDays(56).format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy"));

        assertEquals(expected14Days, result.get("14DaysFromDateOfDirection"));
        assertEquals(expected42Days, result.get("42DaysFromDateOfDirection"));
        assertEquals(expected56Days, result.get("56DaysFromDateOfDirection"));
    }


}