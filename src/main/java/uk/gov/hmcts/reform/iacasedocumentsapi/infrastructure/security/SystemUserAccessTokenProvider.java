package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import feign.FeignException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdentityManagerResponseException;

@Slf4j
@Service
@Qualifier("systemUser")
public class SystemUserAccessTokenProvider implements AccessTokenProvider {

    private final IdamService idamService;

    public SystemUserAccessTokenProvider(IdamService idamService) {
        this.idamService = idamService;
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

            Token tokenResponse = idamService.getServiceUserToken();

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

