package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;


public interface BailNotificationGenerator {

    void generate(Callback<BailCase> callback);

    default Message getSuccessMessage() {
        return new Message("success", "success");
    }
}
