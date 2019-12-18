package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class MaleOrFemaleTest {

    @Test
    public void has_correct_values() {
        assertEquals("All male", MaleOrFemale.MALE.toString());
        assertEquals("All female", MaleOrFemale.FEMALE.toString());
        assertEquals("", MaleOrFemale.NONE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(3, MaleOrFemale.values().length);
    }
}
