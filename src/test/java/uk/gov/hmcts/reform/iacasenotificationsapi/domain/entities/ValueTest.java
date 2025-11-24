package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ValueTest {

    @Test
    public void should_hold_onto_values() {
        String testValue = "test";
        String testLabel = "label";

        Value value = new Value(testValue, testLabel);

        assertEquals(testValue, value.getCode());
        assertEquals(testLabel, value.getLabel());
    }
}
