package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import java.util.EnumMap;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;


@Configuration
@ConfigurationProperties
public class HearingCentreEmailAddressesConfiguration {

    private Map<HearingCentre, String> hearingCentreEmailAddresses = new EnumMap<>(HearingCentre.class);

    public Map<HearingCentre, String> getHearingCentreEmailAddresses() {
        return hearingCentreEmailAddresses;
    }

    @Bean
    public Map<HearingCentre, String> hearingCentreEmailAddresses() {
        return hearingCentreEmailAddresses;
    }
}
