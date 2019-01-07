package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenDecoder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.SystemUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.UserCredentialsProvider;

@Configuration
public class CredentialsProviderConfiguration {

    @Bean("requestUser")
    @Primary
    public UserCredentialsProvider getRequestUserCredentialsProvider(
        RequestUserAccessTokenProvider requestUserAccessTokenProvider,
        AccessTokenDecoder accessTokenDecoder
    ) {
        return new UserCredentialsProvider(
            requestUserAccessTokenProvider,
            accessTokenDecoder
        );
    }

    @Bean("systemUser")
    public UserCredentialsProvider getSystemUserCredentialsProvider(
        SystemUserAccessTokenProvider systemUserAccessTokenProvider,
        AccessTokenDecoder accessTokenDecoder
    ) {
        return new UserCredentialsProvider(
            systemUserAccessTokenProvider,
            accessTokenDecoder
        );
    }
}
