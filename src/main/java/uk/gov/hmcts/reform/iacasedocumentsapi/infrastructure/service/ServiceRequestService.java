package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.ServiceRequestRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.ServiceRequestApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.service.exceptions.PaymentServiceRequestException;

@Service
@Slf4j
@EnableRetry
public class ServiceRequestService {

    private static final String PAYMENT_ACTION = "payment";
    private static final int VOLUME_1 = 1;

    private final SystemTokenGenerator systemTokenGenerator;
    private final AuthTokenGenerator serviceAuthorization;
    private final ServiceRequestApi serviceRequestApi;
    private final String callBackUrl;

    public ServiceRequestService(SystemTokenGenerator systemTokenGenerator,
                                 AuthTokenGenerator serviceAuthorization,
                                 ServiceRequestApi serviceRequestApi,
                                 @Value("${payment.api.callback-url}") String callBackUrl) {
        this.systemTokenGenerator = systemTokenGenerator;
        this.serviceAuthorization = serviceAuthorization;
        this.serviceRequestApi = serviceRequestApi;
        this.callBackUrl = callBackUrl;
    }

    @Retryable(retryFor = { FeignException.class }, maxAttempts = 3, backoff = @Backoff(3000))
    public ServiceRequestResponse createServiceRequest(Callback<AsylumCase> callback, Fee fee) {

        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        AsylumCase asylumCase = caseDetails.getCaseData();
        String ccdCaseReferenceNumber = String.valueOf(caseDetails.getId());
        String appealReferenceNumber = asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
            .orElse("");
        String appellantGivenNames = asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)
            .orElse("");
        String appellantFamilyName = asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)
            .orElse("");
        String responsibleParty = appellantGivenNames + " " + appellantFamilyName;

        String userAuth = systemTokenGenerator.generate();
        String serviceAuth = serviceAuthorization.generate();
        ServiceRequestResponse serviceRequestResponse;
        log.info("Calling Payment Service Request API for case reference {}", ccdCaseReferenceNumber);
        try {
            serviceRequestResponse = serviceRequestApi.createServiceRequest(
                userAuth,
                serviceAuth,
                ServiceRequestRequest.builder()
                    .callBackUrl(callBackUrl)
                    .casePaymentRequest(
                        CasePaymentRequestDto.builder()
                            .action(PAYMENT_ACTION)
                            .responsibleParty(responsibleParty)
                            .build())
                    .caseReference(appealReferenceNumber)
                    .ccdCaseNumber(ccdCaseReferenceNumber)
                    .fees(new FeeDto[]{
                        FeeDto.builder()
                            .calculatedAmount(fee.getCalculatedAmount())
                            .code(fee.getCode())
                            .version(fee.getVersion())
                            .volume(VOLUME_1).build()
                    })
                    .build()
            );
            log.info(
                "Payment Service Request API response for case reference {} - {}",
                ccdCaseReferenceNumber,
                serviceRequestResponse != null ? serviceRequestResponse.getServiceRequestReference() : ""
            );
        } catch (FeignException fe) {
            log.error(
                "Error in calling Payment Service Request API for case reference {} \n {}\n Retrying now...",
                ccdCaseReferenceNumber,
                fe.getMessage()
            );
            throw fe;
        }
        return serviceRequestResponse;
    }

    @Recover
    public ServiceRequestResponse recover(FeignException fe, Callback<AsylumCase> callback, Fee fee) {
        log.error("Error in calling Payment Service Request API for 3 retries \n {}", fe.getMessage());
        throw new PaymentServiceRequestException(fe.getMessage(), fe.getCause());
    }
}
