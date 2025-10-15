package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.payment;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.consumer.util.CardPaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_getPayment", port = "8991")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {PaymentConsumerApplication.class}
)
@TestPropertySource(
    properties = {"payment.api.url=localhost:8991"}
)
@PactFolder("pacts")
public class GetPaymentConsumerTest {

    @Autowired
    CardPaymentApi cardPaymentApi;

    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";

    @Pact(provider = "payment_getPayment", consumer = "ia_casePaymentsApi")
    public V4Pact generateGetPaymentPactFragment(
        PactDslWithProvider builder) throws JSONException, IOException {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("paymentReference", "RC-1638-1892-5327-5886");

        PaymentDto response = getPaymentResponse();

        return builder
            .given("The payment reference should not be empty or null", paymentMap)
            .uponReceiving("A request for card payment")
            .path("/card-payments/" + paymentMap.get("paymentReference"))
            .method("GET")
            .headers("Authorization", AUTHORIZATION_TOKEN)
            .willRespondWith()
            .matchHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .status(200)
            .body(buildGetPaymentResponse(response))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generateGetPaymentPactFragment")
    public void getPayment() {
        cardPaymentApi.getPayment(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, "RC-1638-1892-5327-5886");
    }

    private DslPart buildGetPaymentResponse(PaymentDto paymentDto) {

        return newJsonBody((o) -> {
            o.stringType("amount", paymentDto.getAmount().toString())
                .stringType("description", paymentDto.getDescription())
                .stringType("reference", paymentDto.getReference())
                .stringType("currency", paymentDto.getCurrency())
                .stringType("ccd_case_number", paymentDto.getCcdCaseNumber())
                .stringType("channel", paymentDto.getChannel())
                .stringType("status", paymentDto.getStatus())
                .stringType("service", paymentDto.getService().toString())
                .stringType("external_reference", paymentDto.getExternalReference());
        }).build();
    }

    private PaymentDto getPaymentResponse() {

        return PaymentDto.builder()
            .amount(new BigDecimal("140"))
            .description("A card payment for appeal with hearing")
            .reference("RC-1638-1892-5327-5886")
            .currency("GBP")
            .ccdCaseNumber("1633693806322587")
            .channel("online")
            .service("IAC")
            .status("Initiated")
            .externalReference("9s7g2j2q3fvia0u4kneq0l7dvf")
            .build();
    }
}
