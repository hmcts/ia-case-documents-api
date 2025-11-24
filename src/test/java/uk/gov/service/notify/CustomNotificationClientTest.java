package uk.gov.service.notify;

import static org.junit.jupiter.api.Assertions.*;

import java.net.HttpURLConnection;
import java.net.URL;
import org.junit.jupiter.api.Test;

class CustomNotificationClientTest {

    private final int timeout = 5000;
    private final CustomNotificationClient notificationClient = new CustomNotificationClient("someKey", "http://someurl", timeout);


    @Test
    void should_return_http_connection_with_timeout() throws Exception {
        HttpURLConnection connection = notificationClient.getConnection(new URL("http://someurl"));

        assertEquals(timeout, connection.getConnectTimeout());
        assertEquals(timeout, connection.getReadTimeout());
    }
}
