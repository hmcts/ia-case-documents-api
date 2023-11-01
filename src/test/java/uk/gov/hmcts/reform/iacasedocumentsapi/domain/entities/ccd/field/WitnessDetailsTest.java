package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals(witnessName, witnessDetails.getWitnessName());
        assertEquals(witnessFamilyName, witnessDetails.getWitnessFamilyName());
    }

    @Test
    public void should_build_witness_full_name() {
        assertEquals(witnessName + " " + witnessFamilyName, witnessDetails.buildWitnessFullName());
    }
}
