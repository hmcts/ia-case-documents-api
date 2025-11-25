package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class DetentionEngagementTeamIrcEmailAddressesConfiguration {

    private Map<String, String> detentionEngagementTeamIrcEmailAddresses = new HashMap<>();

    public Map<String, String> getDetentionEngagementTeamIrcEmailAddresses() {
        return detentionEngagementTeamIrcEmailAddresses;
    }

    @Bean
    public Map<String, String> detentionEngagementTeamIrcEmailAddresses() {
        return detentionEngagementTeamIrcEmailAddresses;
    }
}
