package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AsylumPreSubmitCallbackDispatcher;

@Api(
    value = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
public class AsylumPreSubmitCallbackController extends PreSubmitCallbackController<AsylumCase> {

    public AsylumPreSubmitCallbackController(AsylumPreSubmitCallbackDispatcher callbackDispatcher) {
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
            message = "Transformed Asylum case data, with any identified error or warning messages",
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
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
        @ApiParam(value = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
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
            message = "Transformed Asylum case data, with any identified error or warning messages",
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
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
        @ApiParam(value = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return super.ccdAboutToSubmit(callback);
    }
}
