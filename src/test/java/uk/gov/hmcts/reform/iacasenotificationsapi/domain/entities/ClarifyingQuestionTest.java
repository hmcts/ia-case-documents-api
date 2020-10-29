package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ClarifyingQuestionTest {

    private final String question = "When did this happen?";

    private ClarifyingQuestion clarifyingQuestions = new ClarifyingQuestion(
        question
    );

    @Test
    public void should_hold_onto_values() {
        assertEquals(question, clarifyingQuestions.getQuestion());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new ClarifyingQuestion(null))
            .isExactlyInstanceOf(NullPointerException.class);

    }
}
