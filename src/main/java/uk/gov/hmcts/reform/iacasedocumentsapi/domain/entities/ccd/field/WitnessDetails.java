package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

public class WitnessDetails {

    private String witnessName;

    public WitnessDetails() {
        // noop -- for deserializer
    }

    public WitnessDetails(String witnessName) {
        this.witnessName = witnessName;
    }

    public String getWitnessName() {
        return witnessName;
    }

    public void setWitnessName(String witnessName) {
        this.witnessName = witnessName;
    }
}
