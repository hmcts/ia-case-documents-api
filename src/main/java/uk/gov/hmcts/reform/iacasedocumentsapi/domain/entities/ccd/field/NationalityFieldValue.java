package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

public class NationalityFieldValue {
    private String code;

    public NationalityFieldValue(String code) {
        this.code = code;
    }

    private NationalityFieldValue() {
    }

    public String getCode() {
        return code;
    }
}
