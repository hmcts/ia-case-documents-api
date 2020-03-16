package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import org.junit.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;

public class StitchingApiHttpErrorsTest extends SpringBootIntegrationTest {

    private static final String STITCH_API_PATH = "/api/stitch-ccd-bundles";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_500_with_correct_message_when_api_returns_500() throws Exception {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer"));

        stubFor(post(urlPathEqualTo(STITCH_API_PATH))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader("Content-Type", "application/json")
                .withBody("Error from stitching-api"))
        );

        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        assertThatThrownBy(
            () -> iaCaseDocumentsApiClient.aboutToSubmit(callback()
                .caseDetails(someCaseDetailsWith()
                    .jurisdiction("IA")
                    .state(State.FINAL_BUNDLING)
                    .id(1L)
                    .caseData(anAsylumCase()
                        .withCaseDetails(asylumCase)
                    ))
                .event(Event.GENERATE_HEARING_BUNDLE)))
            .hasMessageContaining("500")
            .hasMessageContaining("Couldn't create bundle using API: ")
            .hasMessageContaining(STITCH_API_PATH);

    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_500_with_correct_message_when_api_returns_400() throws Exception {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer"));

        stubFor(post(urlPathEqualTo(STITCH_API_PATH))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader("Content-Type", "application/json")
                .withBody("Error from stitching-api"))
        );

        AsylumCase asylumCase = AsylumCaseFixtures.someAsylumCaseWithDefaults();

        assertThatThrownBy(
            () -> iaCaseDocumentsApiClient.aboutToSubmit(callback()
                .caseDetails(someCaseDetailsWith()
                    .jurisdiction("IA")
                    .state(State.FINAL_BUNDLING)
                    .id(1L)
                    .caseData(anAsylumCase()
                        .withCaseDetails(asylumCase)
                    ))
                .event(Event.GENERATE_HEARING_BUNDLE)))
            .hasMessageContaining("500")
            .hasMessageContaining("Couldn't create bundle using API: ")
            .hasMessageContaining(STITCH_API_PATH);
    }
}
