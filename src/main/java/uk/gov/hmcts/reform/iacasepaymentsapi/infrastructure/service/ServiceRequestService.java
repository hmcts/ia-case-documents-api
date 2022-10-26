package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CasePaymentRequestDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.ServiceRequestApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;

@Service
@Slf4j
public class ServiceRequestService {

    private static final String PAYMENT_ACTION = "payment";
    private static final int VOLUME_1 = 1;

    private final SystemTokenGenerator systemTokenGenerator;
    private final AuthTokenGenerator serviceAuthorization;
    private final ServiceRequestApi serviceRequestApi;
    private String callBackUrl;

    public ServiceRequestService(SystemTokenGenerator systemTokenGenerator,
                                 AuthTokenGenerator serviceAuthorization,
                                 ServiceRequestApi serviceRequestApi,
                                 @Value("${payment.api.callback-url}") String callBackUrl) {
        this.systemTokenGenerator = systemTokenGenerator;
        this.serviceAuthorization = serviceAuthorization;
        this.serviceRequestApi = serviceRequestApi;
        this.callBackUrl = callBackUrl;
    }

    public ServiceRequestResponse createServiceRequest(Callback<AsylumCase> callback, Fee fee) throws Exception {

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

        return serviceRequestApi.createServiceRequest(
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
    }
}
