package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class WitnessDetailsTest {

    private final String witnessName = "Some Name";
    private WitnessDetails witnessDetails;

    @Before
    public void setUp() {
        witnessDetails = new WitnessDetails();
        witnessDetails.setWitnessName(witnessName);
    }

    @Test
    public void should_hold_onto_values() {

        Assert.assertEquals(witnessName, witnessDetails.getWitnessName());
    }
}
