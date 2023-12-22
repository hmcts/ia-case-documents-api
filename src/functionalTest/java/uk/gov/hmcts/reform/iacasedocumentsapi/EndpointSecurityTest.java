package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
 class EndpointSecurityTest {

    @Value("${targetInstance}") private String targetInstance;

    private final List<String> callbackEndpoints =
        Arrays.asList(
            "/asylum/ccdAboutToStart",
            "/asylum/ccdAboutToSubmit"
        );

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @BeforeEach
     void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
     void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {

        String response =
            SerenityRest
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract().body().asString();

        assertThat(response)
            .contains("Welcome");
    }

    @Test
     void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        String response =
            SerenityRest
                .given()
                .when()
                .get("/health")
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .log().all(true)
                .extract().body().asString();

        assertThat(response)
            .contains("UP");
    }

    @Test
     void should_not_allow_unauthenticated_requests_and_return_401_response_code() {

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .body(someCallback())
                .when()
                .post(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
        );
    }

    @Test
     void should_not_allow_requests_without_valid_service_authorisation_and_return_401_response_code() {

        String invalidServiceToken = "invalid";

        String accessToken =
            authorizationHeadersProvider
                .getCaseOfficerAuthorization()
                .getValue("Authorization");

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("ServiceAuthorization", invalidServiceToken)
                .header("Authorization", accessToken)
                .body(someCallback())
                .when()
                .post(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
     void should_not_allow_requests_without_valid_user_authorisation_and_return_401_response_code() {

        String serviceToken =
            authorizationHeadersProvider
                .getCaseOfficerAuthorization()
                .getValue("ServiceAuthorization");

        String invalidAccessToken = "invalid";

        callbackEndpoints.forEach(callbackEndpoint ->

            SerenityRest
                .given()
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("ServiceAuthorization", serviceToken)
                .header("Authorization", invalidAccessToken)
                .body(someCallback())
                .when()
                .post(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value()));
    }

    private Callback<AsylumCase> someCallback() {
        return new Callback<>(
            new CaseDetails<>(1, "IA", State.APPEAL_STARTED, new AsylumCase(), LocalDateTime.now()),
            Optional.empty(),
            Event.SUBMIT_APPEAL
        );
    }
}
