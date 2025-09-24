package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;

import static uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.S2STokenValidator.SERVICE_AUTHORIZATION_HEADER;

@Tag(name = "Update payment status controller")
@OpenAPIDefinition(tags = {@Tag(name = "UpdatePaymentStatusController", description = "Update payment status")})
@RestController
@Slf4j
public class UpdatePaymentStatusController {

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";

    private final CcdDataService ccdDataService;

    public UpdatePaymentStatusController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @Operation(
        summary = "Update payment status",
        responses =
            {
                @ApiResponse(
                    responseCode = "200",
                    description = "Updated payment status successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "400",
                    description = "Bad Request",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "403",
                    description = "Calling service is not authorised to use the endpoint",
                    content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = String.class)))

            }
    )

    @PutMapping(path = "/payment-updates",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitEventDetails> updatePaymentStatus(
        @RequestHeader(value = SERVICE_AUTHORIZATION_HEADER) String s2sAuthToken,
        @RequestBody PaymentDto paymentDto
    ) {
        String caseId = paymentDto.getCcdCaseNumber();

        CaseMetaData caseMetaData =
            new CaseMetaData(Event.UPDATE_PAYMENT_STATUS, JURISDICTION,
                CASE_TYPE, Long.parseLong(caseId), paymentDto.getStatus(), paymentDto.getReference());

        SubmitEventDetails response = ccdDataService.updatePaymentStatus(caseMetaData, false, s2sAuthToken);
        return ResponseEntity.status(response.getCallbackResponseStatusCode()).body(response);
    }

}
