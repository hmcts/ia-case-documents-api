package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static java.util.Collections.emptyList;
import static junit.framework.TestCase.assertEquals;

import org.junit.Test;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;

public class TimeExtensionTest {

    private final String reason = "the reason";
    private final String requestDate = "2020-03-01";
    private final State state = State.AWAITING_REASONS_FOR_APPEAL;
    private final TimeExtensionStatus status = TimeExtensionStatus.SUBMITTED;

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

        String decisionReason = "this is the decision reason";
        String decisionOutcomeDate = "2020-03-02";
        TimeExtension timeExtensionRequest =
            new TimeExtension(
                requestDate,
                reason,
                State.AWAITING_REASONS_FOR_APPEAL,
                TimeExtensionStatus.GRANTED,
                emptyList(),
                TimeExtensionDecision.GRANTED,
                decisionReason,
                decisionOutcomeDate
            );

        assertEquals(requestDate, timeExtensionRequest.getRequestDate());
        assertEquals(reason, timeExtensionRequest.getReason());
        assertEquals(state, timeExtensionRequest.getState());
        assertEquals(TimeExtensionStatus.GRANTED, timeExtensionRequest.getStatus());
        assertEquals(emptyList(), timeExtensionRequest.getEvidence());
        assertEquals(TimeExtensionDecision.GRANTED, timeExtensionRequest.getDecision());
        assertEquals(decisionReason, timeExtensionRequest.getDecisionReason());
        assertEquals(decisionOutcomeDate, timeExtensionRequest.getDecisionOutcomeDate());
    }
}
