package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

public class ClarifyingQuestionTest {
    private final String expectedQuestion = "question";
    private final ClarifyingQuestion clarifyingQuestion = new ClarifyingQuestion(expectedQuestion);

    @Test
    public void pointlessTestToGetCodeCoverageUp() {
        assertThat(clarifyingQuestion.getQuestion(), is(expectedQuestion));
    }
}
