package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WitnessDetailsTest {

    private final String witnessName = "Some Name";
    private final String witnessFamilyName = "Some family Name";
    private WitnessDetails witnessDetails;

    @BeforeEach
    public void setUp() {
        witnessDetails = new WitnessDetails();
        witnessDetails.setWitnessName(witnessName);
        witnessDetails.setWitnessFamilyName(witnessFamilyName);
    }

    @Test
    public void should_hold_onto_values() {

        Assertions.assertEquals(witnessName, witnessDetails.getWitnessName());
        Assertions.assertEquals(witnessFamilyName, witnessDetails.getWitnessFamilyName());
    }
}
