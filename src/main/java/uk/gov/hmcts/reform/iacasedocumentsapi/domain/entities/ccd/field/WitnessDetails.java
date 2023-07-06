package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

public class WitnessDetails {

    private String witnessName;
    private String witnessFamilyName;

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

    public String getWitnessFamilyName() {
        return witnessFamilyName;
    }

    public void setWitnessFamilyName(String witnessFamilyName) {
        this.witnessFamilyName = witnessFamilyName;
    }
}
