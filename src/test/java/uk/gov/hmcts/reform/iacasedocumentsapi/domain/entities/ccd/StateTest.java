package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StateTest {

    @Test
    public void has_correct_values() {
        assertEquals("appealStarted", State.APPEAL_STARTED.toString());
        assertEquals("appealStartedByAdmin", State.APPEAL_STARTED_BY_ADMIN.toString());
        assertEquals("appealSubmitted", State.APPEAL_SUBMITTED.toString());
        assertEquals("appealSubmittedOutOfTime", State.APPEAL_SUBMITTED_OUT_OF_TIME.toString());
        assertEquals("awaitingRespondentEvidence", State.AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("caseBuilding", State.CASE_BUILDING.toString());
        assertEquals("caseUnderReview", State.CASE_UNDER_REVIEW.toString());
        assertEquals("respondentReview", State.RESPONDENT_REVIEW.toString());
        assertEquals("submitHearingRequirements", State.SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("listing", State.LISTING.toString());
        assertEquals("prepareForHearing", State.PREPARE_FOR_HEARING.toString());
        assertEquals("finalBundling", State.FINAL_BUNDLING.toString());
        assertEquals("preHearing", State.PRE_HEARING.toString());
        assertEquals("hearingAndOutcome", State.HEARING_AND_OUTCOME.toString());
        assertEquals("decision", State.DECISION.toString());
        assertEquals("decided", State.DECIDED.toString());
        assertEquals("ended", State.ENDED.toString());
        assertEquals("unknown", State.UNKNOWN.toString());
        assertEquals("awaitingCmaRequirements", State.AWAITING_CMA_REQUIREMENTS.toString());
        assertEquals("cmaAdjustmentsAgreed", State.CMA_ADJUSTMENTS_AGREED.toString());
        assertEquals("pendingPayment", State.PENDING_PAYMENT.toString());
        assertEquals("adjourned", State.ADJOURNED.toString());
        assertEquals("ftpaSubmitted", State.FTPA_SUBMITTED.toString());
        assertEquals("decisionConditionalBail", State.DECISION_CONDITIONAL_BAIL.toString());

    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(25, State.values().length);
    }
}
