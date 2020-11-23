package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

public class StitchingApiHttpErrorsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithDocumentUploadStub, DocmosisStub, WithIdamStub, GivensBuilder, WithStitchingStub {

    private static final String STITCH_API_PATH = "/api/new-bundle";

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void should_return_500_with_correct_message_when_api_returns_500(@WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {
        addServiceAuthStub(server);
        addDocumentUploadStub(server);
        addDocumentUploadStub(server);
        withDefaults(server);
        addStitchingBundleStub(server);

        someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer"), server);

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
    public void should_return_500_with_correct_message_when_api_returns_400(@WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server) throws Exception {
        addServiceAuthStub(server);
        addDocumentUploadStub(server);
        addDocumentUploadStub(server);
        withDefaults(server);
        addStitchingBundleError400Stub(server);

        someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer"), server);

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
