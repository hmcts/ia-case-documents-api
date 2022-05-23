package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

@Component
public class AsylumPreSubmitCallbackDispatcher extends PreSubmitCallbackDispatcher<AsylumCase> {

    public AsylumPreSubmitCallbackDispatcher(
        CcdEventAuthorizor ccdEventAuthorizor,
        List<PreSubmitCallbackHandler<AsylumCase>> callbackHandlers
    ) {
        super(ccdEventAuthorizor, callbackHandlers);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        return super.handle(callbackStage, callback);
    }
}
