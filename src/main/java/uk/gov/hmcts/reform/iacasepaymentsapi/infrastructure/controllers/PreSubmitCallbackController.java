package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.PreSubmitCallbackDispatcher;

@Api(
    value = "Handles callbacks from CCD that occur *before* changes are persisted.",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
@Slf4j
public class PreSubmitCallbackController {

    private final PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;

    public PreSubmitCallbackController(
        PreSubmitCallbackDispatcher<AsylumCase> callbackDispatcher
    ) {
        requireNonNull(callbackDispatcher, "callbackDispatcher must not be null");

        this.callbackDispatcher = callbackDispatcher;
    }

    @ApiOperation("Handles 'AboutToStartEvent' callbacks from CCD")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Asylum case data, with any identified error or warning messages",
            response = PreSubmitCallbackResponse.class
        )
    })
    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
        @ApiParam(value = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_START, callback);
    }

    @ApiOperation("Handles 'AboutToSubmitEvent' callbacks from CCD")
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Asylum case data, with any identified error or warning messages",
            response = PreSubmitCallbackResponse.class
        )
    })
    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
        @ApiParam(value = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return performStageRequest(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
    }

    private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> performStageRequest(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        log.info(
            "Asylum Case CCD `{}` event `{}` handled received for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            callbackDispatcher.handle(callbackStage, callback);

        log.info(
            "Asylum Case CCD `{}` event `{}` handled for Case ID `{}`",
            callbackStage,
            callback.getEvent(),
            callback.getCaseDetails().getId()
        );

        return ok(callbackResponse);
    }
}
