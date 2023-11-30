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

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.utilities.DocmosisStub;

@Ignore
class GenerateDecisionAndReasonsTestWiremock extends SpringBootIntegrationTest implements WithServiceAuthStub,
        WithDocumentUploadStub, DocmosisStub, WithIdamStub, GivensBuilder {

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-caseofficer"})
    void generates_decision_and_reasons() {

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
        theCaseDocumentAmIsAvailable(server);

    }
}
