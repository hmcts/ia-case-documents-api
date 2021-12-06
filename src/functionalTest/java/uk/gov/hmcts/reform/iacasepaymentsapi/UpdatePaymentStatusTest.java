package uk.gov.hmcts.reform.iacasepaymentsapi;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentDto;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.ServiceTokenGeneratorConfiguration;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.CcdCaseCreationTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.util.FunctionalSpringContext;

@SpringBootTest(classes = {
    ServiceTokenGeneratorConfiguration.class,
    FunctionalSpringContext.class
})
@ActiveProfiles("functional")
public class UpdatePaymentStatusTest extends CcdCaseCreationTest {

    private RequestSpecification requestSpecification;

    protected final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:8096"
        );

    @BeforeEach
    public void setup() throws IOException {
        requestSpecification = new RequestSpecBuilder()
            .setBaseUri(targetInstance)
            .setRelaxedHTTPSValidation()
            .build();
    }

    @Test
    public void should_update_payment_status_successfully_with_status_code_200() {

        shouldPayAndSubmitAppeal();

        Response response = given(requestSpecification)
            .when()
            .contentType("application/json")
            .body(getPaymentRequest())
            .put("/payment-updates")
            .then()
            .extract().response();

        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void should_fail_on_invalid_payment_reference_with_status_code_400() {

        shouldPayAndSubmitAppeal();

        PaymentDto paymentDto = getPaymentRequest();
        paymentDto.setReference("RC-1111-2222-3333-4444");

        Response response = given(requestSpecification)
            .when()
            .contentType("application/json")
            .body(paymentDto)
            .put("/payment-updates")
            .then()
            .extract().response();

        assertEquals(response.getStatusCode(), 400);
    }

    private PaymentDto getPaymentRequest() {

        return PaymentDto.builder()
            .ccdCaseNumber(String.valueOf(getCaseId()))
            .reference(paymentReference)
            .status("Success")
            .service("IAC")
            .method("Card")
            .build();
    }
}
