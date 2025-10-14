package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;


public interface NotificationGenerator {

    void generate(Callback<AsylumCase> callback);

    default Message getSuccessMessage() {
        return new Message("success",  "success");
    }
}
