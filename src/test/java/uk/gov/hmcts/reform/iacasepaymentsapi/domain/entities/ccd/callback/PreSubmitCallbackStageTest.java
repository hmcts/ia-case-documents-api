package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PreSubmitCallbackStageTest {

    @Test
    public void has_correct_values() {

        assertEquals("aboutToStart", PreSubmitCallbackStage.ABOUT_TO_START.toString());
        assertEquals("aboutToSubmit", PreSubmitCallbackStage.ABOUT_TO_SUBMIT.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {

        assertEquals(2, PreSubmitCallbackStage.values().length);
    }
}
