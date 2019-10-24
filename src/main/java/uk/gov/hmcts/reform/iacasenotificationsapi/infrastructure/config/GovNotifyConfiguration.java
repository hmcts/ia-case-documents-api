package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.service.notify.NotificationClient;

@Configuration
public class GovNotifyConfiguration {

    @Bean
    @Primary
    public NotificationClient notificationClient(
        @Value("${govnotify.key}") String key,
        @Value("${govnotify.baseUrl}") String goveNotifyBAseUrl
    ) {
        requireNonNull(key);
        return new NotificationClient(key, goveNotifyBAseUrl);
    }
}
