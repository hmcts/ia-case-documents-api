package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import tools.jackson.core.JacksonException;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest;

public interface WithIdamStub {

    final ObjectMapper objectMapper = new JsonMapper();

    default void someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder userDetailsForTestBuilder, WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                                .build(),
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withBody(
                                        getObjectAsJsonString(
                                                userDetailsForTestBuilder))
                                .build()));
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return objectMapper.writeValueAsString(builder.build());
        } catch (JacksonException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }
}
