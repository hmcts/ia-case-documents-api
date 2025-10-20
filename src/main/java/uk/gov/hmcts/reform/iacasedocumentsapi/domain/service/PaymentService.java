package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.PaymentApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@Service
public class PaymentService {

    private final PaymentApi paymentApi;
    private final RequestUserAccessTokenProvider userAuthorizationProvider;
    private final AuthTokenGenerator serviceAuthorizationProvider;

    public PaymentService(PaymentApi paymentApi,
                          RequestUserAccessTokenProvider userAuthorizationProvider,
                          AuthTokenGenerator serviceAuthorizationProvider) {

        this.paymentApi = paymentApi;
        this.userAuthorizationProvider = userAuthorizationProvider;
        this.serviceAuthorizationProvider = serviceAuthorizationProvider;
    }

    public PaymentResponse creditAccountPayment(CreditAccountPayment creditAccountPaymentRequest) {

        return paymentApi.creditAccountPaymentRequest(
            userAuthorizationProvider.getAccessToken(),
            serviceAuthorizationProvider.generate(),
            creditAccountPaymentRequest
        );
    }
}
