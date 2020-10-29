package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;
import static uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures.someUploadResponse;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

public interface WithDocumentUploadStub {

    default void addDocumentUploadStub(WireMockServer server) {
        server.addStubMapping(
                new StubMapping(
                        newRequestPattern(RequestMethod.POST, urlEqualTo("/documents"))
                                .build(),
                        aResponse()
                                .withStatus(201)
                                .withBody(someUploadResponse())
                                .build()));
    }
}
