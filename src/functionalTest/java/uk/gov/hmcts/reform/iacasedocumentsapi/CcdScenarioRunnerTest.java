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
import org.junit.jupiter.api.Named;
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
    public void scenarios_should_behave_as_specified(Map<String, Object> scenario, String filename) throws IOException {
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
        if (state != null && !state.equals("*")) {
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
        caseData.putIfAbsent(
            "listCaseHearingCentre",
            caseData.getOrDefault("hearingCentre", "taylorHouse")
        );

        return caseData;
    }

    private String buildCallbackBody(
        long testCaseId,
        Map<String, Object> input,
        Map<String, String> templatesByFilename
    ) throws IOException {
        String state = MapValueExtractor.extractOrThrow(input, "state");
        Map<String, Object> caseData = buildCaseData(
            MapValueExtractor.extract(input, "caseData"),
            state,
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
        callback.put("event_id", MapValueExtractor.extractOrThrow(input, "eventId"));
        callback.put("case_details", caseDetails);

        if (input.containsKey("caseDataBefore")) {
            Map<String, Object> caseDataBefore = buildCaseData(
                MapValueExtractor.extract(input, "caseDataBefore"),
                state,
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
            case "LegalRepresentativeOrgSuccess" ->
                authorizationHeadersProvider.getLegalRepresentativeOrgSuccessAuthorization();
            case "LegalRepresentativeOrgDeleted" ->
                authorizationHeadersProvider.getLegalRepresentativeOrgDeletedAuthorization();
            case "CaseOfficer" -> authorizationHeadersProvider.getCaseOfficerAuthorization();
            case "AdminOfficer" -> authorizationHeadersProvider.getAdminOfficerAuthorization();
            case "Citizen" -> authorizationHeadersProvider.getCitizenAuthorization();
            case "Judge" -> authorizationHeadersProvider.getJudgeAuthorization();
            case "System", "SystemUser" -> authorizationHeadersProvider.getSystemAuthorization();
            case "HomeOfficeLart" -> authorizationHeadersProvider.getHomeOfficeLartAuthorization();
            case "HomeOfficePOU", "HomeOfficePou" -> authorizationHeadersProvider.getHomeOfficePouAuthorization();
            case "HomeOfficeApc" -> authorizationHeadersProvider.getHomeOfficeApcAuthorization();
            case "HomeOfficeGeneric" -> authorizationHeadersProvider.getHomeOfficeGenericAuthorization();
            case "LegalRepresentativeOrgA" -> authorizationHeadersProvider.getLegalRepresentativeOrgAAuthorization();
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
        scenarioSources.putAll(StringResourceLoader.load("/scenarios/" + scenarioPattern));
        scenarioSources.putAll(StringResourceLoader.load("/scenarios/payments/" + scenarioPattern));
        scenarioSources.putAll(StringResourceLoader.load("/scenarios/bail/" + scenarioPattern));
        scenarioSources.putAll(StringResourceLoader.load("/scenarios/notifications/" + scenarioPattern));

        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        System.out.println((char) 27 + "[33m" + "RUNNING " + scenarioSources.size() + " SCENARIOS");
        System.out.println((char) 27 + "[36m" + "-------------------------------------------------------------------");
        List<Arguments> argumentsList = new ArrayList<>(Collections.emptyList());
        scenarioSources.forEach((filename, scenarioSource) -> {
            try {
                Map<String, Object> scenario = deserializeWithExpandedValues(scenarioSource);
                if (failingScenarios.contains(filename)) {
                    argumentsList.add(Arguments.of(Named.of(filename, scenario), filename));
                }
            } catch (IOException e) {
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
        "RIA-1976-RIA-2936-appeal-submitted-notification-glasgow-hearing-centre.json",
        "RIA-9525-edit-case-listing-notification-with-remote-hearing-refdata-enabled.json",
        "RIA-5476-send-bail-application-submitted-notification-to-applicant-sms.json",
        "RIA-8803-send-bail-relisting-case-listing-notifications-remote-hearing.json",
        "RIA-2047-RIA-2694-RIA-3593-direction-request-hearing-requirements.json",
        "RIA-5345-send-bail-summary-uploaded-notification-with-LR.json",
        "RIA-1024-case-submitted-notification-to-case-officer-taylor-house-save-and-continue-disabled.json",
        "RIA-1024-case-submitted-notification-to-case-officer-bradford-save-and-continue-enabled.json",
        "RIA-4467-post-submit-pay-and-submit-appeal-notification-sms.json",
        "RIA-8343-internal-non-detained-non-standard-direction-appellant-letter-notification-in-country.json",
        "RIA-3023-cma-listed-notification-manchester-hearing-centre.json",
        "RIA-8218-update-tribunal-decision-r31-aip-notification-decision.json",
        "RIA-7031-internal-appeal-submitted-notification-birmingham-hearing-centre-payment-pending.json",
        "RIA-4321-end-appeal-aip-notification-before-listing-judge.json",
        "RIA-4827-appeal-dismissed-payment-failed-aip.json",
        "RIA-8198-decision-under-ima-notification-ut.json",
        "RIA-9525-appeal-exited-online-before-listing-notification.json",
        "RIA-1976-RIA-2936-appeal-submitted-notification-birmingham-hearing-centre.json",
        "RIA-1976-RIA-2936-appeal-submitted-notification-hatton-cross-hearing-centre.json",
        "RIA-8334-update-tribunal-decision-r31-internal-letter-notification-ooc-decision.json",
        "RIA-5559-send-bail-documents-edited-notification-with-LR.json",
        "RIA-3488-notification-of-home-office-uploaded-additional-evidence-before-listing.json",
        "RIA-8260-mark-appeal-as-remitted-aip.json",
        "RIA-5561-send-bail-signed-decision-notice-notifications-not-legally-represented.json",
        "RIA-3488-appeal-submitted-notification-hatton-cross-hearing-centre.json",
        "RIA-6949-appeal-outcome-notification-newport-admin.json",
        "RIA-2783-edit-documents-after-listing.json",
        "RIA-5601-Change-direction-due-date-with-LR.json",
        "RIA-9525-end-appeal-home-office-notification-judge-before-listing.json",
        "RIA-1380-RIA-2936-send-appeal-submitted-notification-to-bradford-hearing-centre.json",
        "RIA-9234-notification-of-home-office-uploaded-additional-evidence-after-listing.json",
        "RIA-4083-cma-listed-notification-taylor-house-hearing-centre-remote-hearing.json",
        "RIA-9525-edit-case-listing-notification-remote-hearing-no-change.json",
        "RIA-9525-case-listed-notification-with-remote-hearing-refdata-enabled.json",
        "RIA-3651-ftpa-applicant-appeal-decision-reheard-rule-35-appellant.json",
        "RIA-7031-send-internal-appeal-submitted-notification-to-manchester.json",
        "RIA-3453-make-an-application-notification-home-office-generic-apc-before-listing.json",
        "RIA-7031-send-internal-appeal-submitted-and-paylater-notifications.json",
        "RIA-4467-post-submit-pay-for-appeal-notification-email.json",
        "RIA-3488-appeal-submitted-notification-birmingham-hearing-centre.json",
        "RIA-4827-appeal-allowed-payment-failed-aip.json",
        "RIA-7162-appeal-outcome-notification-taylorHouse-admin-linkedCase_Yes.json",
        "RIA-3492-appeal-submitted-paid-notification-ea-appeal-type.json",
        "RIA-414-RIA-1191-RIA-2694-send-home-office-non-standard-direction-after-listing.json",
        "RIA-7031-internal-appeal-submitted-notification-birmingham-hearing-centre.json",
        "RIA-5584-RIA-5454-send-bail-application-submitted-notification-to-newport-hearing-centre.json",
        "RIA-3488-end-appeal-home-office-notification-before-listing.json",
        "RIA-2045-RIA-2936-notification-of-legalrep-uploaded-additional-evidence-after-listing.json",
        "RIA-3434-appeal-submission-document_after_pay_and_submit_paid.json",
        "RIA-1793-ho-generic-uploaded-response.json",
        "RIA-9234-update-tribunal-decision-r31-notification.json",
        "RIA-1463-send-appeal-dismissed-decision-notification.json",
        "RIA-6949-appeal-outcome-notification-newcastle-admin.json",
        "RIA-1976-case-submitted-notification-to-case-officer-north-shields-save-and-continue-enabled.json",
        "RIA-8243-update-tribunal-decision-r31-notification.json",
        "RIA-8803-send-bail-initial-case-listing-notifications-with-remote-hearing.json",
        "DIAC-1355-update-tribunal-decision-r31-internal-aip-detention-other-letter-notification-in-country-decision.json",
        "RIA-6072-aip-make-an-application-notification-home-office-apc-before-listing.json",
        "RIA-2270-aip-reason-for-appeal submitted.json",
        "RIA-3855-decision-outcome-judge-home-office-notification-failure.json",
        "RIA-3651-ftpa-applicant-appeal-decision-reheard-rule-35-respondent.json",
        "RIA-3651-ftpa-applicant-appeal-decision-reheard-rule-32-respondent.json",
        "RIA-2118-send-appeal-submitted-to-home-office.json",
        "RIA-4321-end-appeal-aip-notification-before-listing-AO.json",
        "RIA-3488-send-appeal-submitted-notification-to-manchester.json",
        "RIA-4083-cma-listed-notification-manchester-hearing-centre-remote-hearing.json",
        "RIA-1024-case-submitted-notification-to-case-officer-taylor-house-save-and-continue-enabled.json",
        "RIA-3023-cma-listed-notification-glasgow-hearing-centre.json",
        "RIA-2265-aip-reason-for-appeal-received-notification-to-case-worker.json",
        "RIA-4293-send-home-office-evidence-direction.json",
        "RIA-2959-adjourn-hearing-without-date.json",
        "RIA-3453-make-an-application-notification-home-office-apc-before-listing.json",
        "RIA-7030-reinstate-appeal-internal-notification-before-listing.json",
        "RIA-5601-Change-direction-due-date-without-LR.json",
        "RIA-3855-ftpa-submitted-respondent-granted-after-appellant-granted.json",
        "RIA-4083-cma-listed-notification-glasgow-hearing-centre-remote-hearing.json",
        "RIA-8064-payment-reminder-remission-legal-rep-rejected.json",
        "RIA-6949-appeal-outcome-notification-glasgow-tribunal-admin.json",
        "RIA-4932-aip-appeal-outcome-allowed.json",
        "RIA-2045-RIA-2936-notification-of-home-office-uploaded-additional-evidence-after-listing.json",
        "RIA-7031-send-internal-appeal-submitted-notification-to-manchester-payment-pending.json",
        "RIA-1967-RIA-2936-aip-send-appeal-submitted-notification-to-case-worker.json",
        "RIA-365-RIA-2936-send-appeal-submitted-notification-to-manchester.json",
        "RIA-8344-internal-aip-mark-appeal-remitted-letter-notification-in-country.json",
        "RIA-1976-case-submitted-notification-to-case-officer-north-shields-save-and-continue-disabled.json",
        "RIA-3453-make-an-application-notification-home-office-generic-lart-after-listing.json",
        "RIA-3488-send-appeal-submitted-notification-to-bradford-hearing-centre.json",
        "RIA-2999-RIA-3003-ftpa-respondent-reheard-after-appellant-refused-taylor-house.json",
        "RIA-4827-appeal-dismissed-no-payment-status-aip.json",
        "RIA-3799-manage-a-fee-update-pa-appeal-paynow-by-account-before-listing.json",
        "RIA-3023-cma-listed-notification-birmingham-hearing-centre.json",
        "RIA-7162-appeal-outcome-notifcation-taylorHouse-admin-linkedCase_No.json",
        "RIA-9234-send-appeal-submitted-and-paid-notification-legal-rep.json",
        "RIA-3155-reinstate-appeal-notification-before-listing.json",
        "RIA-4827-appeal-dismissed-payment-pending-aip.json",
        "RIA-7031-send-internal-appeal-submitted-notification-to-bradford-hearing-centre-payment-pending.json",
        "RIA-4827-appeal-allowed-no-payment-status-aip.json",
        "RIA-3488-appeal-submitted-notification-north-shields-hearing-centre.json",
        "RIA-6949-appeal-outcome-notification-nottingham-admin.json",
        "RIA-3453-make-an-application-notification-home-office-generic-pou-before-listing.json",
        "RIA-8192-record-adjournment-details-list-assist-integrated.json",
        "RIA-4827-appeal-allowed-payment-pending-aip.json",
        "RIA-7991-record-adjournment-details.json",
        "RIA-7031-send-internal-appeal-submitted-notification-to-newport-hearing-centre.json",
        "RIA-1647-RIA-1276-RIA-2936-end-appeal-home-office-notification-before-listing.json",
        "RIA-3434-appeal-submission-document_after_pay_and_submit_failed_changed_to_pay_offline.json",
        "RIA-3023-cma-listed-notification-bradford-hearing-centre.json",
        "RIA-8334-update-tribunal-decision-r31-internal-letter-notification-in-country-decision.json",
        "RIA-1976-case-submitted-notification-to-case-officer-birmingham-save-and-continue-disabled.json",
        "RIA-3799-manage-a-fee-update-hu-appeal-paid-by-card-before-listing.json",
        "RIA-5214-send-application-ended-notifications.json",
        "RIA-7730-internal-non-ada-detention-appeal-cannot-proceed.json",
        "RIA-1976-case-submitted-notification-to-case-officer-hatton-cross-save-and-continue-disabled.json",
        "RIA-8803-send-bail-initial-case-listing-notifications-with-ref-data-location.json",
        "RIA-9234-case-submitted-notification-to-case-officer-north-shields-save-and-continue-enabled.json",
        "RIA-1939-send-appeal-dismissed-decision-notification-admin.json",
        "RIA-4559-reinstate-appeal-notification-before-listing-aip.json",
        "RIA-3220-send-appeal-submitted-payment-pending-notification-legal-rep.json",
        "RIA-6949-appeal-outcome-notification-hatton-cross-admin.json",
        "RIA-5597-send-direction-sent-notifications.json",
        "RIA-7031-internal-appeal-submitted-notification-glasgow-hearing-centre-payment-pending.json",
        "RIA-1939-send-appeal-dismissed-decision-notification-judge.json",
        "RIA-3690-appeal-with-remission-approved-ea-appeal-type-notification.json",
        "RIA-8064-payment-reminder-remission-legal-rep-partiallyapproved.json",
        "RIA-1939-send-appeal-allowed-decision-notification-judge.json",
        "RIA-7031-internal-appeal-submitted-notification-glasgow-hearing-centre.json",
        "RIA-7031-internal-appeal-submitted-notification-hatton-cross-hearing-centre-pending-payment.json",
        "RIA-8218-update-tribunal-decision-r31-aip-notification-document.json",
        "RIA-1793-ho-lart-uploaded-response.json",
        "RIA-3855-decision-outcome-caseofficer-home-office-notification-failure.json",
        "RIA-9233-internal-aip-mark-appeal-remitted-letter-notification-in-country-represented.json",
        "RIA-2999-RIA-3003-ftpa-appellant-reheard-after-respondent-granted-taylor-house.json",
        "RIA-6949-appeal-outcome-notification-belfast-admin.json",
        "RIA-7948-internal-ada-submit-appeal-notification.json",
        "RIA-3799-manage-a-fee-update-pa-appeal-paylater-by-account-after-listing.json",
        "RIA-3855-ftpa-submitted-appellant-granted-after-appellant-granted.json",
        "RIA-3023-cma-listed-notification-hatton-cross-hearing-centre.json",
        "RIA-6949-appeal-outcome-notification-taylorHouse-admin.json",
        "RIA-4932-aip-appeal-outcome-dismissed.json",
        "RIA-1400-appeal-exited-online-before-listing-notification.json",
        "RIA-1976-case-submitted-notification-to-case-officer-glasgow-save-and-continue-disabled.json",
        "RIA-7950-internal-appeal-exited-online-before-notification.json",
        "RIA-1976-case-submitted-notification-to-case-officer-birmingham-save-and-continue-enabled.json",
        "RIA-6949-appeal-outcome-notification-birmingham-admin.json",
        "RIA-3799-manage-a-fee-update-hu-appeal-paynow-by-PBA-after-listing.json",
        "RIA-3799-manage-a-fee-update-pa-appeal-paynow-by-account-after-listing.json",
        "RIA-5583-send-bail-application-edited-submitted-notification-with-LR.json",
        "RIA-8669-mark-appeal-as-remitted-non-detained-appellant-notification.json",
        "RIA-1939-send-appeal-allowed-decision-notification-admin.json",
        "RIA-2045-RIA-2936-notification-of-legalrep-uploaded-additional-evidence-before-listing.json",
        "RIA-2999-RIA-3003-ftpa-appellant-reheard-after-respondent-granted-bradford.json",
        "RIA-8218-update-tribunal-decision-r31-aip-notification-both-document-decision.json",
        "RIA-3434-appeal-submission-document_after_pay_and_submit_failed_changed_to_pay_later.json",
        "RIA-2895-aip-clarifying-question-answers-submitted.json",
        "RIA-3855-ftpa-decided-appellant-granted-after-appellant-granted.json",
        "RIA-8803-send-bail-relisting-case-listing-notifications-ref-data-location.json",
        "RIA-4083-cma-listed-notification-hatton-cross-hearing-centre-remote-hearing.json",
        "RIA-3453-make-an-application-notification-home-office-lart-after-listing.json",
        "RIA-5583-send-bail-application-edited-submitted-notification-without-LR.json",
        "RIA-7031-internal-appeal-submitted-notification-hatton-cross-hearing-centre.json",
        "RIA-1283-notification-of-submitted-hearing-requirements.json",
        "RIA-9233-internal-non-detained-non-standard-direction-appellant-letter-notification-in-country-represented.json",
        "DIAC-1371-internal-aip-mark-appeal-remitted-letter-notification-detained-other.json",
        "RIA-3488-change-direction-due-date-both-parties.json",
        "RIA-9234-record-adjournment-details-list-assist-integrated.json",
        "RIA-2999-RIA-3003-ftpa-respondent-reheard-after-appellant-refused-glasgow.json",
        "RIA-1463-send-appeal-allowed-decision-notification.json",
        "RIA-1380-RIA-2936-send-appeal-submitted-notification-to-newport-hearing-centre.json",
        "RIA-7031-internal-appeal-submitted-notification-north-shields-hearing-centre.json",
        "RIA-8192-adjourn-hearing-without-date-list-assist-integrated.json",
        "RIA-8366-internal-aip-payment-reminder-notifications-in-country.json",
        "RIA-2048-RIA-2694-RIA-3593-direction-request-hearing-requirements-legal-rep.json",
        "RIA-4467-post-submit-pay-and-submit-timeout-payment-notification-email-pa.json",
        "RIA-3488-notification-of-legalrep-uploaded-additional-evidence-before-listing.json",
        "RIA-806100-aip-payment-reminder-remission-partiallyapproved.json",
        "RIA-7559-internal-detained-non-ada-list-case-letter.json",
        "RIA-3453-make-an-application-notification-home-office-generic-apc-after-listing.json",
        "RIA-5345-send-bail-summary-uploaded-notification-without-LR.json",
        "RIA-6608-ada-suitability-suitable.json",
        "RIA-4467-post-submit-pay-and-submit-appeal-notification-email.json",
        "RIA-7031-send-internal-appeal-submitted-notification-to-newport-hearing-centre-payment-pending.json",
        "RIA-9525-appeal-outcome-notification-manchester-admin.json",
        "RIA-2239-RIA-2936-end-appeal-home-office-notification-judge-before-listing.json",
        "RIA-8366-internal-aip-payment-reminder-notifications-out-of-country.json",
        "RIA-3313-appellant-ftpa-submitted-ho-notification-failure.json",
        "RIA-6949-appeal-outcome-notification-coventry-admin.json",
        "RIA-8061-aip-payment-reminder-remission-rejected.json",
        "RIA-7031-send-internal-appeal-submitted-payment-pending-pay-offline-notifications.json",
        "RIA-3453-make-an-application-notification-home-office-lart-before-listing.json",
        "RIA-8112-HO-upload-bail-summary-direction-notifications.json",
        "RIA-3488-end-appeal-home-office-notification-judge-before-listing.json",
        "RIA-3799-manage-a-fee-update-pa-appeal-paylater-by-account-before-listing.json",
        "RIA-4083-cma-listed-notification-north-shields-hearing-centre-remote-hearing.json",
        "RIA-1976-case-submitted-notification-to-case-officer-glasgow-save-and-continue-enabled.json",
        "RIA-4083-cma-listed-notification-newport-hearing-centre-remote-hearing.json",
        "RIA-4321-end-appeal-aip-notification-before-listing-CO.json",
        "RIA-4827-appeal-ended-no-payment-status-aip.json",
        "RIA-3492-appeal-submitted-paid-notification-hu-appeal-type.json",
        "DIAC-1350-internal-out-of-time-decision-can-proceed-letter-notification.json",
        "RIA-5782-send-bail-stop-representing-notification.json",
        "RIA-3453-make-an-application-notification-home-office-generic-lart-before-listing.json",
        "RIA-7031-internal-appeal-submitted-notification-north-shields-hearing-centre-pending-payment.json",
        "RIA-4546-RIA-4559-appeal-exited-online-before-listing-aip-notification.json",
        "RIA-8349-RIA-8352-send-bail-relisting-case-listing-notifications.json",
        "RIA-3488-appeal-submitted-notification-glasgow-hearing-centre.json",
        "RIA-5553-send-bail-documents-uploaded-notification-with-LR.json",
        "RIA-3488-aip-send-appeal-submitted-notification-to-case-worker.json",
        "RIA-6557-ada-notification-submitted-hearing-requirements.json",
        "RIA-7031-send-internal-appeal-submitted-notification-to-bradford-hearing-centre.json",
        "RIA-9525-appeal-submitted-paid-notification-hu-appeal-type.json",
        "RIA-3020-aip-cma-requirements-submitted.json",
        "RIA-3799-manage-a-fee-update-hu-appeal-paynow-by-PBA-before-listing.json",
        "RIA-3651-ftpa-applicant-appeal-decision-reheard-rule-32-appellant.json",
        "RIA-6949-appeal-outcome-notification-manchester-admin.json",
        "RIA-5553-send-bail-documents-uploaded-notification-without-LR.json",
        "RIA-3799-manage-a-fee-update-pa-appeal-paid-by-card-after-listing.json",
        "RIA-3799-manage-a-fee-update-hu-appeal-paid-by-card-after-listing.json",
        "RIA-6609-ada-suitability-unsuitable.json",
        "RIA-4083-cma-listed-notification-bradford-hearing-centre-remote-hearing.json",
        "RIA-4827-appeal-ended-payment-pending-aip.json",
        "RIA-4083-cma-listed-notification-birmingham-hearing-centre-remote-hearing.json",
        "RIA-3472-send-appeal-submitted-and-paylater-notification-legal-rep.json",
        "RIA-3305-async-stitching-ho-notification-failure.json",
        "RIA-3313-respondent-ftpa-submitted-ho-notification-failure.json",
        "RIA-2975-RIA-3003-ftpa-applicant-appeal-decision-reheard-appellant.json",
        "RIA-9233-update-tribunal-decision-r31-internal-letter-notification-in-country-decision-represented.json",
        "RIA-3228-change-direction-due-date-both-parties.json",
        "RIA-4932-aip-ooc-appeal-outcome-allowed.json",
        "RIA-9234-direction-request-hearing-requirements-legal-rep.json",
        "RIA-1976-RIA-2936-appeal-submitted-notification-north-shields-hearing-centre.json",
        "RIA-3488-send-appeal-submitted-notification-to-newport-hearing-centre.json",
        "RIA-3023-cma-listed-notification-newport-hearing-centre.json",
        "RIA-3453-make-an-application-notification-home-office-generic-pou-after-listing.json",
        "RIA-9525-case-listed-notification-ada.json",
        "RIA-4467-post-submit-pay-and-submit-failed-payment-notification-email-pa.json",
        "RIA-3799-manage-a-fee-update-pa-appeal-paid-by-card-before-listing.json",
        "RIA-8857-aip-edit-case-listing-notification-with-non-remote-hearing-refdata-enabled.json",
        "RIA-2783-edit-documents-before-listing.json",
        "RIA-7031-send-internal-appeal-submitted-to-home-office.json",
        "RIA-3023-cma-listed-notification-taylor-house-hearing-centre.json",
        "RIA-2975-RIA-3003-ftpa-applicant-appeal-decision-reheard-respondent.json",
        "RIA-7162-appeal-outcome-notification-taylorHouse-admin-linkedCase_Yes-notification.json",
        "RIA-3023-cma-listed-notification-north-shields-hearing-centre.json",
        "RIA-1792-notify-case-officer-of-respondent-evidence-submitted.json",
        "RIA-6949-appeal-outcome-notification-bradford--admin.json",
        "RIA-1976-case-submitted-notification-to-case-officer-hatton-cross-save-and-continue-enabled.json",
        "RIA-4932-ooc-aip-appeal-outcome-dismissed.json",
        "RIA-2045-RIA-2936-notification-of-home-office-uploaded-additional-evidence-before-listing.json",
        "RIA-3155-reinstate-appeal-notification-home-office-lart.json",
        "RIA-4546-RIA-4559-link-appeal-after-listing-aip-notification.json",
        "RIA-5584-RIA-5454-send-bail-application-submitted-notification-not-legally-represented.json",
        "RIA-8349-RIA-8352-send-bail-initial-case-listing-notifications.json",
        "RIA-6949-appeal-outcome-notification-glasgow-admin.json",
        "RIA-3855-decision-outcome-admin-home-office-notification-failure.json"
    );
}
