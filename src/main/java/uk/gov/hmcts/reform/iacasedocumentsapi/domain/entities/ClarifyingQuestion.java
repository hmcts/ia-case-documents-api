package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

public class ClarifyingQuestion {
    private String question;

    private ClarifyingQuestion() {
    }

    public ClarifyingQuestion(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }
}
