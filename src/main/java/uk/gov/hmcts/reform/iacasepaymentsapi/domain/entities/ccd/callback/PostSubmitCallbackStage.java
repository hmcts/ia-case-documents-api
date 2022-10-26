package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback;

public enum PostSubmitCallbackStage {

    CCD_SUBMITTED("ccdSubmitted");

    private final String id;

    PostSubmitCallbackStage(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
