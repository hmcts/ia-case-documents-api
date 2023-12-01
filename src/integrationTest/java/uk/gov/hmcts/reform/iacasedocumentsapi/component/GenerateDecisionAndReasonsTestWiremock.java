package uk.gov.hmcts.reform.iacasedocumentsapi.component;

import static com.google.common.collect.Sets.newHashSet;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.*;
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
