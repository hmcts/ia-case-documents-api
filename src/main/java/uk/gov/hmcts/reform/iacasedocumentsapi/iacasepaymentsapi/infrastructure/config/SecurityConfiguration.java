package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.CcdEventAuthorizor;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.SpringAuthorizedRolesProvider;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration  {

    @Getter
    private final List<String> anonymousPaths = new ArrayList<>();
    @Getter
    private final Map<String, List<Event>> roleEventAccess = new HashMap<>();
    private final Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter;
    private final ServiceAuthFilter serviceAuthFilter;

    public SecurityConfiguration(
        Converter<Jwt, Collection<GrantedAuthority>> idamAuthoritiesConverter,
        ServiceAuthFilter serviceAuthFilter
    ) {
        this.idamAuthoritiesConverter = idamAuthoritiesConverter;
        this.serviceAuthFilter = serviceAuthFilter;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().mvcMatchers(
            anonymousPaths.toArray(String[]::new)
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(idamAuthoritiesConverter);


        http
            .addFilterBefore(serviceAuthFilter, AbstractPreAuthenticatedProcessingFilter.class)
            .sessionManagement().sessionCreationPolicy(STATELESS)
            .and()
            .exceptionHandling()
            .and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter)
            .and()
            .and()
            .oauth2Client();

        return http.build();
    }


    @Bean
    public AuthorizedRolesProvider authorizedRolesProvider() {
        return new SpringAuthorizedRolesProvider();
    }

    @Bean
    public CcdEventAuthorizor getCcdEventAuthorizor(AuthorizedRolesProvider authorizedRolesProvider) {

        return new CcdEventAuthorizor(
            ImmutableMap.copyOf(roleEventAccess),
            authorizedRolesProvider
        );
    }

}
