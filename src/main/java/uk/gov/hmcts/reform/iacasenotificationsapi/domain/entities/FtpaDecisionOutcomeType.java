package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonValue;

public enum FtpaDecisionOutcomeType {

    FTPA_GRANTED("granted"),
    FTPA_PARTIALLY_GRANTED("partiallyGranted"),
    FTPA_REFUSED("refused"),
    FTPA_NOT_ADMITTED("notAdmitted"),
    FTPA_REHEARD35("reheardRule35"),
    FTPA_REHEARD32("reheardRule32"),
    FTPA_REMADE31("remadeRule31"),
    FTPA_REMADE32("remadeRule32"),
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
