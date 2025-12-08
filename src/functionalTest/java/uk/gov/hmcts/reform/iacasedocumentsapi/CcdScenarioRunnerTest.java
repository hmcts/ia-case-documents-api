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

        final String requestBody = buildCallbackBody(
            testCaseId,
            MapValueExtractor.extract(scenario, "request.input"),
            templatesByFilename
        );

        final String requestUri = MapValueExtractor.extract(scenario, "request.uri");
        int expectedStatus = MapValueExtractor.extractOrDefault(scenario, "expectation.status", 200);

        String expectedResponseBody = buildCallbackResponseBody(
            MapValueExtractor.extract(scenario, "expectation"),
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
        Map<String, String> templatesByFilename
    ) throws IOException {

        String templateFilename = MapValueExtractor.extract(caseDataInput, "template");

        Map<String, Object> caseData = deserializeWithExpandedValues(templatesByFilename.get(templateFilename));
        Map<String, Object> caseDataReplacements = MapValueExtractor.extract(caseDataInput, "replacements");
        if (caseDataReplacements != null) {
            MapMerger.merge(caseData, caseDataReplacements);
        }
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
        if (state != null && !state.equals("*")) {
            if (!unlistedStates.contains(state)) {
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
            String finalDecisionAndReasonsDocument = "{\"document_url\": \"{$FIXTURE_DOC1_PDF_URL}\",\"document_binary_url\": \"{$FIXTURE_DOC1_PDF_URL_BINARY}\",\"document_filename\": \"{$FIXTURE_DOC1_PDF_FILENAME}\"}";
            caseData.putIfAbsent("finalDecisionAndReasonsDocument", finalDecisionAndReasonsDocument);
        }

        return caseData;
    }

    private String buildCallbackBody(
        long testCaseId,
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {
        String state = MapValueExtractor.extractOrThrow(input, "state");
        String eventId = MapValueExtractor.extractOrThrow(input, "eventId");
        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            state,
            eventId,
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
        // scenarioSources.putAll(StringResourceLoader.load("/scenarios/bail/" + scenarioPattern));
        scenarioSources.putAll(StringResourceLoader.load("/scenarios/notifications/" + scenarioPattern));

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
        "DIAC-1339-case-listed-notification-harmondsworth-hearing-centre.json",
        "DIAC-1339-edit-case-listing-notification-harmondsworth-hearing-centre.json",
        "DIAC-1371-internal-aip-mark-appeal-remitted-letter-notification-detained-other.json",
        "DIAC-1397_internal_detained_edit_appeal_notification.json",
        "DIAC-1398-appeal-ended-detained-IRC-notification.json",
        "DIAC-1747-edit-case-listing-notification-harmondsworth-hearing-centre-lr-manual.json",
        "RIA-1463-send-appeal-allowed-decision-notification.json",
        "RIA-1463-send-appeal-dismissed-decision-notification.json",
        "RIA-1939-send-appeal-allowed-decision-notification-admin.json",
        "RIA-1939-send-appeal-allowed-decision-notification-judge.json",
        "RIA-1939-send-appeal-dismissed-decision-notification-admin.json",
        "RIA-1939-send-appeal-dismissed-decision-notification-judge.json",
        "RIA-2895-aip-clarifying-question-answers-submitted.json",
        "RIA-3488-change-direction-due-date-both-parties.json",
        "RIA-3488-end-appeal-home-office-notification-before-listing.json",
        "RIA-3488-end-appeal-home-office-notification-judge-before-listing.json",
        "RIA-3799-manage-a-fee-update-hu-appeal-paynow-by-PBA-after-listing.json",
        "RIA-3799-manage-a-fee-update-hu-appeal-paynow-by-PBA-before-listing.json",
        "RIA-3855-decision-outcome-admin-home-office-notification-failure.json",
        "RIA-3855-decision-outcome-caseofficer-home-office-notification-failure.json",
        "RIA-3855-decision-outcome-judge-home-office-notification-failure.json",
        "RIA-4293-send-home-office-evidence-direction.json",
        "RIA-4546-RIA-4559-send-change-hearing-centre-aip-notification.json",
        "RIA-4712-aip-edit-listing-notification.json",
        "RIA-4827-appeal-allowed-no-payment-status-aip.json",
        "RIA-4827-appeal-allowed-payment-failed-aip.json",
        "RIA-4827-appeal-allowed-payment-pending-aip.json",
        "RIA-4827-appeal-dismissed-no-payment-status-aip.json",
        "RIA-4827-appeal-dismissed-payment-pending-aip.json",
        "RIA-4861-case-listed-notification-birmingham-hearing-centre-aip.json",
        "RIA-4932-aip-appeal-outcome-allowed.json",
        "RIA-4932-aip-appeal-outcome-dismissed.json",
        "RIA-4932-aip-ooc-appeal-outcome-allowed.json",
        "RIA-4932-ooc-aip-appeal-outcome-dismissed.json",
        "RIA-4975-case-listed-notification-remote-hearing-centre-aip.json",
        "RIA-5808-aip-home-office-upload-addendum-evidence-notification.json",
        "RIA-5811-aip-upload-addendum-evidence-notification.json",
        "RIA-6071-aip-make-a-time-extension-application-notification-appellant.json",
        "RIA-6071-aip-make-an-application-notification-appellant-after-listing.json",
        "RIA-6071-aip-make-an-application-notification-appellant-before-listing.json",
        "RIA-6072-aip-make-an-application-notification-home-office-apc-after-listing.json",
        "RIA-6072-aip-make-an-application-notification-home-office-apc-before-listing.json",
        "RIA-6608-ada-suitability-suitable.json",
        "RIA-6609-ada-suitability-unsuitable.json",
        "RIA-6781-RIA-7173-internal-ada-case-ada-suitability-notification-suitable.json",
        "RIA-6781-RIA-7173-internal-ada-case-ada-suitability-notification-unsuitable.json",
        "RIA-6782-hearing-bundle-is-ready-notification-to-det-ada.json",
        "RIA-6811-7363-internal-ada-case-decide-an-application-applicant-granted.json",
        "RIA-6811-7363-internal-ada-case-decide-an-application-applicant-refused.json",
        "RIA-6949-appeal-outcome-notification-belfast-admin.json",
        "RIA-6949-appeal-outcome-notification-birmingham-admin.json",
        "RIA-6949-appeal-outcome-notification-bradford--admin.json",
        "RIA-6949-appeal-outcome-notification-coventry-admin.json",
        "RIA-6949-appeal-outcome-notification-glasgow-admin.json",
        "RIA-6949-appeal-outcome-notification-glasgow-tribunal-admin.json",
        "RIA-6949-appeal-outcome-notification-hatton-cross-admin.json",
        "RIA-6949-appeal-outcome-notification-manchester-admin.json",
        "RIA-6949-appeal-outcome-notification-newcastle-admin.json",
        "RIA-6949-appeal-outcome-notification-newport-admin.json",
        "RIA-6949-appeal-outcome-notification-nottingham-admin.json",
        "RIA-6949-appeal-outcome-notification-taylorHouse-admin.json",
        "RIA-7030-reinstate-appeal-internal-notification-before-listing.json",
        "RIA-7033-internal-decide-an-application-notification-admin-officer-granted-after-listing.json",
        "RIA-7033-internal-decide-an-application-notification-admin-officer-refused-before-listing.json",
        "RIA-7162-appeal-outcome-notifcation-taylorHouse-admin-linkedCase_No.json",
        "RIA-7162-appeal-outcome-notification-taylorHouse-admin-linkedCase_Yes-notification.json",
        "RIA-7195-aip-case-listed-notification-with-integrated-harmondsworth-hearing-centre.json",
        "RIA-7366-internal-ada-respondent-adjourn-application-granted-det-notification.json",
        "RIA-7366-internal-ada-respondent-adjourn-application-refused-det-notification.json",
        "RIA-7366-internal-ada-respondent-expedite-application-granted-det-notification.json",
        "RIA-7366-internal-ada-respondent-expedite-application-refused-det-notification.json",
        "RIA-7366-internal-ada-respondent-transfer-out-of-ada-application-granted-det-notification.json",
        "RIA-7366-internal-ada-respondent-transfer-out-of-ada-application-refused-det-notification.json",
        "RIA-7366-RIA-7927-internal-ada-respondent-other-application-granted-det-notification.json.json",
        "RIA-7366-RIA-7927-internal-ada-respondent-other-application-refused-det-notification.json.json",
        "RIA-7375-RIA-7928-internal-ada-maintain-case-link-appeal-notification.json",
        "RIA-7417_appeal_edited_ada_notification.json",
        "RIA-7428-internal-reinstate-appeal-det-notification-ada.json",
        "RIA-7432-internal-detained-ada-change-hearing-centre.json",
        "RIA-7538-hearing-bundle-is-ready-notification-to-det-non-ada.json",
        "RIA-7544-internal-detained-mark-appeal-paid-notifications-non-ada.json",
        "RIA-7545-internal-non-ada-end-appeal-automatically-notification.json",
        "RIA-7558-RIA-7939-internal-detained-non-ada-case-listed.json",
        "RIA-7671-internal-detained-case-decide-an-application-applicant-refused.json",
        "RIA-7688-internal-detained-respondent-adjourn-application-granted-det-notification.json",
        "RIA-7688-internal-detained-respondent-adjourn-application-refused-det-notification.json",
        "RIA-7688-internal-detained-respondent-expedite-application-granted-det-notification.json",
        "RIA-7688-internal-detained-respondent-expedite-application-refused-det-notification.json",
        "RIA-7688-internal-detained-respondent-other-application-granted-det-notification.json",
        "RIA-7688-internal-detained-respondent-other-application-refused-det-notification.json",
        "RIA-7688-internal-detained-respondent-transfer-out-of-ada-application-granted-det-notification.json",
        "RIA-7688-internal-detained-respondent-transfer-out-of-ada-application-refused-det-notification.json",
        "RIA-7690-internal-detained-change-hearing-centre.json",
        "RIA-7694-RIA-7928-internal-non-ada-maintain-case-link-appeal-notification.json",
        "RIA-7698-internal-detained-transferred-out-of-ada.json",
        "RIA-7707_appeal_edited_non-ada_notification.json",
        "RIA-7712-internal-reinstate-appeal-det-notification-non-ada.json",
        "RIA-7948-internal-ada-submit-appeal-notification.json",
        "RIA-7950-internal-appeal-exited-online-before-notification.json",
        "RIA-8260-mark-appeal-as-remitted-aip.json",
        "RIA-8305-internal-aip-case-listed-letter-notification-in-country.json",
        "RIA-8305-internal-aip-case-listed-letter-notification-oot.json",
        "RIA-8306-internal-record-out-of-time-decision-in-country-notification.json",
        "RIA-8306-internal-record-out-of-time-decision-out-of-country-notification.json",
        "RIA-8329-internal-aip-edit-case-listing-letter-notification-in-country.json",
        "RIA-8329-internal-aip-edit-case-listing-letter-notification-out-of-country.json",
        "RIA-8337-internal-aip-end-appeal-letter-notification.json",
        "RIA-8344-internal-aip-mark-appeal-remitted-letter-notification-in-country.json",
        "RIA-8602-internal-non-detained-non-ada-case-listed.json",
        "RIA-8602-internal-non-detained-non-ada-edit-case-listing.json",
        "RIA-8669-mark-appeal-as-remitted-non-detained-appellant-notification.json",
        "RIA-8857-aip-case-listed-notification-with-non-remote-hearing-refdata-enabled.json",
        "RIA-8857-aip-case-listed-notification-with-remote-hearing-refdata-enabled.json",
        "RIA-8857-aip-edit-case-listing-notification-with-non-remote-hearing-refdata-enabled.json",
        "RIA-8941-aip-edit-listing-notification-remote-hearing.json",
        "RIA-9233-internal-aip-mark-appeal-remitted-letter-notification-in-country-represented.json",
        "RIA-9525-appeal-outcome-notification-manchester-admin.json",
        "RIA-9525-case-listed-notification-ada.json",
        "RIA-9525-case-listed-notification-with-remote-hearing-refdata-enabled.json",
        "RIA-9525-edit-case-listing-notification-remote-hearing-no-change.json",
        "RIA-9525-edit-case-listing-notification-with-remote-hearing-refdata-enabled.json",
        "RIA-9525-end-appeal-home-office-notification-judge-before-listing.json",
        "RIA-9525-record-out-of-time-decision-appeal-can-proceed.json",
        "RIA-9525-record-out-of-time-decision-appeal-cannot-proceed.json",
        "RIA-8243-update-tribunal-decision-r31-notification.json"
    );
}
