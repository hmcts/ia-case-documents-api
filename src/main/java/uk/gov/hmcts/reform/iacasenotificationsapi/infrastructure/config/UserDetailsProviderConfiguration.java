package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.context.annotation.RequestScope;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.idam.IdamUserDetailsProvider;

@Configuration
public class UserDetailsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserDetailsProvider getRequestUserDetailsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        IdamApi idamApi
    ) {
        return new IdamUserDetailsProvider(
            requestUserAccessTokenProvider,
            idamApi
        );
    }

    @Bean("requestUserDetails")
    @RequestScope
    public UserDetails getRequestUserDetails(UserDetailsProvider userDetailsProvider) {

        return userDetailsProvider.getUserDetails();
    }

}
