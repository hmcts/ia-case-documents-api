package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ClarifyingQuestionTest {
    private final String expectedQuestion = "question";
    private final ClarifyingQuestion clarifyingQuestion = new ClarifyingQuestion(expectedQuestion);

    @Test
    public void pointlessTestToGetCodeCoverageUp() {
        assertEquals(clarifyingQuestion.getQuestion(), expectedQuestion);
    }
}

