package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.TIME_EXTENSIONS;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
public class TimeExtensionFinderTest {

    private final TimeExtensionFinder timeExtensionFinder = new TimeExtensionFinder();
    private final String reason = "the reason";
    private final String requestDate = "2020-03-01";
    private final State state = State.AWAITING_REASONS_FOR_APPEAL;
    private final TimeExtensionStatus status = TimeExtensionStatus.SUBMITTED;
    String decisionReason = "this is the decision reason";
    String decisionOutcomeDate = "2020-03-02";
    @Mock
    private AsylumCase asylumCase;

    @Test
    public void should_find_target_granted_time_extension() {

        TimeExtension timeExtension = new TimeExtension(
            requestDate,
            reason,
            State.AWAITING_REASONS_FOR_APPEAL,
            TimeExtensionStatus.GRANTED,
            emptyList(),
            TimeExtensionDecision.GRANTED,
            decisionReason,
            decisionOutcomeDate
        );

        when(asylumCase.read(TIME_EXTENSIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("1", timeExtension))));

        State currentState = State.AWAITING_REASONS_FOR_APPEAL;

        final IdValue<TimeExtension> timeExtensionIdValue =
            timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.GRANTED, asylumCase);

        assertNotNull(timeExtensionIdValue);
        assertNotNull(timeExtensionIdValue.getValue());
        assertEquals(requestDate, timeExtensionIdValue.getValue().getRequestDate());
        assertEquals(reason, timeExtensionIdValue.getValue().getReason());
        assertEquals(state, timeExtensionIdValue.getValue().getState());
        assertEquals(TimeExtensionStatus.GRANTED, timeExtensionIdValue.getValue().getStatus());
        assertEquals(emptyList(), timeExtensionIdValue.getValue().getEvidence());
        assertEquals(TimeExtensionDecision.GRANTED, timeExtensionIdValue.getValue().getDecision());
        assertEquals(decisionReason, timeExtensionIdValue.getValue().getDecisionReason());
        assertEquals(decisionOutcomeDate, timeExtensionIdValue.getValue().getDecisionOutcomeDate());
    }

    @Test
    public void should_find_target_refused_time_extension() {

        TimeExtension timeExtension = new TimeExtension(
            requestDate,
            reason,
            State.AWAITING_REASONS_FOR_APPEAL,
            TimeExtensionStatus.REFUSED,
            emptyList(),
            TimeExtensionDecision.REFUSED,
            decisionReason,
            decisionOutcomeDate
        );

        when(asylumCase.read(TIME_EXTENSIONS))
            .thenReturn(Optional.of(Collections.singletonList(new IdValue<>("1", timeExtension))));

        State currentState = State.AWAITING_REASONS_FOR_APPEAL;

        final IdValue<TimeExtension> timeExtensionIdValue =
            timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.REFUSED, asylumCase);

        assertNotNull(timeExtensionIdValue);
        assertNotNull(timeExtensionIdValue.getValue());
        assertEquals(requestDate, timeExtensionIdValue.getValue().getRequestDate());
        assertEquals(reason, timeExtensionIdValue.getValue().getReason());
        assertEquals(state, timeExtensionIdValue.getValue().getState());
        assertEquals(TimeExtensionStatus.REFUSED, timeExtensionIdValue.getValue().getStatus());
        assertEquals(emptyList(), timeExtensionIdValue.getValue().getEvidence());
        assertEquals(TimeExtensionDecision.REFUSED, timeExtensionIdValue.getValue().getDecision());
        assertEquals(decisionReason, timeExtensionIdValue.getValue().getDecisionReason());
        assertEquals(decisionOutcomeDate, timeExtensionIdValue.getValue().getDecisionOutcomeDate());
    }

    @Test
    public void should_find_target_refused_time_extension_when_multiple_time_extensions() {

        TimeExtension timeExtension2 = new TimeExtension(
            requestDate,
            reason,
            State.AWAITING_REASONS_FOR_APPEAL,
            TimeExtensionStatus.REFUSED,
            emptyList(),
            TimeExtensionDecision.REFUSED,
            decisionReason,
            decisionOutcomeDate
        );

        TimeExtension timeExtension1 = new TimeExtension(
            requestDate,
            reason,
            State.AWAITING_REASONS_FOR_APPEAL,
            TimeExtensionStatus.GRANTED,
            emptyList(),
            TimeExtensionDecision.GRANTED,
            decisionReason,
            decisionOutcomeDate
        );

        when(asylumCase.read(TIME_EXTENSIONS))
            .thenReturn(Optional.of(asList(new IdValue<>("1", timeExtension1), new IdValue<>("2", timeExtension2))));

        State currentState = State.AWAITING_REASONS_FOR_APPEAL;

        final IdValue<TimeExtension> timeExtensionIdValue =
            timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.REFUSED, asylumCase);

        assertNotNull(timeExtensionIdValue);
        assertNotNull(timeExtensionIdValue.getValue());
        assertEquals(requestDate, timeExtensionIdValue.getValue().getRequestDate());
        assertEquals(reason, timeExtensionIdValue.getValue().getReason());
        assertEquals(state, timeExtensionIdValue.getValue().getState());
        assertEquals(TimeExtensionStatus.REFUSED, timeExtensionIdValue.getValue().getStatus());
        assertEquals(emptyList(), timeExtensionIdValue.getValue().getEvidence());
        assertEquals(TimeExtensionDecision.REFUSED, timeExtensionIdValue.getValue().getDecision());
        assertEquals(decisionReason, timeExtensionIdValue.getValue().getDecisionReason());
        assertEquals(decisionOutcomeDate, timeExtensionIdValue.getValue().getDecisionOutcomeDate());
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        when(asylumCase.read(TIME_EXTENSIONS)).thenReturn(Optional.of(Collections.emptyList()));

        State currentState = State.AWAITING_REASONS_FOR_APPEAL;

        assertThatThrownBy(
            () -> timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.GRANTED, asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("No time extension found with state: 'awaitingReasonsForAppeal' and status: 'granted'");

        assertThatThrownBy(
            () -> timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.REFUSED, asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("No time extension found with state: 'awaitingReasonsForAppeal' and status: 'refused'");
    }

    @Test
    public void should_return_correct_text_description() {
        State currentState;
        String result;

        currentState = State.AWAITING_REASONS_FOR_APPEAL;
        result = timeExtensionFinder.findNextActionText(currentState);
        assertEquals(result, "tell us why you think the Home Office decision is wrong");


        currentState = State.AWAITING_CMA_REQUIREMENTS;
        result = timeExtensionFinder.findNextActionText(currentState);
        assertEquals(result, "tell us if you will need anything at your appointment");


        currentState = State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS;
        result = timeExtensionFinder.findNextActionText(currentState);
        assertEquals(result, "answer the Tribunal's questions");

    }

    @Test
    public void should_throw_exception_if_no_text_Description_found() {

        State currentState = State.APPEAL_STARTED;

        assertThatThrownBy(() -> timeExtensionFinder.findNextActionText(currentState))
            .isExactlyInstanceOf(IllegalArgumentException.class)
            .hasMessage("No next step text description value found for state: appealStarted");

    }
}
