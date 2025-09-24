package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers;

import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;

public interface PostSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(
        PostSubmitCallbackStage callbackStage, Callback<T> callback
    );

    PostSubmitCallbackResponse handle(
        PostSubmitCallbackStage callbackStage, Callback<T> callback
    );
}
