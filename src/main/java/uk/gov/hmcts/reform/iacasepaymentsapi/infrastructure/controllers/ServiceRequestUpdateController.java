package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;


import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.CcdDataService;

@Tag(name = "Update service request controller")
@OpenAPIDefinition(tags = {@Tag(name = "ServiceRequestUpdateController", description = "Update service request")})
@RestController
@Slf4j
public class ServiceRequestUpdateController {

    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";

    private final CcdDataService ccdDataService;

    public ServiceRequestUpdateController(CcdDataService ccdDataService) {
        this.ccdDataService = ccdDataService;
    }

    @PutMapping(path = "/service-request-update",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Ways to pay will call this API and send the status of payment with other details",
        responses = {
            @ApiResponse(responseCode = "200", description = "Callback processed."),
            @ApiResponse(responseCode = "400", description = "Bad Request")
        })
    public ResponseEntity<SubmitEventDetails> serviceRequestUpdate(
        @RequestBody ServiceRequestUpdateDto serviceRequestUpdateDto) {

        String caseId = serviceRequestUpdateDto.getCcdCaseNumber();

        log.info("Callback received for caseId {}, paymentStatus {}", caseId,
                 serviceRequestUpdateDto.getPayment().getStatus());

        CaseMetaData updatePaymentStatusCaseMetaData =
            new CaseMetaData(Event.UPDATE_PAYMENT_STATUS,
                             JURISDICTION,
                             CASE_TYPE,
                             Long.parseLong(caseId),
                             serviceRequestUpdateDto.getServiceRequestStatus(),
                             serviceRequestUpdateDto.getPayment().getReference());

        SubmitEventDetails response = ccdDataService.updatePaymentStatus(updatePaymentStatusCaseMetaData, true);

        return ResponseEntity
            .status(response.getCallbackResponseStatusCode())
            .body(response);
    }
}
