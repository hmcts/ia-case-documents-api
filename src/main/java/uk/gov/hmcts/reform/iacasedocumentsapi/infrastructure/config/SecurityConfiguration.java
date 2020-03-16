package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;
import uk.gov.hmcts.reform.auth.checker.spring.serviceonly.AuthCheckerServiceOnlyFilter;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AuthorizedRolesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.SpringAuthorizedRolesProvider;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final List<String> anonymousPaths = new ArrayList<>();

    private final RequestAuthorizer<User> userRequestAuthorizer;
    private final RequestAuthorizer<Service> serviceRequestAuthorizer;
    private final AuthenticationManager authenticationManager;
    private final Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter;

    private Boolean useOpenId;

    public SecurityConfiguration(
        RequestAuthorizer<User> userRequestAuthorizer,
        RequestAuthorizer<Service> serviceRequestAuthorizer,
        AuthenticationManager authenticationManager,
        Converter<Jwt, Collection<GrantedAuthority>> authoritiesConverter
    ) {
        this.userRequestAuthorizer = userRequestAuthorizer;
        this.serviceRequestAuthorizer = serviceRequestAuthorizer;
        this.authenticationManager = authenticationManager;
        this.authoritiesConverter = authoritiesConverter;
    }

    @Override
    public void configure(WebSecurity web) {

        web.ignoring().mvcMatchers(
            anonymousPaths
                .stream()
                .toArray(String[]::new)
        );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        if (useOpenId) {

            JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
            jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

            AuthCheckerServiceOnlyFilter authCheckerServiceOnlyFilter = new AuthCheckerServiceOnlyFilter(serviceRequestAuthorizer);
            authCheckerServiceOnlyFilter.setAuthenticationManager(authenticationManager);

            http
                .addFilter(authCheckerServiceOnlyFilter)
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .exceptionHandling()
                .accessDeniedHandler((request, response, exc) -> response.sendError(HttpServletResponse.SC_FORBIDDEN))
                .authenticationEntryPoint((request, response, exc) -> response.sendError(HttpServletResponse.SC_FORBIDDEN))
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

        } else {

            AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter =
                new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);
            authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);

            http
                .addFilter(authCheckerServiceAndUserFilter)
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .authorizeRequests().anyRequest().authenticated();
        }

    }

    @Bean
    public AuthorizedRolesProvider authorizedRolesProvider() {
        return new SpringAuthorizedRolesProvider();
    }

    public void setUseOpenId(Boolean useOpenId) {
        this.useOpenId = useOpenId;
    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }
}
