package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;

@EqualsAndHashCode
@ToString
@Getter
@Setter
public class ClarifyingQuestionAnswer {
    private String dateSent;
    private String dueDate;
    private String dateResponded;
    private String question;
    private String answer;
    private List<IdValue<Document>> supportingEvidence;

    private ClarifyingQuestionAnswer() {
    }

    public ClarifyingQuestionAnswer(String dateSent, String dueDate, String dateResponded, String question, String answer, List<IdValue<Document>> supportingEvidence) {
        this.dateSent = dateSent;
        this.dueDate = dueDate;
        this.dateResponded = dateResponded;
        this.question = question;
        this.answer = answer;
        this.supportingEvidence = supportingEvidence;
    }
}

