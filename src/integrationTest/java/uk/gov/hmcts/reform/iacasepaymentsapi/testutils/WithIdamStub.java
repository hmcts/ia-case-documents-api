package uk.gov.hmcts.reform.iacasepaymentsapi.testutils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

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

public interface WithIdamStub {

    String EMAIL = "ia.legalrep.orgcreator@gmail.com";

    default void addUserInfoStub(WireMockServer server) {
        server.addStubMapping(
            new StubMapping(
                newRequestPattern(GET, urlEqualTo("/idam/o/userinfo"))
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
                newRequestPattern(POST, urlEqualTo("/idam/o/token"))
                    .withHeader(CONTENT_TYPE, containing(APPLICATION_FORM_URLENCODED_VALUE))
                    .withRequestBody(equalTo("grant_type=password&redirect_uri=http%3A%2F%2Flocalhost%3"
                                         + "A3002%2Foauth2%2Fcallback&client_id=ia&client_secret=some"
                                         + "thing&username=ia-system-user%40fake.hmcts.net&password=s"
                                         + "omething&scope=openid+profile+authorities+acr+roles+creat"
                                         + "e-user+manage-user+search-user"))
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
