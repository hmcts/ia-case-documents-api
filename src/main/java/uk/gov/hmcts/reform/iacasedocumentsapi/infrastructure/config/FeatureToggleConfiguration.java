package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import com.launchdarkly.sdk.server.Components;
import com.launchdarkly.sdk.server.LDClient;
import com.launchdarkly.sdk.server.LDConfig;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class FeatureToggleConfiguration {

    private final String sdkKey;
    private final Integer connectionTimeout;
    private final Integer socketTimeout;

    public FeatureToggleConfiguration(
        @Value("${launchDarkly.sdkKey}") String sdkKey,
        @Value("${launchDarkly.connectionTimeout}") Integer connectionTimeout,
        @Value("${launchDarkly.socketTimeout}") Integer socketTimeout
    ) {
        this.sdkKey = sdkKey;
        this.connectionTimeout = connectionTimeout;
        this.socketTimeout = socketTimeout;
    }

    @Bean
    public LDConfig ldConfig() {
        return new LDConfig.Builder()
                .http(Components
                    .httpConfiguration()
                .connectTimeout(Duration.ofMillis(connectionTimeout))
                .socketTimeout(Duration.ofMillis(socketTimeout))
                )
                .build();
    }

    @Bean
    public LDClientInterface ldClient(LDConfig ldConfig) {
        return new LDClient(sdkKey, ldConfig);
    }

}
