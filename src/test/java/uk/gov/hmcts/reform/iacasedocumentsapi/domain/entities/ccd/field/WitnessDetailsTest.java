package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    public void should_build_witness_full_name_if_names_present() {
        assertEquals(witnessName + " " + witnessFamilyName, witnessDetails.buildWitnessFullName());
    }

    @Test
    public void should_build_witness_full_name_if_names_not_present() {
        WitnessDetails witnessDetails2 = new WitnessDetails();
        assertEquals(" ", witnessDetails2.buildWitnessFullName());
    }

    @Test
    public void should_initialize_object() {
        WitnessDetails witnessDetails3 = new WitnessDetails(witnessName);
        assertEquals(witnessName, witnessDetails3.getWitnessName());
        assertNull(witnessDetails3.getWitnessFamilyName());

        WitnessDetails witnessDetails4 = new WitnessDetails(witnessName, witnessFamilyName);
        assertEquals(witnessName, witnessDetails4.getWitnessName());
        assertEquals(witnessFamilyName, witnessDetails4.getWitnessFamilyName());
    }
}
