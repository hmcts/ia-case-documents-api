package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ClarifyingQuestionTest {
    private final String expectedQuestion = "question";
    private final ClarifyingQuestion clarifyingQuestion = new ClarifyingQuestion(expectedQuestion);

    @Test
    public void should_hold_onto_values() {
        assertEquals(expectedQuestion, clarifyingQuestion.getQuestion());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new ClarifyingQuestion(null))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}

