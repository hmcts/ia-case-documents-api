package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties
public class DetentionEngagementTeamPrisonEmailAddressesConfiguration {

    private Map<String, String> detentionEngagementTeamPrisonEmailAddresses = new HashMap<>();

    public Map<String, String> getDetentionEngagementTeamPrisonEmailAddresses() {
        return detentionEngagementTeamPrisonEmailAddresses;
    }

    @Bean
    public Map<String, String> detentionEngagementTeamPrisonEmailAddresses() {
        return detentionEngagementTeamPrisonEmailAddresses;
    }
}
