package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithFeeStub {

    default void addFeesRegisterStub(WireMockServer server) {

        String feesHearingRegisterUrl = "/fees/fees-register/fees/lookup?channel=default&event=issue"
                                        + "&jurisdiction1=tribunal"
                                        + "&jurisdiction2=immigration%20and%20asylum%20chamber"
                                        + "&keyword=HearingOral"
                                        + "&service=other";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo(feesHearingRegisterUrl))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"code\": \"FEE0238\",\n"
                              + "  \"description\": \"Appeal determined with a hearing\",\n"
                              + "  \"fee_amount\": 140.00,\n"
                              + "  \"version\": 2}")
                    .build()
            )
        );

        String feesWithoutHearingRegisterUrl = "/fees/fees-register/fees/lookup?channel=default&event=issue"
                                               + "&jurisdiction1=tribunal"
                                               + "&jurisdiction2=immigration%20and%20asylum%20chamber"
                                               + "&keyword=HearingPaper"
                                               + "&service=other";

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo(feesWithoutHearingRegisterUrl))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"code\": \"FEE0456\",\n"
                              + " \"description\": \"Appeal determined without a hearing\",\n"
                              + " \"fee_amount\": 80.00,\n"
                              + "  \"version\": 2}")
                    .build()
            )
        );

    }
}
