package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest;

public interface WithIdamStub {

    final ObjectMapper objectMapper = new ObjectMapper();

    default void someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder userDetailsForTestBuilder, WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.GET, urlEqualTo("/userAuth/o/userinfo"))
                                .build(),
                        aResponse()
                                .withStatus(201)
                                .withHeader("Content-Type", "application/json")
                                .withHeader(HttpHeaders.CONNECTION, "close")
                                .withBody(
                                        getObjectAsJsonString(
                                                userDetailsForTestBuilder))
                                .build()));
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return objectMapper.writeValueAsString(builder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }
}
