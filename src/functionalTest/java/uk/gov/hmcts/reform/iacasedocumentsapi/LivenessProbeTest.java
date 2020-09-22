package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class LivenessProbeTest {

    @Value("${targetInstance}") private String targetInstance;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_ping_liveness_endpoint_and_get_ok() {

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
