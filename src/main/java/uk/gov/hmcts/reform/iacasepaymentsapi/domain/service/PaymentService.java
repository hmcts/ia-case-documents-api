package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Service.IAC;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.PaymentProperties;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@Service
@Slf4j
public class PaymentService {

    private final PaymentApi paymentApi;
    private final PaymentProperties paymentProperties;
    private final RequestUserAccessTokenProvider userAuthorizationProvider;
    private final AuthTokenGenerator serviceAuthorizationProvider;

    public PaymentService(PaymentApi paymentApi,
                          PaymentProperties paymentProperties,
                          RequestUserAccessTokenProvider userAuthorizationProvider,
                          AuthTokenGenerator serviceAuthorizationProvider) {

        this.paymentApi = paymentApi;
        this.paymentProperties = paymentProperties;
        this.userAuthorizationProvider = userAuthorizationProvider;
        this.serviceAuthorizationProvider = serviceAuthorizationProvider;
    }

    public PaymentResponse creditAccountPayment(CreditAccountPayment creditAccountPaymentRequest) {

        creditAccountPaymentRequest.setOrganisationName(paymentProperties.getOrganisationUrn());
        creditAccountPaymentRequest.setService(IAC);
        creditAccountPaymentRequest.setSiteId(paymentProperties.getSiteId());

        return paymentApi.creditAccountPaymentRequest(
            userAuthorizationProvider.getAccessToken(),
            serviceAuthorizationProvider.generate(),
            creditAccountPaymentRequest);
    }
}
