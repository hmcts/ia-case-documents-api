package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

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
