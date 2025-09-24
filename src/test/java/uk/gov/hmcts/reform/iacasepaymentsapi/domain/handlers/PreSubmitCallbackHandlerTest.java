package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

class PreSubmitCallbackHandlerTest implements PreSubmitCallbackHandler<CaseData> {

    @Test
    void default_dispatch_priority_is_late() {

        Assertions.assertEquals(DispatchPriority.LATE, this.getDispatchPriority());
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
        return false;
    }

    @Override
    public PreSubmitCallbackResponse<CaseData> handle(PreSubmitCallbackStage callbackStage, Callback<CaseData> callback) {
        return null;
    }
}
