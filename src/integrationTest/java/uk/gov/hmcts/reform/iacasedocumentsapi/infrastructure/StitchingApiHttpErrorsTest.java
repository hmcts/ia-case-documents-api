package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.AsylumCaseFixtures;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

class StitchingApiHttpErrorsTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithDocumentUploadStub, DocmosisStub, WithIdamStub, GivensBuilder, WithStitchingStub, WithRoleAssignmentStub {

    @MockBean
    private FeatureToggler featureToggler;

    private static final String STITCH_API_PATH = "/api/new-bundle";

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void should_return_500_with_correct_message_when_api_returns_500(boolean cdamEnabled) {
        when(featureToggler.getValue("use-ccd-document-am", false)).thenReturn(cdamEnabled);
        addServiceAuthStub(server);
        addDocumentUploadStub(server, cdamEnabled);
        withDefaults(server);
        addNewStitchingBundleStub(server);
        addRoleAssignmentActorStub(server);

        someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "tribunal-caseworker"))
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

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void should_return_500_with_correct_message_when_api_returns_400(boolean cdamEnabled) {
        when(featureToggler.getValue("use-ccd-document-am", false)).thenReturn(cdamEnabled);
        addServiceAuthStub(server);
        addDocumentUploadStub(server, cdamEnabled);
        withDefaults(server);
        addNewStitchingBundleError400Stub(server);
        addRoleAssignmentActorStub(server);

        someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "tribunal-caseworker"))
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
