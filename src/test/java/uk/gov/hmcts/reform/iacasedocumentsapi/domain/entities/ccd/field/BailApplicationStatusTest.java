package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BailApplicationStatusTest {

    @Test
    public void has_correct_values() {
        assertEquals("Yes", BailApplicationStatus.YES.toString());
        assertEquals("YesWithoutBailApplicationNumber", BailApplicationStatus.YES_WITHOUT_BAIL_APPLICATION_NUMBER.toString());
        assertEquals("No", BailApplicationStatus.NO.toString());
        assertEquals("NotSure", BailApplicationStatus.NOT_SURE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(4, BailApplicationStatus.values().length);
    }
}
