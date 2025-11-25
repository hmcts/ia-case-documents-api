package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Message;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;


public interface BailNotificationGenerator {

    void generate(Callback<BailCase> callback);

    default Message getSuccessMessage() {
        return new Message("success", "success");
    }
}
