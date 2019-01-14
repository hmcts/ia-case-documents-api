package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

public enum YesOrNo {

    NO("No"),
    YES("Yes");

    private final String id;

    YesOrNo(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
