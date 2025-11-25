package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class ClarifyingQuestion {
    private String question;

    private ClarifyingQuestion() {
    }

    public ClarifyingQuestion(String question) {
        requireNonNull(question);
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
