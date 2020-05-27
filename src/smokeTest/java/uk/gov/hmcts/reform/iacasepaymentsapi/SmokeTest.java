package uk.gov.hmcts.reform.iacasepaymentsapi;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

public class SmokeTest {

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8096"
        );

    @Test
    public void should_prove_app_is_running_and_healthy() {

        RequestSpecification requestSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();

        Response response = given(requestSpecification)
            .when()
            .get("/health")
            .then()
            .extract().response();

        switch (response.getStatusCode()) {
            case 200:
                assertThat(response.getBody().asString()).contains("UP");
                break;
            case 503:
                throw new RestClientException(
                    response.getBody().asString(),
                    new HttpClientErrorException(HttpStatus.valueOf(response.getStatusCode()),
                        "One or more downstream services are unavailable."));
            default:
                throw new IllegalStateException("Issue with downstream services");
        }
    }
}
