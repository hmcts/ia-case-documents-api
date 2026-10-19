package uk.gov.hmcts.reform.iacasedocumentsapi.component;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.GivensBuilder;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithDocumentUploadStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;

@TestPropertySource(properties = "featureFlag.isEmStitchingEnabled=false")
class Stf24WeeksTestWiremock extends SpringBootIntegrationTest
        implements WithServiceAuthStub, WithDocumentUploadStub, DocmosisStub, WithIdamStub, GivensBuilder, WithRoleAssignmentStub {

    @MockitoBean
    private FeatureToggler featureToggler;

    private static AsylumCaseForTest baseCase() {
        return AsylumCaseForTest.anAsylumCase()
                .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                .with(APPELLANT_FAMILY_NAME, "some-fname")
                .with(APPELLANT_GIVEN_NAMES, "some-gname")
                .with(CASE_INTRODUCTION_DESCRIPTION, "some-case-intro")
                .with(APPELLANT_CASE_SUMMARY_DESCRIPTION, "some-case-summary-description")
                .with(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.YES)
                .with(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, "some-agreed-immigration-description")
                .with(SCHEDULE_OF_ISSUES_AGREEMENT, "Yes")
                .with(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, "some-agreed-schedule-of-issues-description")
                .with(DECISION_AND_REASONS_AVAILABLE, YesOrNo.NO)
                .with(ANONYMITY_ORDER, YesOrNo.YES)
                .with(APPELLANT_REPRESENTATIVE, "ted")
                .with(RESPONDENT_REPRESENTATIVE, "bill")
                .with(LIST_CASE_HEARING_CENTRE, "taylorHouse")
                .with(APPEAL_TYPE, AppealType.HU.getValue());
    }

    private void setAdminFlag(AsylumCaseForTest caseData, boolean admin) {
        caseData.with(IS_ADMIN, admin ? YES : NO);
    }

    private void setup(boolean cdamEnabled) {
        when(featureToggler.getValue("use-ccd-document-am", false)).thenReturn(cdamEnabled);

        addServiceAuthStub(server);
        addDocumentUploadStub(server, cdamEnabled);
        withDefaults(server);

        someLoggedIn(UserDetailsForTest.UserDetailsForTestBuilder.userWith()
                .roles(newHashSet("caseworker-ia", "tribunal-caseworker"))
                .forename("Case")
                .surname("Officer"), server);

        docmosisWillReturnSomeDocument(server);
        theDocoumentsManagementApiIsAvailable(server);
        theCaseDocumentAmIsAvailable(server);
        addRoleAssignmentActorStub(server);
    }

    private PreSubmitCallbackResponseForTest submit(AsylumCaseForTest caseData, Event event) {
        return iaCaseDocumentsApiClient.aboutToSubmit(
                CallbackForTest.CallbackForTestBuilder.callback()
                        .event(event)
                        .caseDetails(
                                CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith()
                                        .state(State.DECISION)
                                        .caseData(caseData)
                        )
        );
    }

    private static void assertSingleDocument(
            Optional<List<IdValue<DocumentWithMetadata>>> docs,
            DocumentTag expectedTag
    ) {
        IdValue<DocumentWithMetadata> docValue = docs.map(List::getFirst).orElse(null);

        assertThat(docs.orElse(emptyList()).size()).isEqualTo(1);
        assertThat(docValue).isNotNull();
        assertThat(docValue.getValue().getTag()).isEqualTo(expectedTag);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void shouldCreate24WeeksReviewDocument(boolean cdamEnabled) {
        setup(cdamEnabled);

        AsylumCaseForTest caseData = baseCase();
        caseData.with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES)
                .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                .with(APPEAL_SUBMISSION_DATE, "2002-02-02")
                .with(TRIBUNAL_RECEIVED_DATE, "2002-02-02")
                .with(HOME_OFFICE_DECISION_DATE, "2002-02-02");
        setAdminFlag(caseData, false);

        Optional<List<IdValue<DocumentWithMetadata>>> docs =
                submit(caseData, Event.COMPLETE_CASE_REVIEW).getAsylumCase().read(AsylumCaseDefinition.TRIBUNAL_DOCUMENTS);

        assertSingleDocument(docs, DocumentTag.STF_24WEEKS_CASE_REVIEW_APPELLANT_DOCUMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void shouldCreate24WeeksReviewDocumentIfCaseCreatedByAdmin(boolean cdamEnabled) {
        setup(cdamEnabled);

        AsylumCaseForTest caseData = baseCase();
        caseData.with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.YES)
                .with(COMPLETE_CASE_REVIEW_DATE, "2002-02-02")
                .with(APPEAL_SUBMISSION_DATE, "2002-02-02")
                .with(TRIBUNAL_RECEIVED_DATE, "2002-02-02")
                .with(HOME_OFFICE_DECISION_DATE, "2002-02-02");
        setAdminFlag(caseData, true);

        Optional<List<IdValue<DocumentWithMetadata>>> docs =
                submit(caseData, Event.COMPLETE_CASE_REVIEW).getAsylumCase().read(AsylumCaseDefinition.TRIBUNAL_DOCUMENTS);

        assertSingleDocument(docs, DocumentTag.STF_24WEEKS_CASE_REVIEW_APPELLANT_DOCUMENT);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void shouldCreate24WeeksCaseListedAppellantLetter(boolean cdamEnabled) {
        setup(cdamEnabled);

        AsylumCaseForTest caseData = baseCase();
        caseData.with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YES)
                .with(IS_ADMIN, YES)
                .with(APPELLANTS_REPRESENTATION, YES)
                .with(LIST_CASE_HEARING_DATE, "2025-01-01T10:00:00")
                .with(APPELLANT_ADDRESS, new AddressUk("1 Some Street", null, null, "London", null, "SW1A 1AA", "United Kingdom"));

        submit(caseData, Event.LIST_CASE);

        server.verify(postRequestedFor(urlEqualTo("/docmosis"))
                .withRequestBody(containing("TB-IAC-LET-ENG-Internal-Case-Listed-24-Weeks.docx"))
                .withRequestBody(containing("some-gname")));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @WithMockUser(authorities = {"caseworker-ia", "tribunal-caseworker"})
    void shouldCreate24WeeksCaseListedLegalRepresentativeLetter(boolean cdamEnabled) {
        setup(cdamEnabled);

        AsylumCaseForTest caseData = baseCase();
        caseData.with(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YES)
                .with(IS_ADMIN, YES)
                .with(APPELLANTS_REPRESENTATION, NO)
                .with(LEGAL_REP_HAS_ADDRESS, YES)
                .with(LEGAL_REP_ADDRESS_U_K, new AddressUk("1 Some Street", null, null, "London", null, "SW1A 1AA", "United Kingdom"))
                .with(APPELLANT_ADDRESS, new AddressUk("1 Some Street", null, null, "London", null, "SW1A 1AA", "United Kingdom"))
                .with(LEGAL_REP_GIVEN_NAME, "Legal")
                .with(LEGAL_REP_FAMILY_NAME_PAPER_J, "Representative")
                .with(LIST_CASE_HEARING_DATE, "2025-01-01T10:00:00");

        submit(caseData, Event.LIST_CASE);

        server.verify(postRequestedFor(urlEqualTo("/docmosis"))
                .withRequestBody(containing("TB-IAC-LET-ENG-Internal-Case-Listed-24-Weeks.docx"))
                .withRequestBody(containing("Legal")));
    }
}