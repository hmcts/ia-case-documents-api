package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;

@Api(tags = {"Update payment status controller"})
@SwaggerDefinition(tags = {@Tag(name = "UpdatePaymentStatusController", description = "Update payment status")})
@RestController
@Slf4j
public class UpdatePaymentStatusController {

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";

    private final CcdDataService ccdDataService;

    public UpdatePaymentStatusController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @ApiOperation(value = "Update payment status", notes = "Update payment status")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Updated payment status successfully"),
        @ApiResponse(code = 400, message = "Bad request"),
        @ApiResponse(code = 500, message = "Internal server error")
    })
    @PutMapping(path = "/payment-updates",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubmitEventDetails> updatePaymentStatus(@RequestBody PaymentDto paymentDto) {

        String caseId = paymentDto.getCcdCaseNumber();

        CaseMetaData caseMetaData =
            new CaseMetaData(Event.UPDATE_PAYMENT_STATUS, JURISDICTION,
                             CASE_TYPE, Long.valueOf(caseId), paymentDto.getStatus(), paymentDto.getReference());

        SubmitEventDetails response = ccdDataService.updatePaymentStatus(caseMetaData);
        return ResponseEntity.status(response.getCallbackResponseStatusCode()).body(response);
    }
}
