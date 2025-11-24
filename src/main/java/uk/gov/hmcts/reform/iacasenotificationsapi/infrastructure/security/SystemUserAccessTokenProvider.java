package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security;

import feign.FeignException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security.idam.IdentityManagerResponseException;

@Slf4j
@Service
@Qualifier("systemUser")
public class SystemUserAccessTokenProvider implements AccessTokenProvider {

    private final IdamApi idamApi;
    private final String systemUsername;
    private final String systemPassword;
    private final String systemUserScope;
    private final String idamRedirectUrl;
    private final String idamClientId;
    private final String idamClientSecret;

    public SystemUserAccessTokenProvider(
        IdamApi idamApi,
        @Value("${ia_system_user}") String systemUsername,
        @Value("${ia_system_user_password}") String systemPassword,
        @Value("${ia_system_user_scope}") String systemUserScope,
        @Value("${idam.redirectUrl}") String idamRedirectUrl,
        @Value("${spring.security.oauth2.client.registration.oidc.client-id}") String idamClientId,
        @Value("${spring.security.oauth2.client.registration.oidc.client-secret}") String idamClientSecret
    ) {
        this.idamApi = idamApi;
        this.systemUsername = systemUsername;
        this.systemPassword = systemPassword;
        this.systemUserScope = systemUserScope;
        this.idamRedirectUrl = idamRedirectUrl;
        this.idamClientId = idamClientId;
        this.idamClientSecret = idamClientSecret;
    }

    @Override
    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("System user access token could not be retrieved"));
    }

    @Override
    @Cacheable(value = "systemUserTokenCache")
    public Optional<String> tryGetAccessToken() {
        try {
            log.debug("Fetching system user access token from IDAM");
            
            MultiValueMap<String, String> tokenRequest = new LinkedMultiValueMap<>();
            tokenRequest.add("grant_type", "password");
            tokenRequest.add("redirect_uri", idamRedirectUrl);
            tokenRequest.add("client_id", idamClientId);
            tokenRequest.add("client_secret", idamClientSecret);
            tokenRequest.add("username", systemUsername);
            tokenRequest.add("password", systemPassword);
            tokenRequest.add("scope", systemUserScope);

            Token tokenResponse = idamApi.token(tokenRequest);
            
            if (tokenResponse == null || tokenResponse.getAccessToken() == null || tokenResponse.getAccessToken().trim().isEmpty()) {
                log.error("IDAM returned null or empty access token");
                throw new IdentityManagerResponseException(
                    "Could not get system user token from IDAM",
                    new IllegalStateException("Access token is null or empty")
                );
            }
            
            String accessToken = "Bearer " + tokenResponse.getAccessToken();
            
            log.debug("Successfully retrieved system user access token");
            return Optional.of(accessToken);
            
        } catch (FeignException ex) {
            log.error("Failed to get system user token from IDAM", ex);
            throw new IdentityManagerResponseException(
                "Could not get system user token from IDAM",
                ex
            );
        }
    }
}

