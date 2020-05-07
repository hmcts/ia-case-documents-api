package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures.someUploadResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.testutils.fixtures.Builder;

public class GivensBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GivensBuilder someLoggedIn(UserDetailsForTestBuilder userDetailsForTestBuilder) {

        stubFor(get(urlEqualTo("/userAuth/details"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(
                    getObjectAsJsonString(
                        userDetailsForTestBuilder))));

        return this;
    }

    public GivensBuilder docmosisWillReturnSomeDocument() {

        stubFor(post(urlEqualTo("/docmosis/rs/render"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(RandomStringUtils.random(100).getBytes())));

        return this;
    }

    public GivensBuilder theDocoumentsManagementApiIsAvailable() {

        stubFor(post(urlEqualTo("/ccdGateway/documents"))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(someUploadResponse())));

        return this;
    }

    private String getObjectAsJsonString(Builder builder) {

        try {
            return objectMapper.writeValueAsString(builder.build());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't serialize object", e);
        }
    }

    public GivensBuilder and() {
        return this;
    }
}
