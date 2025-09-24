package uk.gov.hmcts.reform.iacasepaymentsapi.util;

import feign.FeignException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2.IdentityManagerResponseException;

@Service
public class IdamAuthProvider {

    @Value("${idam.redirectUrl}")
    protected String idamRedirectUri;

    @Value("${idam.scope}") 
    protected String userScope;

    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;

    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;

    @Autowired private IdamApi idamApi;
    @Autowired private IdamService idamService;

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

    @Cacheable(value = "legalRepTokenCache")
    public String getLegalRepToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepOrgDeletedTokenCache")
    public String getLegalRepOrgDeletedToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_DELETED_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_DELETED_PASSWORD")
        );
    }

    @Cacheable(value = "legalRepOrgSuccessTokenCache")
    public String getLegalRepOrgSuccessToken() {
        return getUserToken(
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD")
        );
    }

    @Cacheable(value = "citizenTokenCache")
    public String getCitizenToken() {
        return getUserToken(
            System.getenv("TEST_CITIZEN_USERNAME"),
            System.getenv("TEST_CITIZEN_PASSWORD")
        );
    }
}
