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
import java.util.Arrays;
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
import uk.gov.hmcts.reform.iacasepaymentsapi.consumer.entities.CardPaymentRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.consumer.util.CardPaymentApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;

@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "payment_cardPayment", port = "8991")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
    classes = {PaymentConsumerApplication.class}
)
@TestPropertySource(
    properties = {"payment.api.url=localhost:8991"}
)
@PactFolder("pacts")
public class CardPaymentConsumerTest {

    @Autowired
    CardPaymentApi cardPaymentApi;

    ObjectMapper objectMapper = new ObjectMapper();

    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String AUTHORIZATION_TOKEN = "Bearer some-access-token";

    @Pact(provider = "payment_cardPayment", consumer = "ia_casePaymentsApi")
    public V4Pact generateCreatePaymentPactFragment(
        PactDslWithProvider builder) throws JSONException, IOException {

        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("amount", "140");
        paymentMap.put("calculatedAmount", "140");

        return builder
            .given("The appeal payment amount and fee amount should be equal", paymentMap)
            .uponReceiving("A request for card payment")
            .path("/card-payments")
            .method("POST")
            .headers("Authorization", AUTHORIZATION_TOKEN)
            .body(objectMapper.writeValueAsString(getCardPaymentRequest()))
            .willRespondWith()
            .status(201)
            .body(buildCreatePaymentResponse("RC-1638-1892-5327-5886", "Initiated",
                                       "9s7g2j2q3fvia0u4kneq0l7dvf",
                                       new PaymentDto.LinksDto(
                                           new PaymentDto.LinkDto(
                                               "secure/"
                                                   + "65888814-3a93-48cf-8e6b-fc78536eb7ad", "GET"),
                                           null, null)
            ))
            .toPact(V4Pact.class);
    }

    @Test
    @PactTestFor(pactMethod = "generateCreatePaymentPactFragment")
    public void createPayment() {
        cardPaymentApi.cardPaymentRequest(AUTHORIZATION_TOKEN, SERVICE_AUTH_TOKEN, getCardPaymentRequest());
    }

    private DslPart buildCreatePaymentResponse(String reference, String status, String externalReference,
                                         PaymentDto.LinksDto links) {
        return newJsonBody((o) -> {
            o.stringType("reference", "reference")
                .stringType("status", status)
                .minArrayLike("status_histories", 1, 1,
                    (sh) -> {
                        sh.stringMatcher("date_updated",
                                         "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                         "2021-11-29T12:34:15.545+0000")
                            .stringMatcher("date_created",
                                           "^(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}\\+\\d{4})$",
                                           "2021-11-29T12:34:15.545+0000")
                            .stringValue("status", status);
                    })
                .minArrayLike("_links", 1, 1,
                    (sh) -> {
                        sh.stringMatcher("href", links.getNextUrl().getHref())
                            .stringMatcher("method", links.getNextUrl().getMethod());
                    });
        }).build();
    }

    private CardPaymentRequest getCardPaymentRequest() {

        CardPaymentRequest cardPaymentRequest = new CardPaymentRequest();
        cardPaymentRequest.setAmount(new BigDecimal("140"));
        cardPaymentRequest.setCcdCaseNumber("1633693806322587");
        cardPaymentRequest.setChannel("online");
        cardPaymentRequest.setCurrency("GBP");
        cardPaymentRequest.setDescription("A card payment for appeal with hearing");
        cardPaymentRequest.setSiteId("BFA1");
        cardPaymentRequest.setService("IAC");
        cardPaymentRequest.setFees(Arrays.asList(getFeeWithHearing()));

        return cardPaymentRequest;
    }

    private FeeDto getFeeWithHearing() {

        return FeeDto.builder()
            .calculatedAmount(new BigDecimal("140"))
            .code("FEE0123")
            .version("1")
            .build();
    }
}
