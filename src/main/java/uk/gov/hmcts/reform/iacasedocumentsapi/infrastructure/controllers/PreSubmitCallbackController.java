package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;

import io.swagger.v3.oas.annotations.Parameter;
import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PreSubmitCallbackDispatcher;

import java.util.Optional;

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
        log.info("------------------ccdAboutToStart111");
        printLogs(callback);
        log.info("------------------ccdAboutToStart333");
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    private static <T extends CaseData> void printLogs(Callback<T> callback) {
        if (callback.getCaseDetails().getCaseData() instanceof AsylumCase) {
            AsylumCase asylumCase = (AsylumCase) callback.getCaseDetails().getCaseData();
            Optional<AppealType> appealTypeOpt = asylumCase.read(APPEAL_TYPE, AppealType.class);
            log.info("{}", appealTypeOpt);
            log.info("----------");
            Optional<CaseDetails<T>> c = callback.getCaseDetailsBefore();
            if (c.isPresent()) {
                AsylumCase asylumCaseDataBefore = (AsylumCase) c.get().getCaseData();
                log.info("----------asylumCaseDataBefore111");
                Optional<AppealType> appealTypeBeforeOpt = asylumCaseDataBefore.read(APPEAL_TYPE, AppealType.class);
                log.info("{}", appealTypeBeforeOpt);
                log.info("----------asylumCaseDataBefore222");
            } else {
                log.info("----------asylumCaseDataBefore is not present");
            }
        }
    }

    public ResponseEntity<PreSubmitCallbackResponse<T>> ccdAboutToSubmit(
            Callback<T> callback
    ) {
        log.info("------------------ccdAboutToSubmit111");
        printLogs(callback);
        log.info("------------------ccdAboutToSubmit333");
        ResponseEntity<PreSubmitCallbackResponse<T>> response =
                performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        AsylumCase asylumCase = (AsylumCase) response.getBody().getData();
        if (asylumCase != null) {
            Optional<AppealType> appealTypeOpt = asylumCase.read(APPEAL_TYPE, AppealType.class);
            log.info("{}", appealTypeOpt);
            log.info("{}", response);
            log.info("{}", response.getBody());
            log.info("{}", response.getBody().getData());
            for (String key: response.getHeaders().keySet()) {
                log.info("----------ccdAboutToSubmit333 header: {} - {}", key, response.getHeaders().get(key));
            }
            log.info("------------------ccdAboutToSubmit444");
        }
        return response;
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

        return ok(callbackResponse);
    }
}
