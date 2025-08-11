package uk.gov.hmcts.reform.iacasedocumentsapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class AuthorizationHeadersProvider {

    @Value("${idam.redirectUrl}")
    protected String idamRedirectUrl;
    @Value("${idam.scope}")
    protected String userScope;
    @Value("${spring.security.oauth2.client.registration.oidc.client-id}")
    protected String idamClientId;
    @Value("${spring.security.oauth2.client.registration.oidc.client-secret}")
    protected String idamClientSecret;

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamAuthProvider idamAuthProvider;

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    private Headers getUserAuthorization(String username, String password, String userType) {

        MultiValueMap<String, String> tokenRequestForm = new LinkedMultiValueMap<>();
        tokenRequestForm.add("grant_type", "password");
        tokenRequestForm.add("redirect_uri", idamRedirectUrl);
        tokenRequestForm.add("client_id", idamClientId);
        tokenRequestForm.add("client_secret", idamClientSecret);
        tokenRequestForm.add("username", username);
        tokenRequestForm.add("password", password);
        tokenRequestForm.add("scope", userScope);

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = tokens.computeIfAbsent(
            userType,
            user -> "Bearer " + idamApi.token(tokenRequestForm).getAccessToken()
        );

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }

    public Headers getLegalRepresentativeAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD"),
            "LegalRepresentative"
        );
    }


    public Headers getLegalRepresentativeOrgSuccessAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_SUCCESS_PASSWORD"),
            "LegalRepresentativeOrgSuccess"
        );
    }

    public Headers getLegalRepresentativeOrgDeletedAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_LAW_FIRM_ORG_DELETED_USERNAME"),
            System.getenv("TEST_LAW_FIRM_ORG_DELETED_PASSWORD"),
            "LegalRepresentativeOrgDeleted"
        );
    }

    public Headers getCaseOfficerAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_CASEOFFICER_USERNAME"),
            System.getenv("TEST_CASEOFFICER_PASSWORD"),
            "CaseOfficer"
        );
    }

    public Headers getAdminOfficerAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_ADMINOFFICER_USERNAME"),
            System.getenv("TEST_ADMINOFFICER_PASSWORD"),
            "AdminOfficer"
        );
    }

    public Headers getHomeOfficeApcAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_HOMEOFFICE_APC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_APC_PASSWORD"),
            "HomeOfficeApc"
        );
    }

    public Headers getHomeOfficeLartAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_HOMEOFFICE_LART_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_LART_PASSWORD"),
            "HomeOfficeLart"
        );
    }

    public Headers getHomeOfficePouAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_HOMEOFFICE_POU_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_POU_PASSWORD"),
            "HomeOfficePou"
        );
    }

    public Headers getHomeOfficeGenericAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_HOMEOFFICE_GENERIC_USERNAME"),
            System.getenv("TEST_HOMEOFFICE_GENERIC_PASSWORD"),
            "HomeOfficeGeneric"
        );
    }

    public Headers getLegalRepresentativeOrgAAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_SHARE_CASE_A_PASSWORD"),
            "LegalRepresentativeOrgA"
        );
    }

    public Headers getJudgeAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_JUDGE_X_USERNAME"),
            System.getenv("TEST_JUDGE_X_PASSWORD"),
            "Judge"
        );
    }

    public Headers getCitizenAuthorization() {
        return getUserAuthorization(
            System.getenv("TEST_CITIZEN_USERNAME"),
            System.getenv("TEST_CITIZEN_PASSWORD"),
            "Citizen"
        );
    }

    public Headers getSystemAuthorization() {
        return getUserAuthorization(
            System.getenv("IA_SYSTEM_USERNAME"),
            System.getenv("IA_SYSTEM_PASSWORD"),
            "SystemUser"
        );
    }
}
