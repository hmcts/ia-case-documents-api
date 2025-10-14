package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.notify.CustomNotificationClient;

@Slf4j
@Configuration
public class GovNotifyConfiguration {

    @Bean
    @Primary
    public RetryableNotificationClient notificationClient(
        @Value("${govnotify.key}") String key,
        @Value("${govnotify.baseUrl}") String goveNotifyBaseUrl,
        @Value("${govnotify.timeout}") int timeout
    ) {
        requireNonNull(key);

        return new RetryableNotificationClient(new CustomNotificationClient(key, goveNotifyBaseUrl, timeout));
    }

    @Bean("BailClient")
    public RetryableNotificationClient notificationBailClient(
            @Value("${govnotify.bail.key}") String key,
            @Value("${govnotify.baseUrl}") String goveNotifyBaseUrl,
            @Value("${govnotify.bail.timeout}") int timeout
    ) {
        requireNonNull(key);

        return new RetryableNotificationClient(new CustomNotificationClient(key, goveNotifyBaseUrl, timeout));
    }
}
