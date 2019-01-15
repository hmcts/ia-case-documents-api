package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

public enum HearingCentre {

    MANCHESTER("manchester"),
    TAYLOR_HOUSE("taylorHouse");

    private final String id;

    HearingCentre(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
