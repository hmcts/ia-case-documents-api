package uk.gov.hmcts.reform.iacasenotificationsapi.component.testutils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class GovNotifyApiVerifications {

    public void govNotifyReceivesAnEmailNotificationRequest() {

        verify(
            postRequestedFor(urlEqualTo("/v2/notifications/email")));
    }
}
