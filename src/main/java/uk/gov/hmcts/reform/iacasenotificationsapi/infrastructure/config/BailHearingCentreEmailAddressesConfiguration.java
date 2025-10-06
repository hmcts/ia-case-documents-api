package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailHearingCentre;

@Configuration
@ConfigurationProperties
public class BailHearingCentreEmailAddressesConfiguration {

    private Map<BailHearingCentre, String> bailHearingCentreEmailAddresses = new EnumMap<>(BailHearingCentre.class);

    public Map<BailHearingCentre, String> getBailHearingCentreEmailAddresses() {
        return bailHearingCentreEmailAddresses;
    }

    @Bean
    public Map<BailHearingCentre, String> bailHearingCentreEmailAddresses() {
        return bailHearingCentreEmailAddresses;
    }
}
