package uk.gov.hmcts.reform.iacasedocumentsapi.component;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.GivensBuilder;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithDocumentUploadStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;

/**
 * Simplified-but-equivalent version of the original test class.
 */
class Stf24WeeksTestWiremock extends SpringBootIntegrationTest
        implements WithServiceAuthStub, WithDocumentUploadStub, DocmosisStub, WithIdamStub, GivensBuilder, WithRoleAssignmentStub {

    @MockBean
    private FeatureToggler featureToggler;

    private static AsylumCaseForTest mockCaseData() {
        // build a small test case with the fields the test relies on
        return AsylumCaseForTest.anAsylumCase()
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME, "some-fname")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, "some-gname")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CASE_INTRODUCTION_DESCRIPTION, "some-case-intro")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_CASE_SUMMARY_DESCRIPTION, "some-case-summary-description")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.YES)
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.AGREED_IMMIGRATION_HISTORY_DESCRIPTION, "some-agreed-immigration-description")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.SCHEDULE_OF_ISSUES_AGREEMENT, "Yes")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, "some-agreed-schedule-of-issues-description")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_AND_REASONS_AVAILABLE, YesOrNo.NO)
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ANONYMITY_ORDER, YesOrNo.YES)
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_REPRESENTATIVE, "ted")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.RESPONDENT_REPRESENTATIVE, "bill")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, "taylorHouse")
                .with(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE, AppealType.HU.getValue());
    }

    private void createdByAdmin(AsylumCaseForTest caseData) {
        caseData.with(IS_ADMIN, YesOrNo.YES);
    }

    private void notCreatedByAdmin(AsylumCaseForTest caseData) {
        caseData.with(IS_ADMIN, YesOrNo.NO);
    }

    private void setup(boolean cdamEnabled) {
        // feature toggle for document management
        when(featureToggler.getValue("use-ccd-document-am", false)).thenReturn(cdamEnabled);
        // wiremock stubs and default test setup provided by the test utils / base class
        addServiceAuthStub(server);

        addDocumentUploadStub(server, cdamEnabled);
        withDefaults(server);
        someLoggedIn(uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith()
                .roles(newHashSet("caseworker-ia", "tribunal-caseworker"))
                .forename("Case")
                .surname("Officer"), server);

        // external services behavior
        docmosisWillReturnSomeDocument(server);
        theDocoumentsManagementApiIsAvailable(server);
        theCaseDocumentAmIsAvailable(server);
        addRoleAssignmentActorStub(server);
    }

    private PreSubmitCallbackResponseForTest doCaseReview(AsylumCaseForTest caseData) {
        // perform the callback that the system under test expects
        PreSubmitCallbackResponseForTest response = iaCaseDocumentsApiClient.aboutToSubmit(
                uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback()
                        .event(Event.COMPLETE_CASE_REVIEW)
                        .caseDetails(
                                uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith()
                                        .state(uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State.DECISION)
                                        .caseData(caseData)
                        )
        );
        return response;
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void shouldCreate24WeeksReviewDocument(boolean cdamEnabled) {
        setup(cdamEnabled);

        AsylumCaseForTest caseData = mockCaseData();
        caseData.with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES).with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                .with(APPEAL_SUBMISSION_DATE, "2002-02-02")
                .with(TRIBUNAL_RECEIVED_DATE, "2002-02-02")
                .with(HOME_OFFICE_DECISION_DATE, "2002-02-02");
        notCreatedByAdmin(caseData);

        Optional<List<IdValue<DocumentWithMetadata>>> docsOpt =
                doCaseReview(caseData).getAsylumCase().read(AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS);

        IdValue<DocumentWithMetadata> docValue = docsOpt.get().get(0);

        assertThat(docsOpt.get().size()).isEqualTo(1);
        assertThat(docValue.getValue().getTag()).isEqualTo(DocumentTag.INTERNAL_APPEAL_SUBMISSION);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void shouldNotCreate24WeeksReviewDocumentIfCaseCreatedByAdmin(boolean cdamEnabled) {
        setup(cdamEnabled);
        AsylumCaseForTest caseData = mockCaseData();
        caseData.with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES).with(HOME_OFFICE_DECISION_DATE, "2002-02-02");
        notCreatedByAdmin(caseData);
        createdByAdmin(caseData);
        Optional<List<IdValue<DocumentWithMetadata>>> docsOpt =
                doCaseReview(caseData).getAsylumCase().read(AsylumCaseDefinition.NOTIFICATION_ATTACHMENT_DOCUMENTS);
        assertThat(docsOpt).isNotPresent();
    }


}