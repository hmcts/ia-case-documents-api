package uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostSubmitCallbackStageTest {

    @Test
    void has_correct_case_event_ids() {
        assertEquals("ccdSubmitted", PostSubmitCallbackStage.CCD_SUBMITTED.toString());
    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(1, PostSubmitCallbackStage.values().length);
    }
}
