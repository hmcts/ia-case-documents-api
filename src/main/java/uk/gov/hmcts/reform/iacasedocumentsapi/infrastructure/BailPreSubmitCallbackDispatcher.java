package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class BailPreSubmitCallbackDispatcher extends PreSubmitCallbackDispatcher<BailCase> {

    public BailPreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<BailCase>> callbackHandlers
    ) {
        super(ccdEventAuthorizor, callbackHandlers);
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        return super.handle(callbackStage, callback);
    }
}
