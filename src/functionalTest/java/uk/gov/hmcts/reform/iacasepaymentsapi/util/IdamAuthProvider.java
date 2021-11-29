package uk.gov.hmcts.reform.iacasepaymentsapi.util;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2.IdentityManagerResponseException;

@Service
public class IdamAuthProvider {

    @Value("${idam.redirectUrl}")
    protected String idamRedirectUri;

    protected String userScope = "openid profile roles";

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;

    @Autowired private IdamApi idamApi;

    public String getUserToken(String username, String password) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", idamRedirectUri);
        map.add("client_id", idamClientId);
        map.add("client_secret", idamClientSecret);
        map.add("username", username);
        map.add("password", password);
        map.add("scope", userScope);

        try {

            Token tokenResponse = idamApi.token(map);
            return "Bearer " + tokenResponse.getAccessToken();
        } catch (FeignException ex) {

            throw new IdentityManagerResponseException("Could not get system user token from IDAM", ex);
        }

    }

    public String getSystemUserToken() {

        return getUserToken(
            System.getenv("IA_SYSTEM_USERNAME"),
            System.getenv("IA_SYSTEM_PASSWORD")
        );
    }

    public String getLegalRepToken() {

        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD")
        );
    }

    public String getUserId(String token) {

        try {

            UserInfo userInfo = idamApi.userInfo(token);

            return userInfo.getUid();

        } catch (FeignException ex) {

            throw new IdentityManagerResponseException("Could not get system user token from IDAM", ex);
        }
    }
}
