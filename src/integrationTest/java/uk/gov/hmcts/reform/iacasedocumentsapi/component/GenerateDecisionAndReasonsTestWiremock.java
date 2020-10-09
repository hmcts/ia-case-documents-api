package uk.gov.hmcts.reform.iacasedocumentsapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag.DECISION_AND_REASONS_DRAFT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State.DECISION;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

public class GenerateDecisionAndReasonsTestWiremock extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithDocumentUploadStub, DocmosisStub, WithIdamStub, GivensBuilder {

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    public void generates_decision_and_reasons(
            @WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class) WireMockServer server
    ) {

        addServiceAuthStub(server);
        addDocumentUploadStub(server);
        addDocumentUploadStub(server);
        withDefaults(server);
        //addUserInfoStub(server);

        someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-caseofficer"))
            .forename("Case")
            .surname("Officer"), server);

        docmosisWillReturnSomeDocument(server);
        theDocoumentsManagementApiIsAvailable(server);

        PreSubmitCallbackResponseForTest response = iaCaseDocumentsApiClient.aboutToSubmit(callback()
            .event(Event.GENERATE_DECISION_AND_REASONS)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_FAMILY_NAME, "some-fname")
                    .with(APPELLANT_GIVEN_NAMES, "some-gname")
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
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

                )));

        Optional<List<IdValue<DocumentWithMetadata>>> draftDecisionAndReasonsDocuments =
            response.getAsylumCase().read(DRAFT_DECISION_AND_REASONS_DOCUMENTS);

        IdValue<DocumentWithMetadata> documentWithMetadataIdValue = draftDecisionAndReasonsDocuments.get().get(0);

        assertThat(draftDecisionAndReasonsDocuments.get().size()).isEqualTo(1);
        assertThat(documentWithMetadataIdValue.getValue().getTag()).isEqualTo(DECISION_AND_REASONS_DRAFT);
    }
}
