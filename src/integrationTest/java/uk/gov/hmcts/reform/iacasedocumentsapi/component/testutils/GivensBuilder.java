package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures.someUploadResponse;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.apache.commons.lang.RandomStringUtils;

public interface GivensBuilder {

    default void docmosisWillReturnSomeDocument(WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.POST, urlEqualTo("/docmosis/rs/render"))
                                .build(),
                        aResponse()
                .withStatus(200)
                .withBody(RandomStringUtils.random(100).getBytes())
                .build()));
    }

    default void theDocoumentsManagementApiIsAvailable(WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.POST, urlEqualTo("/ccdGateway/documents"))
                                .build(),
                        aResponse()
                .withStatus(200)
                .withBody(someUploadResponse())
                .build()));
    }

    default GivensBuilder and() {
        return this;
    }
}
