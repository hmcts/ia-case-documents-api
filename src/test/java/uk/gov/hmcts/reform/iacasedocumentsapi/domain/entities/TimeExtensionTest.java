package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;

public class TimeExtensionTest {

    private final String reason = "the reason";
    private final String requestDate = "2020-03-01";
    private final State state = State.AWAITING_REASONS_FOR_APPEAL;
    private final TimeExtensionStatus status = TimeExtensionStatus.SUBMITTED;

    private TimeExtension timeExtensionRequest;

    @BeforeEach
    void set_up() {

        timeExtensionRequest =
            new TimeExtension(
                requestDate,
                reason,
                State.AWAITING_REASONS_FOR_APPEAL,
                TimeExtensionStatus.GRANTED,
                emptyList(),
                TimeExtensionDecision.GRANTED,
                "this is the decision reason",
                "2020-03-02"
            );
    }

    @Test
    public void time_extension_request_should_hold_onto_values() {

        TimeExtension timeExtensionRequest =
            new TimeExtension(
                requestDate,
                reason,
                state,
                status,
                emptyList()
            );

        assertEquals(requestDate, timeExtensionRequest.getRequestDate());
        assertEquals(reason, timeExtensionRequest.getReason());
        assertEquals(state, timeExtensionRequest.getState());
        assertEquals(status, timeExtensionRequest.getStatus());
        assertEquals(emptyList(), timeExtensionRequest.getEvidence());
        assertEquals(null, timeExtensionRequest.getDecision());
        assertEquals(null, timeExtensionRequest.getDecisionReason());
        assertEquals(null, timeExtensionRequest.getDecisionOutcomeDate());
    }


    @Test
    public void time_extension_decision_should_hold_onto_values() {

        assertEquals(requestDate, timeExtensionRequest.getRequestDate());
        assertEquals(reason, timeExtensionRequest.getReason());
        assertEquals(state, timeExtensionRequest.getState());
        assertEquals(TimeExtensionStatus.GRANTED, timeExtensionRequest.getStatus());
        assertEquals(emptyList(), timeExtensionRequest.getEvidence());
        assertEquals(TimeExtensionDecision.GRANTED, timeExtensionRequest.getDecision());
        assertEquals("this is the decision reason", timeExtensionRequest.getDecisionReason());
        assertEquals("2020-03-02", timeExtensionRequest.getDecisionOutcomeDate());
    }

    @Test
    void should_compare_time_extension_objects_and_to_string() {

        TimeExtension timeExtensionAnother =
            new TimeExtension(
                requestDate,
                reason,
                State.AWAITING_REASONS_FOR_APPEAL,
                TimeExtensionStatus.GRANTED,
                emptyList(),
                TimeExtensionDecision.GRANTED,
                "this is the decision reason",
                "2020-03-02"
            );

        assertEquals(timeExtensionRequest, timeExtensionAnother);
        assertEquals(timeExtensionRequest.hashCode(), timeExtensionAnother.hashCode());
        assertThat(timeExtensionAnother.toString()).containsSequence(timeExtensionRequest.toString());
    }
}
