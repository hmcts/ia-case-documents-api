package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
class LivenessProbeTest {

    @Value("${targetInstance}") private String targetInstance;

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    void should_ping_liveness_endpoint_and_get_ok() {

        String response = SerenityRest
            .given()
            .when()
            .get("/health/liveness")
            .then()
            .statusCode(HttpStatus.OK.value())
            .log().all()
            .and()
            .extract().body()
            .jsonPath().get("status");

        assertThat(response).isEqualTo("UP");

    }

}
