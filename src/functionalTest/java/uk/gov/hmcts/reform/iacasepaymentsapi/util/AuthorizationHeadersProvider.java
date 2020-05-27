package uk.gov.hmcts.reform.iacasepaymentsapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@Service
public class AuthorizationHeadersProvider {

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    @Autowired
    private IdamAuthProvider idamAuthProvider;

    public Headers getLegalRepresentativeAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();
        String accessToken = idamAuthProvider.getUserToken(
            System.getenv("TEST_LAW_FIRM_A_USERNAME"),
            System.getenv("TEST_LAW_FIRM_A_PASSWORD")
        );

        return new Headers(
            new Header("Authorization", accessToken),
            new Header("ServiceAuthorization", serviceToken)
        );

    }
}
