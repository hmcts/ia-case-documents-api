package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

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
