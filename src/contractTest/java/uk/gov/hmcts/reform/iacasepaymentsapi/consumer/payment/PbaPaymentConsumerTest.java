package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.payment;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Currency;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Service;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.PaymentApi;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_creditAccountPayment", port = "8991")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {PaymentConsumerApplication.class}
)
@TestPropertySource(
    properties = {"payment.api.url=localhost:8991"}
)
@PactFolder("pacts")
public class PbaPaymentConsumerTest {
    @Autowired
    PaymentApi paymentApi;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";

    @Pact(provider = "payment_creditAccountPayment", consumer = "ia_casePaymentsApi")
    public V4Pact generatePactFragment(PactDslWithProvider builder) throws JSONException, IOException {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("accountNumber", "PBA123");
        paymentMap.put("availableBalance", "1000.00");
        paymentMap.put("accountName", "test.account.name");

        return builder
            .given("An active account has sufficient funds for a payment", paymentMap)
            .uponReceiving("A request for payment")
            .path("/credit-account-payments")
            .method("POST")
            .headers("Authorization", AUTHORIZATION_TOKEN)
            .body(objectMapper.writeValueAsString(getPaymentRequest()))
            .willRespondWith()
            .status(201)
            .body(buildPaymentResponse("Success", "success", null, "Insufficient funds available"))
            .toPact(V4Pact.class);
    }

    private DslPart buildPaymentResponse(String status, String paymentStatus, String errorCode, String errorMessage) {
        return newJsonBody((o) -> {
            o.stringType("reference", "reference")
                .stringType("status", status)
                .minArrayLike("status_histories", 1, 1,
                    (sh) -> {
                        sh.stringMatcher("date_updated",
                                         "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                         "2020-10-06T18:54:48.785+0000")
                            .stringMatcher("date_created",
                                           "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                           "2020-10-06T18:54:48.785+0000")
                            .stringValue("status", paymentStatus);
                        if (errorCode != null) {
                            sh.stringValue("error_code", errorCode);
                            sh.stringType("error_message",
                                          errorMessage);
                        }
                    });
        }).build();
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragment")
    public void verifyPayment() {
        paymentApi.creditAccountPaymentRequest(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, getPaymentRequest());
    }

    private static CreditAccountPayment getPaymentRequest() {
        Fee fee = new Fee("code", "description", "v1", BigDecimal.valueOf(200.00));
        return CreditAccountPayment.builder()
            .accountNumber("PBA123")
            .amount(BigDecimal.valueOf(200.00))
            .caseReference("caseRef")
            .ccdCaseNumber("caseNumber")
            .currency(Currency.GBP)
            .customerReference("customerRef")
            .description("IAC Payment")
            .organisationName("immigration and asylum chamber")
            .service(Service.IAC)
            .siteId("BFA1")
            .fees(Collections.singletonList(fee))
            .build();
    }
}
