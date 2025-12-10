package uk.gov.hmcts.reform.iacasedocumentsapi;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.http.Headers;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
    @Autowired
    private LaunchDarklyFunctionalTestClient launchDarklyFunctionalTestClient;
    @MockBean
    RequestUserAccessTokenProvider requestUserAccessTokenProvider;

    private List<String> runScenarios;

    @BeforeAll
    public void init() {
        String accessToken = authorizationHeadersProvider.getCaseOfficerAuthorization().getValue("Authorization");
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(accessToken);
        when(requestUserAccessTokenProvider.getAccessToken()).thenReturn(accessToken);
        MapSerializer.setObjectMapper(objectMapper);
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        runScenarios = new ArrayList<>();
    }

    @AfterAll
    public void teardown() {
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        List<String> scenariosThatHaveRun = runScenarios.stream().distinct().toList();
        System.out.println((char) 27 + "[" + scenariosThatHaveRun.size() + " SCENARIOS HAVE RUN]");
        System.out.println(String.join(";\n", scenariosThatHaveRun));
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        if (!haveAllPassed) {
            System.out.println("Not all scenarios passed.\nFailed scenarios are:\n" + failedScenarios.stream().map(
                Object::toString).collect(Collectors.joining(";\n")));
        }
    }

    @ParameterizedTest
    @MethodSource("scenarioSourcesProvider")
    public void scenarios_should_behave_as_specified(String filename, Map<String, Object> scenario) throws IOException {
        String description = MapValueExtractor.extractOrDefault(scenario, "description", "No description provided");

        if (!isScenarioEnabled(scenario) || isScenarioDisabled(scenario)) {
            System.out.println((char) 27 + "[31m" + "SCENARIO: " + description + " **disabled**");
            return;
        }

        Map<String, String> templatesByFilename = StringResourceLoader.load("/templates/*.json");

        final long testCaseId = getTestId(scenario);

        final String requestUri = MapValueExtractor.extract(scenario, "request.uri");

        boolean isAsylumCase = requestUri.contains("asylum");
        final String requestBody = buildCallbackBody(
            testCaseId,
            MapValueExtractor.extract(scenario, "request.input"),
            isAsylumCase,
            templatesByFilename
        );

        int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);

        String expectedResponseBody = buildCallbackResponseBody(
            MapValueExtractor.extract(scenario, "expectation"),
            isAsylumCase,
            templatesByFilename
        );

        Map<String, Object> expectedResponse = MapSerializer.deserialize(expectedResponseBody);

        int maxRetries = 1;
        for (int i = 0; i < maxRetries; i++) {
            try {
                System.out.println((char) 27 + "[33m" + "Attempt " + (i + 1) + "SCENARIO: " + description);
                final Headers authorizationHeaders = getAuthorizationHeaders(scenario);
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
                        .log().ifError()
                        .log().ifValidationFails()
                        .statusCode(expectedStatus)
                        .and()
                        .extract()
                        .body()
                        .asString();

                Map<String, Object> actualResponse = MapSerializer.deserialize(actualResponseBody);

                verifiers.forEach(verifier ->
                                      verifier.verify(
                                          testCaseId,
                                          isAsylumCase,
                                          scenario,
                                          expectedResponse,
                                          actualResponse
                                      )
                );
                runScenarios.add(filename);
                break;
            } catch (Error | Exception e) {
                System.out.println(description + " Scenario failed with error " + e.getMessage());
                if (i == maxRetries - 1) {
                    failedScenarios.add(filename);
                    haveAllPassed = false;
                    throw e;
                }
            }
        }
    }

    private void loadPropertiesIntoMapValueExpander() {

        MutablePropertySources propertySources = ((AbstractEnvironment) environment).getPropertySources();
        StreamSupport
            .stream(propertySources.spliterator(), false)
            .filter(propertySource -> propertySource instanceof EnumerablePropertySource)
            .map(propertySource -> ((EnumerablePropertySource<?>) propertySource).getPropertyNames())
            .flatMap(Arrays::stream)
            .forEach(name -> MapValueExpander.ENVIRONMENT_PROPERTIES.setProperty(
                name,
                environment.getProperty(name)
            ));
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
        String state,
        String eventId,
        boolean isAsylumCase,
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        Map<String, Object> caseData = deserializeWithExpandedValues(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }

        if (isAsylumCase) {

            if (caseData.containsKey("detentionFacility")) {
                caseData.putIfAbsent("ircName", "Brookhouse");
            }

            List<String> unlistedStates = List.of(
                "appealStarted",
                "appealStartedByAdmin",
                "appealSubmitted",
                "appealSubmittedOutOfTime",
                "pendingPayment",
                "awaitingRespondentEvidence",
                "caseBuilding",
                "caseUnderReview",
                "respondentReview",
                "submitHearingRequirements",
                "awaitingReasonsForAppeal",
                "reasonsForAppealSubmitted"
            );
            String stateBeforeEndAppeal;
            if (caseData.containsKey("stateBeforeEndAppeal")) {
                stateBeforeEndAppeal = caseData.get("stateBeforeEndAppeal").toString();
            } else {
                stateBeforeEndAppeal = state;
            }
            if (stateBeforeEndAppeal != null && !stateBeforeEndAppeal.equals("*")) {
                if (!unlistedStates.contains(stateBeforeEndAppeal)) {
                    caseData.putIfAbsent(
                        "listCaseHearingCentre",
                        caseData.getOrDefault("hearingCentre", "taylorHouse")
                    );
                }
                caseData.putIfAbsent("currentCaseStateVisibleToCaseOfficer", state);
                caseData.putIfAbsent("currentCaseStateVisibleToJudge", state);
                caseData.putIfAbsent("currentCaseStateVisibleToLegalRepresentative", state);
                caseData.putIfAbsent("currentCaseStateVisibleToAdminOfficer", state);
                caseData.putIfAbsent("currentCaseStateVisibleToHomeOfficeApc", state);
                caseData.putIfAbsent("currentCaseStateVisibleToHomeOfficeLart", state);
                caseData.putIfAbsent("currentCaseStateVisibleToHomeOfficePou", state);
                caseData.putIfAbsent("currentCaseStateVisibleToHomeOfficeGeneric", state);
                caseData.putIfAbsent("currentCaseStateVisibleToHomeOfficeAll", state);
            }

            if (eventId != null && eventId.equals("sendDecisionAndReasons")) {
                Map<String, Object> finalDecisionAndReasonsDocument = new HashMap<>();
                finalDecisionAndReasonsDocument.put("document_url", "{$FIXTURE_DOC1_PDF_URL}");
                finalDecisionAndReasonsDocument.put("document_binary_url", "{$FIXTURE_DOC1_PDF_URL_BINARY}");
                finalDecisionAndReasonsDocument.put("document_filename", "{$FIXTURE_DOC1_PDF_FILENAME}");
                mapValueExpander.expandValues(finalDecisionAndReasonsDocument);
                caseData.putIfAbsent("finalDecisionAndReasonsDocument", finalDecisionAndReasonsDocument);
            }
        }

        return caseData;
    }

    private String buildCallbackBody(
        long testCaseId,
        Map<String, Object> input,
        boolean isAsylumCase,
        Map<String, String> templatesByFilename
    ) throws IOException {
        String state = MapValueExtractor.extractOrThrow(input, "state");
        String eventId = MapValueExtractor.extractOrThrow(input, "eventId");
        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            state,
            eventId,
            isAsylumCase,
            templatesByFilename
        );

        LocalDateTime createdDate =
            LocalDateTime.parse(
                MapValueExtractor.extractOrDefault(input, "createdDate", LocalDateTime.now().toString())
            );

        Map<String, Object> caseDetails = new HashMap<>();
        caseDetails.put("id", testCaseId);
        caseDetails.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
        caseDetails.put("state", state);
        caseDetails.put(
            "security_classification",
            MapValueExtractor.extractOrDefault(input, "securityClassification", "PUBLIC")
        );
        caseDetails.put("created_date", createdDate);
        caseDetails.put("case_data", caseData);

        Map<String, Object> callback = new HashMap<>();
        callback.put("event_id", eventId);
        callback.put("case_details", caseDetails);

        if (input.containsKey("caseDataBefore")) {
            Map<String, Object> caseDataBefore = buildCaseData(
                MapValueExtractor.extract(input, "caseDataBefore"),
                state,
                eventId,
                isAsylumCase,
                templatesByFilename
            );

            Map<String, Object> caseDetailsBefore = new HashMap<>();
            caseDetailsBefore.put("id", testCaseId);
            caseDetailsBefore.put("jurisdiction", MapValueExtractor.extractOrDefault(input, "jurisdiction", "IA"));
            caseDetailsBefore.put("state", state);
            caseDetailsBefore.put("created_date", createdDate);
            caseDetailsBefore.put("case_data", caseDataBefore);
            callback.put("case_details_before", caseDetailsBefore);
        }

        return MapSerializer.serialize(callback);
    }

    private String buildCallbackResponseBody(
        Map<String, Object> expectation,
        boolean isAsylumCase,
        Map<String, String> templatesByFilename
    ) throws IOException {

        if (MapValueExtractor.extract(expectation, "confirmation") != null) {

            final Map<String, Object> callbackResponse = new HashMap<>();

            callbackResponse.put(
                "confirmation_header",
                MapValueExtractor.extract(expectation, "confirmation.header")
            );
            callbackResponse.put("confirmation_body", MapValueExtractor.extract(expectation, "confirmation.body"));

            return MapSerializer.serialize(callbackResponse);

        } else {

            Map<String, Object> caseData = buildCaseData(
                MapValueExtractor.extract(expectation, "caseData"),
                null,
                null,
                isAsylumCase,
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
            .orElse("None")
            .toLowerCase();

        return switch (credentials) {
            case "legalrepresentative" -> authorizationHeadersProvider.getLegalRepresentativeAuthorization();
            case "legalrepresentativeorgsuccess" ->
                authorizationHeadersProvider.getLegalRepresentativeOrgSuccessAuthorization();
            case "legalrepresentativeorgdeleted" ->
                authorizationHeadersProvider.getLegalRepresentativeOrgDeletedAuthorization();
            case "caseofficer" -> authorizationHeadersProvider.getCaseOfficerAuthorization();
            case "adminofficer" -> authorizationHeadersProvider.getAdminOfficerAuthorization();
            case "citizen" -> authorizationHeadersProvider.getCitizenAuthorization();
            case "judge" -> authorizationHeadersProvider.getJudgeAuthorization();
            case "system", "systemuser" -> authorizationHeadersProvider.getSystemAuthorization();
            case "homeofficelart" -> authorizationHeadersProvider.getHomeOfficeLartAuthorization();
            case "homeofficepou" -> authorizationHeadersProvider.getHomeOfficePouAuthorization();
            case "homeofficeapc" -> authorizationHeadersProvider.getHomeOfficeApcAuthorization();
            case "homeofficegeneric" -> authorizationHeadersProvider.getHomeOfficeGenericAuthorization();
            case "legalrepresentativeorga" -> authorizationHeadersProvider.getLegalRepresentativeOrgAAuthorization();
            default -> new Headers();
        };
    }

    private Stream<Arguments> scenarioSourcesProvider() throws IOException {
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

        Map<String, String> scenarioSources = new HashMap<>();
        // scenarioSources.putAll(StringResourceLoader.load("/scenarios/" + scenarioPattern));
        // scenarioSources.putAll(StringResourceLoader.load("/scenarios/payments/" + scenarioPattern));
        scenarioSources.putAll(StringResourceLoader.load("/scenarios/bail/" + scenarioPattern));
        // scenarioSources.putAll(StringResourceLoader.load("/scenarios/notifications/" + scenarioPattern));

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        List<Arguments> argumentsList = new ArrayList<>(Collections.emptyList());
        scenarioSources.forEach((filename, scenarioSource) -> {
            try {
                if (failingScenarios.contains(filename)) {
                    Map<String, Object> scenario = deserializeWithExpandedValues(scenarioSource);
                    argumentsList.add(Arguments.of(filename, scenario));
                }
            } catch (IOException e) {
                System.out.println("Failed to parse scenario file: " + filename);
                failedScenarios.add(filename);
                e.printStackTrace();
            }
        });

        return argumentsList.stream();
    }

    private boolean isScenarioEnabled(Map<String, Object> scenario) {
        Object scenarioEnabled = MapValueExtractor.extract(scenario, "enabled");
        boolean scenarioEnabledFlag = true;
        if (scenarioEnabled instanceof Boolean) {
            scenarioEnabledFlag = (Boolean) scenarioEnabled;
        } else if (scenarioEnabled instanceof String) {
            scenarioEnabledFlag = Boolean.parseBoolean((String) scenarioEnabled);
        }
        return scenarioEnabledFlag;
    }

    private boolean isScenarioDisabled(Map<String, Object> scenario) {
        Object scenarioDisabled = MapValueExtractor.extract(scenario, "disabled");
        boolean scenarioDisabledFlag = false;
        if (scenarioDisabled instanceof Boolean) {
            scenarioDisabledFlag = (Boolean) scenarioDisabled;
        } else if (scenarioDisabled instanceof String) {
            scenarioDisabledFlag = Boolean.parseBoolean((String) scenarioDisabled);
        }
        return scenarioDisabledFlag;
    }

    private long getTestId(Map<String, Object> scenario) {
        final long scenarioTestCaseId = MapValueExtractor.extractOrDefault(
            scenario,
            "request.input.id",
            -1
        );

        return (scenarioTestCaseId == -1)
            ? ThreadLocalRandom.current().nextLong(1111111111111111L, 1999999999999999L)
            : scenarioTestCaseId;
    }

    private final List<String> failingScenarios = List.of(
        "RIA-5553-send-bail-documents-uploaded-notification-with-LR.json",
        "RIA-5583-send-bail-application-edited-submitted-notification-with-LR.json",
        "RIA-5584-RIA-5454-send-bail-application-submitted-notification-not-legally-represented.json",
        "RIA-7162-appeal-outcome-notification-taylorHouse-admin-linkedCase_Yes.json",
        "RIA-8349-RIA-8352-send-bail-initial-case-listing-notifications.json",
        "RIA-5583-send-bail-application-edited-submitted-notification-without-LR.json",
        "RIA-8803-send-bail-relisting-case-listing-notifications-remote-hearing.json",
        "RIA-5214-send-application-ended-notifications.json",
        "RIA-5597-send-direction-sent-notifications.json",
        "RIA-5601-Change-direction-due-date-with-LR.json",
        "RIA-5584-RIA-5454-send-bail-application-submitted-notification-to-newport-hearing-centre.json",
        "RIA-8112-HO-upload-bail-summary-direction-notifications.json",
        "RIA-5559-send-bail-documents-edited-notification-with-LR.json",
        "RIA-8803-send-bail-initial-case-listing-notifications-with-ref-data-location.json",
        "RIA-5601-Change-direction-due-date-without-LR.json",
        "RIA-5214-send-application-ended-notifications-not-legally-represented.json",
        "RIA-5561-send-signed-decision-notice-notifications.json",
        "RIA-5782-send-bail-stop-representing-notification.json",
        "RIA-8349-RIA-8352-send-bail-relisting-case-listing-notifications.json",
        "RIA-5345-send-bail-summary-uploaded-notification-without-LR.json",
        "RIA-5561-send-bail-signed-decision-notice-notifications-not-legally-represented.json",
        "RIA-8803-send-bail-initial-case-listing-notifications-with-remote-hearing.json",
        "RIA-8803-send-bail-relisting-case-listing-notifications-ref-data-location.json",
        "RIA-5553-send-bail-documents-uploaded-notification-without-LR.json",
        "RIA-8198-decision-under-ima-notification-ut.json"
    );
}
