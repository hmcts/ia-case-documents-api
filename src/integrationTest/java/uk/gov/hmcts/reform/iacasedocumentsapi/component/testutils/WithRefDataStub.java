package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;

public interface WithRefDataStub {

    default void addRefDataStub(WireMockServer server, Resource resourceFile) throws IOException {

        String refDataUrl = "/refdata/refdata/external/v1/organisations/pbas";
        String userEmail = "ia.legalrep.orgcreator@gmail.com";

        String refDataResponseJson =
                new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.GET, urlEqualTo(refDataUrl))
                    .withHeader("UserEmail", equalTo(userEmail))
                    .build(),
                aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(refDataResponseJson)
                    .build()
            )
        );
    }

}
