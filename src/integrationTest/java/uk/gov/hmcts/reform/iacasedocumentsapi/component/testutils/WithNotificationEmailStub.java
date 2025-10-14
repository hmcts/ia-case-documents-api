package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.matching.RequestPatternBuilder.newRequestPattern;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import java.util.UUID;

public interface WithNotificationEmailStub {

    String someNotificationId = UUID.randomUUID().toString();

    default void addNotificationEmailStub(WireMockServer server) {

        server.addStubMapping(
            new StubMapping(
                newRequestPattern(RequestMethod.POST, urlEqualTo("/v2/notifications/email"))
                    .build(),
                aResponse()
                    .withStatus(201)
                    .withBody("{\n"
                        + "  \"id\" : \"" + someNotificationId + "\",\n"
                        + "  \"content\": {\n"
                        + "    \"body\" : \"some-body\",\n"
                        + "    \"subject\" : \"some-subject\"\n"
                        + "  },\n"
                        + "  \"template\": {\n"
                        + "    \"id\" : \"" + someNotificationId + "\",\n"
                        + "    \"version\" : 1,\n"
                        + "    \"uri\" : \"some-uri\"\n"
                        + "  }\n"
                        + "}")
                    .build()));
    }
}
