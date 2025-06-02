package uk.gov.hmcts.reform.iacasedocumentsapi.testutils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.oauth2.IdamAuthoritiesConverter.REGISTRATION_ID;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

@Configuration
public class TestConfiguration {

    @Bean
    @Primary
    ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {

        return registrationId -> OAuth2ClientPropertiesRegistrationAdapter
            .getClientRegistrations(properties)
            .get(REGISTRATION_ID);
    }
}
