package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

import java.util.UUID;

@SuppressWarnings("OperatorWrap")
public class GivensBuilder {

    public GivensBuilder govNotifyWillHandleEmailNotificationAndReturnNotificationId(String someNotificationId) {

        stubFor(post(urlEqualTo("/v2/notifications/email"))
            .willReturn(aResponse()
                .withStatus(201)
                .withHeader("Content-Type", "application/json")
                .withBody("{\n" +
                    "  \"id\" : \"" + someNotificationId +  "\",\n" +
                    "  \"content\": {\n" +
                    "    \"body\" : \"some-body\",\n" +
                    "    \"subject\" : \"some-subject\"\n" +
                    "  },\n" +
                    "  \"template\": {\n" +
                    "    \"id\" : \""  + UUID.randomUUID().toString() + "\",\n" +
                    "    \"version\" : 1,\n" +
                    "    \"uri\" : \"some-uri\"\n" +
                    "  }\n" +
                    "}")));

        return this;
    }

    public GivensBuilder and() {
        return this;
    }
}
