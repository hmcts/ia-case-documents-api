package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

public interface WithPaymentStub {

    default void addPaymentStub(WireMockServer server) {

        String paymentUrl = "/payment/credit-account-payments";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(POST, urlEqualTo(paymentUrl))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"status\": \"Success\",\n"
                                  + "  \"reference\": \"RC-1590-6786-1063-9996\" ,"
                                  + "  \"date_created\": \"2020-05-29T15:10:10.694+0000\"  }")
                    .build()
            )
        );
    }

}
