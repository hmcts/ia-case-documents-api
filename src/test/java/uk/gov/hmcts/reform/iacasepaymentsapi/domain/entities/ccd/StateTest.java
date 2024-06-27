package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class StateTest {
    private final List<String> expectedValues = List.of(
        "appealStarted",
        "appealStartedByAdmin",
        "appealSubmitted",
        "appealSubmittedOutOfTime",
        "pendingPayment",
        "awaitingRespondentEvidence",
        "caseBuilding",
        "caseUnderReview",
        "respondentReview",
        "submitHearingRequirements",
        "listing",
        "prepareForHearing",
        "finalBundling",
        "preHearing",
        "decision",
        "decided",
        "ended",
        "appealTakenOffline",
        "awaitingReasonsForAppeal",
        "reasonsForAppealSubmitted",
        "ftpaSubmitted",
        "ftpaDecided",
        "awaitingClarifyingQuestionsAnswers",
        "clarifyingQuestionsAnswersSubmitted",
        "awaitingCmaRequirements",
        "cmaRequirementsSubmitted",
        "cmaAdjustmentsAgreed",
        "cmaListed",
        "adjourned",
        "unknown"
    );

    @Test
    void has_correct_values() {
        assertEquals(expectedValues.size(), State.values().length);
        List<String> actualValues = Arrays.stream(State.values())
            .map(State::toString)
            .toList();
        assertEquals(expectedValues, actualValues);
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(30, State.values().length);
        assertEquals(30, expectedValues.size());
    }
}
