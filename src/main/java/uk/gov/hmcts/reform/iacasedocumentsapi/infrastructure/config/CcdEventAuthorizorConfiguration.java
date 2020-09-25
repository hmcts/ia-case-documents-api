package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.CcdEventAuthorizor;

@Configuration
@ConfigurationProperties(prefix = "security")
public class CcdEventAuthorizorConfiguration {

    private final Map<String, List<Event>> roleEventAccess = new HashMap<>();

    public Map<String, List<Event>> getRoleEventAccess() {
        return roleEventAccess;
    }

    @Bean
    @Primary
    public CcdEventAuthorizor getCcdEventAuthorizor(
        AuthorizedRolesProvider authorizedRolesProvider
    ) {
        return new CcdEventAuthorizor(
            ImmutableMap.copyOf(roleEventAccess),
            authorizedRolesProvider
        );
    }
}
