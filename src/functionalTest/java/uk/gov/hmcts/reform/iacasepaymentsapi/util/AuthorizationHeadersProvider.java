package uk.gov.hmcts.reform.iacasepaymentsapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class AuthorizationHeadersProvider {
    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamAuthProvider idamAuthProvider;

    private final Map<String, String> tokens = new ConcurrentHashMap<>();

    public Headers getLegalRepresentativeAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getLegalRepToken();

        return new Headers(
            new Header("Authorization", accessToken),
            new Header("ServiceAuthorization", serviceToken)
        );
    }

    public Headers getLegalRepresentativeOrgSuccessAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getLegalRepOrgSuccessToken();

        return new Headers(
            new Header("Authorization", accessToken),
            new Header("ServiceAuthorization", serviceToken)
        );
    }

    public Headers getLegalRepresentativeOrgDeletedAuthorization() {

        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getLegalRepOrgDeletedToken();

        return new Headers(
            new Header("Authorization", accessToken),
            new Header("ServiceAuthorization", serviceToken)
        );
    }

    public Headers getCitizenAuthorization() {
        String serviceToken = tokens.computeIfAbsent("ServiceAuth", user -> serviceAuthTokenGenerator.generate());
        String accessToken = idamAuthProvider.getCitizenToken();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken),
            new Header("Authorization", accessToken)
        );
    }
}
