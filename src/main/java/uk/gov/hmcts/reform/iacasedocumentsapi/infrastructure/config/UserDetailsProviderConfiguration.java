package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdamUserDetailsProvider;

@Configuration
public class UserDetailsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserDetailsProvider getRequestUserDetailsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        IdamService idamService
    ) {
        return new IdamUserDetailsProvider(
            requestUserAccessTokenProvider,
            idamService
        );
    }

    @Bean("systemUser")
    public UserDetailsProvider getSystemUserDetailsProvider(
        @Qualifier("systemUser") AccessTokenProvider systemUserAccessTokenProvider,
        IdamService idamService
    ) {
        return new IdamUserDetailsProvider(
            systemUserAccessTokenProvider,
            idamService
        );
    }

    @Bean("requestUserDetails")
    @RequestScope
    public UserDetails getRequestUserDetails(UserDetailsProvider userDetailsProvider) {

        return userDetailsProvider.getUserDetails();
    }

}
