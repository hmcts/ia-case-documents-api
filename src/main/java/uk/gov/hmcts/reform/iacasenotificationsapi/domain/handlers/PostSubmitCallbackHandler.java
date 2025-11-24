package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;

public interface PostSubmitCallbackHandler<T extends CaseData> {

    boolean canHandle(
        PostSubmitCallbackStage callbackStage,
        Callback<T> callback
    );

    PostSubmitCallbackResponse handle(
        PostSubmitCallbackStage callbackStage,
        Callback<T> callback
    );
}

