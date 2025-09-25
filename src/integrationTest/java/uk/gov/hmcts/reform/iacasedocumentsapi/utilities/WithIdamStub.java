package uk.gov.hmcts.reform.iacasedocumentsapi.utilities;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.http.RequestMethod.GET;
import static com.github.tomakehurst.wiremock.http.RequestMethod.POST;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.Builder;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.fixtures.TokenForTest;

public interface WithIdamStub {

    ObjectMapper idamObjectMapper = new ObjectMapper();
    String EMAIL = "ia.legalrep.orgcreator@gmail.com";

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
                    .build()
            ));
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return idamObjectMapper.writeValueAsString(builder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }

    default void addUserInfoStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(GET, urlEqualTo("/userAuth/o/userinfo"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"sub\": \"" + EMAIL + "\"}")
                    .build()
            )
        );
    }

    default void addIdamTokenStub(WireMockServer server) throws JsonProcessingException {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(POST, urlEqualTo("/userAuth/o/token"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(equalTo("password=something" +
                                                 "&grant_type=password" +
                                                 "&scope=openid+profile+authorities+acr+roles+create-user+manage-user+search-user" +
                                                 "&redirect_uri=http%3A%2F%2Flocalhost%3A3002%2Foauth2%2Fcallback" +
                                                 "&client_secret=something" +
                                                 "&client_id=ia" +
                                                 "&username=ia-system-user%40fake.hmcts.net"))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                    .withBody(new ObjectMapper().writeValueAsString(TokenForTest.generateValid()))
                    .build()
            )
        );
    }
}
