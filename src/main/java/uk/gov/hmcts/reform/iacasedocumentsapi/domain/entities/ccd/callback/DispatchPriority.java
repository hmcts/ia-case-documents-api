package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback;

public enum DispatchPriority {

    PAYMENTS("payments"),
    EARLIEST("earliest"),
    EARLY("early"),
    LATE("late"),
    LATEST("latest");

    private final String id;

    DispatchPriority(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
