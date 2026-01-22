package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PreSubmitCallbackDispatcher;

@Slf4j
public class PreSubmitCallbackController<T extends CaseData> {

    private static final org.slf4j.Logger LOG = getLogger(PreSubmitCallbackController.class);

    private final PreSubmitCallbackDispatcher<T> callbackDispatcher;

    public PreSubmitCallbackController(
        PreSubmitCallbackDispatcher<T> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToStart(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<T> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToSubmit(
        Callback<T> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    private ResponseEntity<PreSubmitCallbackResponse<T>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<T> callback
    ) {
        LOG.info(
            "Asylum Case CCD `{}` event `{}` received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<T> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        LOG.info(
            "Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        AsylumCase asylumCase = (AsylumCase)callbackResponse.getData();
        log.info("-------------appealType {}", asylumCase.read(AsylumCaseDefinition.APPEAL_TYPE));
        log.info("-------------appellantInUk {}", asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK));

        return ok(callbackResponse);
    }
}
