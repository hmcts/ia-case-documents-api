package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class YesOrNoTest {

    @Test
    public void has_correct_values() {
        assertEquals("No", YesOrNo.NO.toString());
        assertEquals("Yes", YesOrNo.YES.toString());
    }

    @Test
    public void has_correct_subscriber_types() {
        assertEquals(YesOrNo.YES, YesOrNo.from("Yes").orElse(null));
        assertEquals(YesOrNo.NO, YesOrNo.from("No").orElse(null));
    }

    @Test
    public void has_correct_subscriber_type_ids() {
        assertEquals("No", YesOrNo.NO.getId());
        assertEquals("Yes", YesOrNo.YES.getId());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(2, YesOrNo.values().length);
    }
}
