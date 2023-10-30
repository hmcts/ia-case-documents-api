package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WitnessDetails {

    private String witnessPartyId;
    private String witnessName;
    private String witnessFamilyName;

    public WitnessDetails() {
        // noop -- for deserializer
    }

    public WitnessDetails(String witnessName) {
        this.witnessName = witnessName;
    }

    public WitnessDetails(String witnessName, String witnessFamilyName) {
        this.witnessName = witnessName;
        this.witnessFamilyName = witnessFamilyName;
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

    public String buildWitnessFullName() {
        String givenNames = getWitnessName() == null ? " " : getWitnessName();
        String familyName = getWitnessFamilyName() == null ? " " : getWitnessFamilyName();

        return !(givenNames.isBlank() || familyName.isBlank()) ? givenNames + " " + familyName : givenNames;
    }
}
