package uk.gov.hmcts.reform.iacasepaymentsapi.consumer.idam;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.RestTemplateConfiguration;

@ExtendWith(SpringExtension.class)
@ExtendWith(PactConsumerTestExt.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactFolder("pacts")
@PactTestFor(providerName = "idamApi_oidc", port = "5000")
@ContextConfiguration(classes = {IdamApiConsumerApplication.class})
@TestPropertySource(
    properties = {"idam.baseUrl=localhost:5000"}
)
@Import(RestTemplateConfiguration.class)
public class IdamApiConsumerTest {
    @Autowired
    IdamApi idamApi;
    private static final String AUTH_TOKEN = "Bearer someAuthorizationToken";

    @Pact(provider = "idamApi_oidc", consumer = "ia_casePaymentsApi")
    public V4Pact generatePactFragmentUser(PactDslWithProvider builder) {
        return builder
            .given("userinfo is requested")
            .uponReceiving("a request for a user")
            .path("/o/userinfo")
            .method("GET")
            .matchHeader(HttpHeaders.AUTHORIZATION, AUTH_TOKEN)
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .body(createUserDetailsResponse())
            .toPact(V4Pact.class);

    }

    @Pact(provider = "idamApi_oidc", consumer = "ia_casePaymentsApi")
    public V4Pact generatePactFragmentToken(PactDslWithProvider builder) throws JSONException {
        Map<String, String> responseheaders = ImmutableMap.<String, String>builder()
            .put("Content-Type", "application/json")
            .build();
        return builder
            .given("a token is requested")
            .uponReceiving("Provider receives a POST /o/token request from an IA API")
            .path("/o/token")
            .method(HttpMethod.POST.toString())
            .body("redirect_uri=http%3A%2F%2Fwww.dummy-pact-service.com%2Fcallback"
                      + "&client_id=pact&grant_type=password"
                      + "&username=ia-caseofficer@fake.hmcts.net"
                      + "&password=London01"
                      + "&client_secret=pactsecret"
                      + "&scope=openid profile roles",
                  "application/x-www-form-urlencoded")
            .willRespondWith()
            .status(org.springframework.http.HttpStatus.OK.value())
            .headers(responseheaders)
            .body(createAuthResponse())
            .toPact(V4Pact.class);
    }

    private PactDslJsonBody createUserDetailsResponse() {
        return new PactDslJsonBody()
            .stringType("uid", "1111-2222-3333-4567")
            .stringValue("sub", "ia-caseofficer@fake.hmcts.net")
            .stringValue("givenName", "Case")
            .stringValue("familyName", "Officer")
            .minArrayLike("roles", 1, PactDslJsonRootValue.stringType("caseworker-ia-legalrep-solicitor"), 1)
            .stringType("IDAM_ADMIN_USER");
    }

    private PactDslJsonBody createAuthResponse() {
        return new PactDslJsonBody()
            .stringType("access_token", "eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre")
            .stringType("scope", "openid roles profile");

    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentUser")
    public void verifyPactResponse() {
        UserInfo userInfo = idamApi.userInfo(AUTH_TOKEN);
        Assertions.assertEquals("ia-caseofficer@fake.hmcts.net", userInfo.getEmail());
    }

    @Test
    @PactTestFor(pactMethod = "generatePactFragmentToken")
    public void verifyIdamUserDetailsRolesPactToken() {

        MultiValueMap<String, String> tokenRequestMap = buildTokenRequestMap();
        Token token = idamApi.token(tokenRequestMap);
        Assertions.assertEquals("eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdjEre", token.getAccessToken(),
                                "Token is not expected");
    }

    private MultiValueMap<String, String> buildTokenRequestMap() {
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("redirect_uri", "http://www.dummy-pact-service.com/callback");
        map.add("client_id", "pact");
        map.add("client_secret", "pactsecret");
        map.add("username", "ia-caseofficer@fake.hmcts.net");
        map.add("password", "London01");
        map.add("scope", "openid profile roles");
        return map;
    }
}
