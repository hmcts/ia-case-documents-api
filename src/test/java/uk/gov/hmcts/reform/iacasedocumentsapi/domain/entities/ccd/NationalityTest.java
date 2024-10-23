package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class NationalityTest {

    @Test
    public void testToString() {
        assertEquals("Afghanistan", Nationality.AF.toString());
        assertEquals("United States of America", Nationality.US.toString());
        assertEquals("Zimbabwe", Nationality.ZW.toString());
    }

    @Test
    public void testJsonValue() {
        assertEquals("AF", Nationality.AF.name());
        assertEquals("US", Nationality.US.name());
        assertEquals("ZW", Nationality.ZW.name());
    }

    @Test
    public void testUnknown() {
        assertEquals("unknown", Nationality.UNKNOWN.toString());
    }

    @Test
    public void testAllValues() {
        for (Nationality nationality : Nationality.values()) {
            assertNotNull(nationality.toString());
            assertNotNull(nationality.name());
        }
    }
}
