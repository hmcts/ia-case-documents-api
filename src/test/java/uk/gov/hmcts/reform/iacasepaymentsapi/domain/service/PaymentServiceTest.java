package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.BDDMockito.when;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Currency;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Service;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.StatusHistories;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock private PaymentApi paymentApi;
    @Mock private RequestUserAccessTokenProvider userAuthorizationProvider;
    @Mock private AuthTokenGenerator serviceAuthorizationProvider;

    private CreditAccountPayment creditAccountPayment;
    private PaymentService paymentService;

    @BeforeEach
    public void setUp() {
        paymentService = new PaymentService(
            paymentApi,
            userAuthorizationProvider,
            serviceAuthorizationProvider);
    }

    @Test
    void should_make_a_pba_payment() {
        when(userAuthorizationProvider.getAccessToken()).thenReturn("userAuthorizationToken");
        when(serviceAuthorizationProvider.generate()).thenReturn("serviceAuthorizationToken");

        creditAccountPayment = getCreditAccountPaymentRequest();
        PaymentResponse expectedPaymentResponse = getExpectedPaymentResponse();
        when(paymentApi.creditAccountPaymentRequest(
            userAuthorizationProvider.getAccessToken(),
            serviceAuthorizationProvider.generate(),
            creditAccountPayment
            ))
            .thenReturn(expectedPaymentResponse);

        PaymentResponse paymentResponse = paymentService.creditAccountPayment(creditAccountPayment);

        assertNotNull(paymentResponse);

        assertEquals("RC-1590-6748-2373-9129", paymentResponse.getReference());
        assertEquals("Success", paymentResponse.getStatus());
    }

    private CreditAccountPayment getCreditAccountPaymentRequest() {

        return new CreditAccountPayment(
            "PBA0066906",
            new BigDecimal("140.00"),
            "caseReference",
            "ccdCaseNumber",
            Currency.GBP,
            "customerReference",
            "Some description",
            "ia-legal-rep-org",
            Service.IAC,
            "BFA1",
            List.of(new Fee("FEE0123",
                            "Fee description",
                            "1",
                            new BigDecimal("140.00"))));
    }

    private PaymentResponse getExpectedPaymentResponse() {
        return new PaymentResponse("RC-1590-6748-2373-9129", new Date(),
                                   "Success", "2020-1590674823325",
                List.of(
                        new StatusHistories("Success",
                                null,
                                null,
                                null,
                                null)));
    }
}
