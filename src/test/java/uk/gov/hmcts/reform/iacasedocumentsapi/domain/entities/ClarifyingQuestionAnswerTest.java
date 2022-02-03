package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ClarifyingQuestionAnswerTest {

    @Test
    void shouldCreateClarifyingQuestionAnswer() {
        ClarifyingQuestionAnswer clarifyingQuestionAnswer = new ClarifyingQuestionAnswer("dateSent", "dueDate", "dateResponded", "Question", "Answer", "directionId", Collections.emptyList());
        assertEquals("dateSent", clarifyingQuestionAnswer.getDateSent());
        assertEquals("dueDate", clarifyingQuestionAnswer.getDueDate());
        assertEquals("dateResponded", clarifyingQuestionAnswer.getDateResponded());
        assertEquals("Question", clarifyingQuestionAnswer.getQuestion());
        assertEquals("Answer", clarifyingQuestionAnswer.getAnswer());
        assertEquals("directionId", clarifyingQuestionAnswer.getDirectionId());
        assertEquals(Collections.emptyList(), clarifyingQuestionAnswer.getSupportingEvidence());

    }
}
