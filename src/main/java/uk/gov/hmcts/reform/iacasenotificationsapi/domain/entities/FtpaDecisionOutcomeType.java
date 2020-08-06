package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FtpaDecisionOutcomeType {

    FTPA_GRANTED("granted"),
    FTPA_PARTIALLY_GRANTED("partiallyGranted"),
    FTPA_REFUSED("refused"),
    FTPA_NOT_ADMITTED("notAdmitted"),
    FTPA_REHEARD("reheard"),
    FTPA_ALLOWED("allowed"),
    FTPA_DISMISSED("dismissed");

    @JsonValue
    private final String value;

    FtpaDecisionOutcomeType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
