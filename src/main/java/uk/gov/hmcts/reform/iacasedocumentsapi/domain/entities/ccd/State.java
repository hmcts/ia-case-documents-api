package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum State {

    APPEAL_STARTED("appealStarted"),
    APPEAL_STARTED_BY_ADMIN("appealStartedByAdmin"),
    APPEAL_SUBMITTED("appealSubmitted"),
    APPEAL_SUBMITTED_OUT_OF_TIME("appealSubmittedOutOfTime"),
    PENDING_PAYMENT("pendingPayment"),
    AWAITING_RESPONDENT_EVIDENCE("awaitingRespondentEvidence"),
    CASE_BUILDING("caseBuilding"),
    CASE_UNDER_REVIEW("caseUnderReview"),
    RESPONDENT_REVIEW("respondentReview"),
    SUBMIT_HEARING_REQUIREMENTS("submitHearingRequirements"),
    LISTING("listing"),
    PREPARE_FOR_HEARING("prepareForHearing"),
    FINAL_BUNDLING("finalBundling"),
    PRE_HEARING("preHearing"),
    HEARING_AND_OUTCOME("hearingAndOutcome"),
    DECISION("decision"),
    DECIDED("decided"),
    AWAITING_REASONS_FOR_APPEAL("awaitingReasonsForAppeal"),
    REASONS_FOR_APPEAL_SUBMITTED("reasonsForAppealSubmitted"),
    FTPA_SUBMITTED("ftpaSubmitted"),
    FTPA_DECIDED("ftpaDecided"),
    AWAITING_CLARIFYING_QUESTIONS_ANSWERS("awaitingClarifyingQuestionsAnswers"),
    CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED("clarifyingQuestionsAnswersSubmitted"),
    ENDED("ended"),
    AWAITING_CMA_REQUIREMENTS("awaitingCmaRequirements"),
    CMA_REQUIREMENTS_SUBMITTED("cmaRequirementsSubmitted"),
    CMA_ADJUSTMENTS_AGREED("cmaAdjustmentsAgreed"),
    CMA_LISTED("cmaListed"),
    ADJOURNED("adjourned"),
    APPEAL_TAKEN_OFFLINE("appealTakenOffline"),
    REMITTED("remitted"),
    DECISION_CONDITIONAL_BAIL("decisionConditionalBail"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    State(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

    private static final Map<String, State> lookup = new HashMap<>();

    static {
        for (State state : State.values()) {
            lookup.put(state.id, state);
        }
    }

    public static State get(String name) {
        return lookup.get(name);
    }
}
