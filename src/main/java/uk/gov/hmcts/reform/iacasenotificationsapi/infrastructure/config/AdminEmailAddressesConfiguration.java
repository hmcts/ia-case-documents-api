package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;

@Configuration
@ConfigurationProperties
public class AdminEmailAddressesConfiguration {

    private Map<HearingCentre, String> adminEmailAddresses = new EnumMap<>(HearingCentre.class);

    public Map<HearingCentre, String> getAdminEmailAddresses() {
        return adminEmailAddresses;
    }

    @Bean
    public Map<HearingCentre, String> adminEmailAddresses() {

        return adminEmailAddresses;
    }

}
