package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;
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

@Tag(name = "Asylum Service")
@RequestMapping(
    path = "/asylum",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
@RestController
@Slf4j
public class AsylumPreSubmitCallbackController extends PreSubmitCallbackController<AsylumCase> {

    private final ObjectMapper objectMapper;

    public AsylumPreSubmitCallbackController(AsylumPreSubmitCallbackDispatcher callbackDispatcher, ObjectMapper objectMapper) {
        super(callbackDispatcher);
        this.objectMapper = objectMapper;
    }

    @Operation(
        summary = "Handles 'AboutToStartEvent' callbacks from CCD or delegated calls from IA Case API",
        security =
        {
            @SecurityRequirement(name = "Authorization"),
            @SecurityRequirement(name = "ServiceAuthorization")
        },
        responses =
        {
            @ApiResponse(
                responseCode = "200",
                description = "Transformed Asylum case data, with any identified error or warning messages",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "415",
                description = "Unsupported Media Type",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class)))
        }

    )
    @PostMapping(path = "/ccdAboutToStart")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToStart(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        return super.ccdAboutToStart(callback);
    }

    @Operation(
        summary = "Handles 'AboutToSubmitEvent' callbacks from CCD or delegated calls from IA Case API",
        security =
        {
            @SecurityRequirement(name = "Authorization"),
            @SecurityRequirement(name = "ServiceAuthorization")
        },
        responses =
        {
            @ApiResponse(
                responseCode = "200",
                description = "Transformed Asylum case data, with any identified error or warning messages",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "400",
                description = "Bad Request",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "415",
                description = "Unsupported Media Type",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class))),
            @ApiResponse(
                responseCode = "500",
                description = "Internal Server Error",
                content = @Content(schema = @Schema(implementation = PreSubmitCallbackResponse.class)))
        }
    )
    @PostMapping(path = "/ccdAboutToSubmit")
    public ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> ccdAboutToSubmit(
        @Parameter(name = "Asylum case data", required = true) @NotNull @RequestBody Callback<AsylumCase> callback
    ) {
        log.info("---------ccdAboutToSubmit");
        ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> preSubmitCallbackResponseResponseEntity = super.ccdAboutToSubmit(callback);
        PreSubmitCallbackResponse<AsylumCase> response = preSubmitCallbackResponseResponseEntity.getBody();
        try {
            String json = objectMapper.writeValueAsString(response);
            log.info("---------111:\n{}", json);
            PreSubmitCallbackResponse<AsylumCase> resp = objectMapper.readValue(json, PreSubmitCallbackResponse.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(resp);
        } catch (JsonProcessingException ex) {
            log.info("---------222");
            log.error("--------222 ", ex);
            return preSubmitCallbackResponseResponseEntity;
        }
        //return super.ccdAboutToSubmit(callback);
    }
}
