package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

@Component("requestUser")
public class IdamUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final IdamApi idamApi;

    public IdamUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        IdamApi idamApi
    ) {

        this.accessTokenProvider = accessTokenProvider;
        this.idamApi = idamApi;
    }

    public IdamUserDetails getUserDetails() {

        String accessToken = accessTokenProvider.getAccessToken();

        UserInfo response;

        try {

            response = idamApi.userInfo(accessToken);

        } catch (FeignException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        if (response.getUid() == null) {
            throw new IllegalStateException("IDAM user details missing 'uid' field");
        }

        if (response.getRoles() == null) {
            throw new IllegalStateException("IDAM user details missing 'roles' field");
        }

        if (response.getEmail() == null) {
            throw new IllegalStateException("IDAM user details missing 'sub' field");
        }

        if (response.getGivenName() == null) {
            throw new IllegalStateException("IDAM user details missing 'given_name' field");
        }

        if (response.getFamilyName() == null) {
            throw new IllegalStateException("IDAM user details missing 'family_name' field");
        }

        return new IdamUserDetails(
            accessToken,
            response.getUid(),
            response.getRoles(),
            response.getEmail(),
            response.getGivenName(),
            response.getFamilyName()
        );
    }
}
