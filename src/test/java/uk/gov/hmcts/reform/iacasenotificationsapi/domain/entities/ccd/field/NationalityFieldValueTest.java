package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;

public class NationalityFieldValueTest {

    @Test
    public void should_store_correct_value() {
        NationalityFieldValue nationalityFieldValue = new NationalityFieldValue("American");
        assertEquals("American", nationalityFieldValue.getCode());
    }
}
