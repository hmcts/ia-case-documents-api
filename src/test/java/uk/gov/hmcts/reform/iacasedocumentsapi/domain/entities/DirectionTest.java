package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

public class DirectionTest {

    private final String explanation = "Do the thing";
    private final Parties parties = Parties.RESPONDENT;
    private final String dateDue = "2018-12-31T12:34:56";
    private final String dateSent = "2018-12-25";
    private DirectionTag tag = DirectionTag.LEGAL_REPRESENTATIVE_REVIEW;
    private final List<IdValue<ClarifyingQuestion>> question = asList(new IdValue<>("1", new ClarifyingQuestion("question")));
    private final String previousDateDue = "2019-12-01";
    private final String previousDateSent = "2018-12-01T12:34:56";

    private PreviousDates previousDate = new PreviousDates(
        previousDateDue,
        previousDateSent
    );

    private List<IdValue<PreviousDates>> previousDates = newArrayList(new IdValue<>("1", previousDate));

    private Direction direction = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates
    );

    private Direction directionWithQuestion = new Direction(
        explanation,
        parties,
        dateDue,
        dateSent,
        tag,
        previousDates,
        question
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(explanation, direction.getExplanation());
        assertEquals(parties, direction.getParties());
        assertEquals(dateDue, direction.getDateDue());
        assertEquals(dateSent, direction.getDateSent());
        assertEquals(tag, direction.getTag());
        assertEquals(previousDates, direction.getPreviousDates());

        assertEquals(explanation, directionWithQuestion.getExplanation());
        assertEquals(parties, directionWithQuestion.getParties());
        assertEquals(dateDue, directionWithQuestion.getDateDue());
        assertEquals(dateSent, directionWithQuestion.getDateSent());
        assertEquals(tag, directionWithQuestion.getTag());
        assertEquals(previousDates, directionWithQuestion.getPreviousDates());
        assertEquals(question, directionWithQuestion.getClarifyingQuestions());
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> new Direction(null, parties, dateDue, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, null, dateDue, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, null, dateSent, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, null, tag, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new Direction(explanation, parties, dateDue, dateSent, null, previousDates))
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
