package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class EvidenceManagementBundlingTest {

    private static final String ENDPOINT = "/api/stitch-ccd-bundles";
    private static final String CLIENT_REDIRECT_URI = "/api/stitch-ccd-bundles";

    private static final String ACCESS_TOKEN = "111";
    private static final String AUTHORIZATION_TOKEN = "222";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "em_api", consumer = "ia_case_documents_api")
    public RequestResponsePact executeGetEvidenceBundleIdAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);

        return builder
                .given("EM successfully returns OK Status")
                .uponReceiving("Provider receives a POST request from an IA Documents API")
                .path(ENDPOINT)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(new PactDslJsonBody()
                        .stringType("id", "12345"))
                .toPact();
    }

    @Pact(provider = "em_api", consumer = "ia_case_documents_api")
    public RequestResponsePact executeGetEvidenceBundleCaseDataAndGet200Response(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);

        return builder
                .given("EM successfully returns Bundle Case Data")
                .uponReceiving("Provider receives a POST request for bundle case data from an IA Documents API")
                .path(ENDPOINT)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createBundleCaseDataArrayResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetEvidenceBundleIdAndGet200Response")
    public void should_post_to_Evidence_bundle_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "bundleCaseData");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(ContentType.URLENC)
                        .formParams(body)
                        .when()
                        .post(mockServer.getUrl() + ENDPOINT)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .asString();

        assertThat(actualResponseBody).isNotNull();

        JSONObject response = new JSONObject(actualResponseBody);
        assertThat(response.get("id").toString()).isNotBlank();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetEvidenceBundleCaseDataAndGet200Response")
    public void should_post_to_Evidence_bundleCaseData_and_receive_code_with_200_response(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.put("ServiceAuthorization", AUTHORIZATION_TOKEN);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "bundleCaseData");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(ContentType.URLENC)
                        .formParams(body)
                        .when()
                        .post(mockServer.getUrl() + ENDPOINT)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .asString();

        assertThat(actualResponseBody).isNotNull();

        JSONArray responseArray = new JSONArray(actualResponseBody);
        JSONObject response = new JSONObject(String.valueOf(responseArray.get(0)));
        assertThat(response.get("id").toString()).isNotBlank();
        assertThat(response.get("title").toString()).isNotBlank();
        assertThat(response.get("description").toString()).isNotBlank();
        assertThat(response.get("eligibleForStitching").toString()).isNotBlank();
        assertThat(response.get("hasCoversheets").toString()).isNotBlank();
        assertThat(response.get("hasTableOfContents").toString()).isNotBlank();
        assertThat(response.get("filename").toString()).isNotBlank();
    }

    private PactDslJsonArray createBundleCaseDataArrayResponse() {

        return new PactDslJsonArray().object()
                .stringValue("id", "123")
                .stringValue("title", "some-bundle-title1")
                .stringValue("description", "some-bundle-description")
                .stringValue("eligibleForStitching", "yes")
                .stringValue("hasCoversheets", "yes")
                .stringValue("hasTableOfContents", "yes")
                .stringValue("filename", "Gonzlez-decision-and-reasons-draft")
                .close().object()
                .stringValue("id", "124")
                .stringValue("title", "some-bundle-title2")
                .stringValue("description", "some-bundle-description2")
                .stringValue("eligibleForStitching", "no")
                .stringValue("hasCoversheets", "no")
                .stringValue("hasTableOfContents", "no")
                .stringValue("filename", "Gonzlez-decision-and-reasons-draft2")
                .close().array();
    }

}
