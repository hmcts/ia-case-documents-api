package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.springframework.http.HttpHeaders;

public interface WithStitchingStub {

    default void addStitchingBundleStub(WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.POST, urlEqualTo("/api/stitch-ccd-bundles"))
                                .build(),
                        aResponse()
                                .withStatus(500)
                                .withHeader("Content-Type", "application/json")
                                .withHeader(HttpHeaders.CONNECTION, "close")
                                .withBody("Error from stitching-api")
                                .build()
                )
        );
    }

    default void addStitchingBundleError400Stub(WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.POST, urlEqualTo("/api/stitch-ccd-bundles"))
                                .build(),
                        aResponse()
                                .withStatus(400)
                                .withHeader("Content-Type", "application/json")
                                .withHeader(HttpHeaders.CONNECTION, "close")
                                .withBody("Error from stitching-api")
                                .build()
                )
        );
    }
}
