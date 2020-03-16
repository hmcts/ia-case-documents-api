package uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils;

import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest.JWT_URL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdamAuthoritiesConverter.REGISTRATION_ID;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class TestConfiguration {

    // delay ClientRegistrationRepository init when Idam stubs are ready
    @Bean
    @Primary
    ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {

        return new ClientRegistrationRepository() {
            @Override
            public ClientRegistration findByRegistrationId(String registrationId) {
                return OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(properties).get(REGISTRATION_ID);
            }
        };
    }

    // use Custom JwtDecoder to override token validation because of expiration date
    @Bean
    @Primary
    JwtDecoder jwtDecoder() {
        return new CustomJwtDecoder();
    }

    static class CustomJwtDecoder implements JwtDecoder {

        @Override
        public Jwt decode(String token) throws JwtException {
            NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(JWT_URL)
                .build();
            decoder.setJwtValidator(new OAuth2TokenValidator<Jwt>() {
                @Override
                public OAuth2TokenValidatorResult validate(Jwt token) {
                    return OAuth2TokenValidatorResult.success();
                }
            });

            return decoder.decode(token);
        }
    }
}
