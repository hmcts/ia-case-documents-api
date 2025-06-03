package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RetryableException;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.fixtures.Fixture;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.RequestUserAccessTokenProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.util.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.verifiers.Verifier;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CcdScenarioRunnerTest {

    @Value("${targetInstance}")
    private String targetInstance;
    @Autowired
    FeatureToggler featureToggler;
    @Autowired
    private Environment environment;
    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;
    @Autowired
    private MapValueExpander mapValueExpander;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private List<Fixture> fixtures;
    @Autowired
    private List<Verifier> verifiers;
    private boolean haveAllPassed = true;
    private final ArrayList<String> failedScenarios = new ArrayList<>();
    @MockBean
    RequestUserAccessTokenProvider requestUserAccessTokenProvider;

    @BeforeEach
    void authenticateMe() {
        String accessToken = authorizationHeadersProvider.getCaseOfficerAuthorization().getValue("Authorization");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(accessToken);
        when(requestUserAccessTokenProvider.getAccessToken()).thenReturn(accessToken);
    }

    @BeforeEach
    public void setup() {
        MapSerializer.setObjectMapper(objectMapper);
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void scenarios_should_behave_as_specified() throws IOException {

        loadPropertiesIntoMapValueExpander();

        for (Fixture fixture : fixtures) {
            fixture.prepare();
        }

        assertFalse(
            "Verifiers are configured",
            verifiers.isEmpty()
        );

        String scenarioPattern = System.getProperty("scenario");
        if (scenarioPattern == null) {
            scenarioPattern = "*.json";
        } else {
            scenarioPattern = "*" + scenarioPattern + "*.json";
        }

        Collection<String> scenarioSources = new ArrayList<>();
        scenarioSources.addAll(StringResourceLoader.load("/scenarios/" + scenarioPattern).values());
        scenarioSources.addAll(StringResourceLoader.load("/scenarios/bail/" + scenarioPattern).values());
        scenarioSources.addAll(StringResourceLoader.load("/scenarios/payments/" + scenarioPattern).values());

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");

        List<String> runScenarios = new ArrayList<>();
        int maxRetries = 3;
        for (String scenarioSource : scenarioSources) {

            for (int i = 0; i < maxRetries; i++) {

                String description = "";
                try {
                    Map<String, Object> scenario = deserializeWithExpandedValues(scenarioSource);

                    description = MapValueExtractor.extract(scenario, "description");

                    Object scenarioEnabled = MapValueExtractor.extract(scenario, "enabled");
                    boolean scenarioEnabledFlag = true;
                    if (scenarioEnabled instanceof Boolean) {
                        scenarioEnabledFlag = (Boolean) scenarioEnabled;
                    } else if (scenarioEnabled instanceof String) {
                        scenarioEnabledFlag = Boolean.parseBoolean((String) scenarioEnabled);
                    }

                    Object scenarioDisabled = MapValueExtractor.extract(scenario, "disabled");
                    boolean scenarioDisabledFlag = false;
                    if (scenarioDisabled instanceof String) {
                        scenarioDisabledFlag = Boolean.parseBoolean((String) scenarioDisabled);
                    }

                    if (!scenarioEnabledFlag || scenarioDisabledFlag) {
                        System.out.println((char) 27 + "[31m" + "SCENARIO: " + description + " **disabled**");
                        continue;
                    }

                    System.out.println((char) 27 + "[33m" + "SCENARIO: " + description);

                    Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

                    final long testCaseId = MapValueExtractor.extractOrDefault(
                        scenario,
                        "request.input.id",
                        ThreadLocalRandom.current().nextInt(1, 9999999 + 1)

                    );

                    final String requestBody = buildCallbackBody(
                        testCaseId,
                        MapValueExtractor.extract(scenario, "request.input"),
                        templatesByFilename
                    );

                    final Headers authorizationHeaders = getAuthorizationHeaders(scenario);
                    final String requestUri = MapValueExtractor.extract(scenario, "request.uri");
                    final int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);

                    String actualResponseBody =
                        SerenityRest
                            .given()
                            .headers(authorizationHeaders)
                            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                            .body(requestBody)
                            .when()
                            .post(requestUri)
                            .peek()
                            .then()
                            .statusCode(expectedStatus)
                            .and()
                            .extract()
                            .body()
                            .asString();

                    String expectedResponseBody = buildCallbackResponseBody(
                        MapValueExtractor.extract(scenario, "expectation"),
                        templatesByFilename
                    );

                    Map<String, Object> actualResponse = MapSerializer.deserialize(actualResponseBody);
                    Map<String, Object> expectedResponse = MapSerializer.deserialize(expectedResponseBody);

                    verifiers.forEach(verifier ->
                                          verifier.verify(
                                              testCaseId,
                                              scenario,
                                              expectedResponse,
                                              actualResponse
                                          )
                    );
                    runScenarios.add(description);
                    break;
                } catch (Error | RetryableException e) {
                    System.out.println("Scenario failed with error " + e.getMessage());
                    if (i == maxRetries - 1) {
                        this.failedScenarios.add(description);
                        this.haveAllPassed = false;
                    }
                }
            }
        }

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        List<String> scenariosThatHaveRun = runScenarios.stream().distinct().toList();
        System.out.println((char) 27 + "[" + scenariosThatHaveRun.size() + " SCENARIOS HAVE RUN]");
        System.out.println(String.join(";\n", scenariosThatHaveRun));
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        if (!haveAllPassed) {
            throw new AssertionError("Not all scenarios passed.\nFailed scenarios are:\n" + failedScenarios.stream().map(
                Object::toString).collect(Collectors.joining(";\n")));
        }
    }

    private void loadPropertiesIntoMapValueExpander() {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport
            .stream(propertySources.spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(propertySource -> ((EnumerablePropertySource<?>) propertySource).getPropertyNames())
            .flatMap(Arrays::stream)
            .forEach(name -> MapValueExpander.ENVIRONMENT_PROPERTIES.setProperty(name, environment.getProperty(name)));
    }

    private Map<String, Object> deserializeWithExpandedValues(
        String source
    ) throws IOException {
        Map<String, Object> data = MapSerializer.deserialize(source);
        mapValueExpander.expandValues(data);
        return data;
    }

    private Map<String, Object> buildCaseData(
        Map<String, Object> caseDataInput,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        Map<String, Object> caseData = deserializeWithExpandedValues(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        return caseData;
    }

    private String buildCallbackBody(
        long testCaseId,
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {

        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            templatesByFilename
        );

        LocalDateTime createdDate =
            LocalDateTime.parse(
                MapValueExtractor.extractOrDefault(input, "createdDate", LocalDateTime.now().toString())
            );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", testCaseId);
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
        caseDetails.put("state", MapValueExtractor.extractOrThrow(input, "state"));
        caseDetails.put("created_date", createdDate);
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", MapValueExtractor.extractOrThrow(input, "eventId"));
        callback.put("case_details", caseDetails);

        if (input.containsKey("caseDataBefore")) {
            Map<String, Object> caseDataBefore = buildCaseData(
                MapValueExtractor.extract(input, "caseDataBefore"),
                templatesByFilename
            );

            Map<String, Object> caseDetailsBefore = new HashMap<>();
            caseDetailsBefore.put("id", testCaseId);
            caseDetailsBefore.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
            caseDetailsBefore.put("state", MapValueExtractor.extractOrThrow(input, "state"));
            caseDetailsBefore.put("created_date", createdDate);
            caseDetailsBefore.put("case_data", caseDataBefore);
            callback.put("case_details_before", caseDetailsBefore);
        }

        return MapSerializer.serialize(callback);
    }

    private String buildCallbackResponseBody(
        Map<String, Object> expectation,
        Map<String, String> templatesByFilename
    ) throws IOException {

        if (MapValueExtractor.extract(expectation, "confirmation") != null) {

            final Map<String, Object> callbackResponse = new HashMap<>();

            callbackResponse.put("confirmation_header", MapValueExtractor.extract(expectation, "confirmation.header"));
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(expectation, "confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            Map<String, Object> caseData = buildCaseData(
                MapValueExtractor.extract(expectation, "caseData"),
                templatesByFilename
            );

            PreSubmitCallbackResponse<AsylumCase> preSubmitCallbackResponse =
                new PreSubmitCallbackResponse<>(
                    objectMapper.readValue(
                        MapSerializer.serialize(caseData),
                        new TypeReference<>() {
                        }
                    )
                );

            preSubmitCallbackResponse.addErrors(MapValueExtractor.extract(expectation, "errors"));

            return objectMapper.writeValueAsString(preSubmitCallbackResponse);
        }
    }

    private Headers getAuthorizationHeaders(Map<String, Object> scenario) {
        String credentials = Optional.ofNullable(MapValueExtractor.extract(scenario, "request.credentials"))
            .map(Object::toString)
            .orElse("None");

        return switch (credentials) {
            case "LegalRepresentative" -> authorizationHeadersProvider.getLegalRepresentativeAuthorization();
            case "LegalRepresentativeOrgSuccess" -> authorizationHeadersProvider.getLegalRepresentativeOrgSuccessAuthorization();
            case "LegalRepresentativeOrgDeleted" -> authorizationHeadersProvider.getLegalRepresentativeOrgDeletedAuthorization();
            case "CaseOfficer" -> authorizationHeadersProvider.getCaseOfficerAuthorization();
            case "AdminOfficer" -> authorizationHeadersProvider.getAdminOfficerAuthorization();
            case "Citizen" -> authorizationHeadersProvider.getCitizenAuthorization();
            case "Judge" -> authorizationHeadersProvider.getJudgeAuthorization();
            case "System" -> authorizationHeadersProvider.getSystemAuthorization();
            case "HomeOfficeLart" -> authorizationHeadersProvider.getHomeOfficeLartAuthorization();
            case "HomeOfficePOU" -> authorizationHeadersProvider.getHomeOfficePouAuthorization();
            default -> new Headers();
        };
    }
}
