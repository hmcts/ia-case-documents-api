package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonCreator;

public class ClarifyingQuestion {
    private String question;

    private ClarifyingQuestion() {
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public ClarifyingQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
