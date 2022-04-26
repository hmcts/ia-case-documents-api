package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static org.slf4j.LoggerFactory.getLogger;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import javax.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PreSubmitCallbackDispatcher;

@Api(
    value = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/bail",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class BailPreSubmitCallbackController extends PreSubmitCallbackController<BailCase> {

    private static final org.slf4j.Logger LOG = getLogger(BailPreSubmitCallbackController.class);

    public BailPreSubmitCallbackController(PreSubmitCallbackDispatcher<BailCase> callbackDispatcher) {
        super(callbackDispatcher);
    }

    @ApiOperation(
        value = "Handles 'AboutToStartEvent' callbacks from CCD or delegated calls from IA Case API",
        response = PreSubmitCallbackResponse.class,
        authorizations =
            {
            @Authorization(value = "Authorization"),
            @Authorization(value = "ServiceAuthorization")
            }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Bail case data, with any identified error or warning messages",
            response = PreSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 400,
            message = "Bad Request"
            ),
        @ApiResponse(
            code = 403,
            message = "Forbidden"
            ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type"
            ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error"
            )
    })
    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToStart(
        @ApiParam(value = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return super.ccdAboutToStart(callback);
    }

    @ApiOperation(
        value = "Handles 'AboutToSubmitEvent' callbacks from CCD or delegated calls from IA Case API",
        response = PreSubmitCallbackResponse.class,
        authorizations =
            {
            @Authorization(value = "Authorization"),
            @Authorization(value = "ServiceAuthorization")
            }
    )
    @ApiResponses({
        @ApiResponse(
            code = 200,
            message = "Transformed Bail case data, with any identified error or warning messages",
            response = PreSubmitCallbackResponse.class
            ),
        @ApiResponse(
            code = 400,
            message = "Bad Request"
            ),
        @ApiResponse(
            code = 403,
            message = "Forbidden"
            ),
        @ApiResponse(
            code = 415,
            message = "Unsupported Media Type"
            ),
        @ApiResponse(
            code = 500,
            message = "Internal Server Error"
            )
    })
    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<BailCase>> ccdAboutToSubmit(
        @ApiParam(value = "Bail case data", required = true) @NotNull @RequestBody Callback<BailCase> callback
    ) {
        return super.ccdAboutToSubmit(callback);
    }

}
