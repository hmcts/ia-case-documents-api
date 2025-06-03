package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.io.IOException;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class EvidenceManagementBundlingTest {

    private static final String ENDPOINT = "/api/stitch-ccd-bundles";
    private static final String CLIENT_REDIRECT_URI = "/api/stitch-ccd-bundles";
    private static final String DOC_QUERY_STRING = "document1=doc1&filename1=file1&document2=doc2&filename2=file2";

    private static final String ACCESS_TOKEN = "111";
    private static final String AUTHORIZATION_TOKEN = "222";

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Pact(provider = "em_api", consumer = "ia_caseDocumentsApi")
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
                .toPact(RequestResponsePact.class);
    }

    @Pact(provider = "em_api", consumer = "ia_caseDocumentsApi")
    public RequestResponsePact executeGetEvidenceBundleCaseDataAndGetNotNullValues(PactDslWithProvider builder) {

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
                .toPact(RequestResponsePact.class);
    }

    @Pact(provider = "em_api", consumer = "ia_caseDocumentsApi")
    public RequestResponsePact executeGetEvidenceBundleCaseDataAndGetCorrectType(PactDslWithProvider builder) {

        return builder
                .given("EM successfully returns Bundle Case Data")
                .uponReceiving("Provider receives a GET request for BundleCase data with correct type from an IA Documents API")
                .path(ENDPOINT)
                .method(HttpMethod.GET.toString())
                .query(DOC_QUERY_STRING)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createBundleCaseDataArrayResponse())
                .toPact(RequestResponsePact.class);
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
    @PactTestFor(pactMethod = "executeGetEvidenceBundleCaseDataAndGetNotNullValues")
    public void should_post_to_Evidence_bundleCaseData_and_receive_not_null_values(MockServer mockServer) throws JSONException {

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

    @Test
    @PactTestFor(pactMethod = "executeGetEvidenceBundleCaseDataAndGetCorrectType")
    public void should_get_Evidence_bundleCaseData_and_receive_correct_type_values(MockServer mockServer) throws JSONException, IOException {

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("response_type", "bundleCaseData");
        body.add("redirect_uri", CLIENT_REDIRECT_URI);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .log().all(true)
                        .when()
                        .get(mockServer.getUrl() + ENDPOINT + "?" + DOC_QUERY_STRING)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract().body()
                        .asString();

        assertThat(actualResponseBody).isNotNull();

        JSONArray responseArray = new JSONArray(actualResponseBody);
        JSONObject response = new JSONObject(String.valueOf(responseArray.get(0)));

        Bundle bundle = objectMapper.readValue(response.toString(), Bundle.class);

        assertThat(bundle).isNotNull();
        assertThat(bundle.getId()).isEqualTo("123");
        assertThat(bundle.getHasCoversheets().equals(YesOrNo.YES));
        assertThat(bundle.getHasTableOfContents().equals(YesOrNo.YES));
        assertThat(bundle.getTitle()).isEqualTo("some-bundle-title1");
        assertThat(bundle.getDescription()).isEqualTo("some-bundle-description1");
        assertThat(bundle.getFilename()).isEqualTo("Gonzlez-decision-and-reasons-draft");
    }

    private PactDslJsonArray createBundleCaseDataArrayResponse() {

        return new PactDslJsonArray().object()
                .stringValue("id", "123")
                .stringValue("title", "some-bundle-title1")
                .stringValue("description", "some-bundle-description1")
                .stringValue("eligibleForStitching", "Yes")
                .stringValue("hasCoversheets", "Yes")
                .stringValue("hasTableOfContents", "Yes")
                .stringValue("filename", "Gonzlez-decision-and-reasons-draft")
                .close().object()
                .stringValue("id", "124")
                .stringValue("title", "some-bundle-title2")
                .stringValue("description", "some-bundle-description2")
                .stringValue("eligibleForStitching", "No")
                .stringValue("hasCoversheets", "No")
                .stringValue("hasTableOfContents", "No")
                .stringValue("filename", "Gonzlez-decision-and-reasons-draft2")
                .close().array();
    }

}
