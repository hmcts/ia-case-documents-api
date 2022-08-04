package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.controllers;


import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PostSubmitCallbackDispatcher;

@Slf4j

public class PostSubmitCallbackController<T extends CaseData> {

    private static final org.slf4j.Logger LOG = getLogger(PostSubmitCallbackController.class);

    private final PostSubmitCallbackDispatcher<T> callbackDispatcher;

    public PostSubmitCallbackController(
        PostSubmitCallbackDispatcher<T> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    public ResponseEntity<PostSubmitCallbackResponse> ccdSubmitted(
        @Parameter(name = "Asylum case data", required = true) @RequestBody Callback<T> callback
    ) {
        LOG.info(
            "Asylum Case Notifications API `ccdSubmitted` event `{}` received for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PostSubmitCallbackResponse callbackResponse =
            callbackDispatcher.handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        LOG.info(
            "Asylum Case Notifications API `ccdSubmitted` event `{}` handled for Case ID `{}`",
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
