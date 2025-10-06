package uk.gov.service.notify;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class CustomNotificationClient extends NotificationClient {

    private final int timeout;

    public CustomNotificationClient(final String apiKey, final String baseUrl, int timeout) {
        super(apiKey, baseUrl);
        this.timeout = timeout;
    }

    @Override
    HttpURLConnection getConnection(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setReadTimeout(timeout);
        conn.setConnectTimeout(timeout);

        return conn;
    }

}
