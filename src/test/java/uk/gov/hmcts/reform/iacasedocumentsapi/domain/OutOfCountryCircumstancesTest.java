package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;


class OutOfCountryCircumstancesTest {

    @Test
    public void testEnumValues() {
        OutOfCountryCircumstances[] values = OutOfCountryCircumstances.values();
        assertEquals(3, values.length);
        assertTrue(containsValue(values, "entryClearanceDecision"));
        assertTrue(containsValue(values, "leaveUk"));
        assertTrue(containsValue(values, "none"));
    }

    private boolean containsValue(OutOfCountryCircumstances[] values, String value) {
        for (OutOfCountryCircumstances ooc : values) {
            if (ooc.toString().equals(value)) {
                return true;
            }
        }
        return false;
    }
}
