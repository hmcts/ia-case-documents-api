package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class Stf24WeeksUtilsTest {
    @Mock
    private AsylumCase asylumCase;


    @BeforeEach
    void setUp() {
        when(asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
    }

    @Test
    void should_return_true_for_case_review_when_event_complete_appellant_in_uk_and_stf24_status_yes() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));

        boolean result = Stf24WeeksUtils.isCaseReviewFor24WeeksCase(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW, asylumCase);

        assertTrue(result);
    }

    @Test
    void should_return_false_for_case_review_when_event_not_complete_or_conditions_not_met() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));

        boolean wrongEvent = Stf24WeeksUtils.isCaseReviewFor24WeeksCase(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.SUBMIT_APPEAL, asylumCase);
        assertFalse(wrongEvent);

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));
        boolean notInUk = Stf24WeeksUtils.isCaseReviewFor24WeeksCase(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW, asylumCase);
        assertFalse(notInUk);

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));
        boolean statusNo = Stf24WeeksUtils.isCaseReviewFor24WeeksCase(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW, asylumCase);
        assertFalse(statusNo);
    }

    @Test
    void should_return_true_when_stf24w_current_status_is_yes_and_false_when_missing_or_no() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES));
        assertTrue(Stf24WeeksUtils.hasStf24WeeksStatus(asylumCase));

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.of(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO));
        assertFalse(Stf24WeeksUtils.hasStf24WeeksStatus(asylumCase));

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.class))
                .thenReturn(Optional.empty());
        assertFalse(Stf24WeeksUtils.hasStf24WeeksStatus(asylumCase));
    }

    @Test
    void should_format_home_office_decision_date_to_d_MMM_yyyy_when_present_and_throw_on_missing_or_invalid() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("2023-06-02"));
        String formatted = Stf24WeeksUtils.getHomeOfficeDecisionDate(asylumCase);
        assertEquals("2 Jun 2023", formatted);

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.empty());
        assertThrows(java.time.format.DateTimeParseException.class, () -> Stf24WeeksUtils.getHomeOfficeDecisionDate(asylumCase));

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE, String.class))
                .thenReturn(Optional.of("invalid-date"));
        assertThrows(java.time.format.DateTimeParseException.class, () -> Stf24WeeksUtils.getHomeOfficeDecisionDate(asylumCase));
    }

    @Test
    void should_return_appellant_given_and_family_name_or_empty_when_missing() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.of("John"));
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.of("Smith"));
        assertEquals("John", Stf24WeeksUtils.getAppellantGivenName(asylumCase));
        assertEquals("Smith", Stf24WeeksUtils.getAppellantFamilyName(asylumCase));

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
                .thenReturn(Optional.empty());
        assertEquals("", Stf24WeeksUtils.getAppellantGivenName(asylumCase));
        assertEquals("", Stf24WeeksUtils.getAppellantFamilyName(asylumCase));
    }

    @Test
    void should_add_24_weeks_to_tribunal_received_date_when_present_and_use_submission_when_tribunal_missing() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-01-01"));
        String fromTribunal = Stf24WeeksUtils.populateStatutoryTimeFrame24wDate(asylumCase);
        assertEquals("18 Jun 2023", fromTribunal);

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of("2023-02-01"));
        String fromSubmission = Stf24WeeksUtils.populateStatutoryTimeFrame24wDate(asylumCase);
        assertEquals("19 Jul 2023", fromSubmission);
    }

    @Test
    void should_throw_DateTimeParseException_when_both_dates_missing_for_populateStatutoryTimeFrame24wDate() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.empty());
        assertThrows(java.time.format.DateTimeParseException.class, () -> Stf24WeeksUtils.populateStatutoryTimeFrame24wDate(asylumCase));
    }

    @Test
    void should_return_formatted_appeal_received_date_when_tribunal_or_submission_present_and_throw_when_missing() {
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.of("2023-03-05"));
        assertEquals("5 Mar 2023", Stf24WeeksUtils.getAppealReceivedDate(asylumCase));

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.of("2023-04-07"));
        assertEquals("7 Apr 2023", Stf24WeeksUtils.getAppealReceivedDate(asylumCase));

        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE, String.class))
                .thenReturn(Optional.empty());
        when(asylumCase.read(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE, String.class))
                .thenReturn(Optional.empty());
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> Stf24WeeksUtils.getAppealReceivedDate(asylumCase));
        assertEquals("Received date  is not present", ex.getMessage());
    }
}
