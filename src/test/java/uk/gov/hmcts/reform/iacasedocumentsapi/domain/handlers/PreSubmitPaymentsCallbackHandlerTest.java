package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

public class PreSubmitPaymentsCallbackHandlerTest implements PreSubmitPaymentsCallbackHandler {

    @Test
    public void default_dispatch_priority_is_late() {
        assertEquals(DispatchPriority.PAYMENTS, this.getDispatchPriority());
    }

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return false;
    }

    public PreSubmitCallbackResponse handle(PreSubmitCallbackStage callbackStage, Callback callback) {
        return null;
    }
}
