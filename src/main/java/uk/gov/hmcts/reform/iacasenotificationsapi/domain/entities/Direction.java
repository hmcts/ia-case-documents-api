package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.List;
import lombok.AllArgsConstructor;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@AllArgsConstructor
public class Direction {

    private String explanation;
    private Parties parties;
    private String dateDue;
    private String dateSent;
    private DirectionTag tag;
    private List<IdValue<PreviousDates>> previousDates;
    private List<IdValue<ClarifyingQuestion>> clarifyingQuestions;
    private String uniqueId;
    private String directionType;

    private Direction() {
        // noop -- for deserializer
    }

    public String getExplanation() {
        requireNonNull(explanation);
        return explanation;
    }

    public Parties getParties() {
        requireNonNull(parties);
        return parties;
    }

    public String getDateDue() {
        requireNonNull(dateDue);
        return dateDue;
    }

    public String getDateSent() {
        requireNonNull(dateSent);
        return dateSent;
    }

    public DirectionTag getTag() {
        requireNonNull(tag);
        return tag;
    }

    public List<IdValue<PreviousDates>> getPreviousDates() {
        return previousDates;
    }

    public List<IdValue<ClarifyingQuestion>> getClarifyingQuestions() {
        return clarifyingQuestions;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public String getDirectionType() {
        return directionType;
    }

}
