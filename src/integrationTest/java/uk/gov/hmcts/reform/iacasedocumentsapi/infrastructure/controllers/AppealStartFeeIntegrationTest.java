package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithRefDataStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithServiceAuthStub;

public class AppealStartFeeIntegrationTest extends SpringBootIntegrationTest
        implements WithServiceAuthStub, WithFeeStub, WithIdamStub, WithRefDataStub {

    @Value("classpath:organisation-response.json")
    Resource resourceFile;

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void executionEndpoint() throws Exception {

        addServiceAuthStub(server, "ia");
        addUserInfoStub(server);
        addFeesRegisterStub(server);
        addRefDataStub(server, resourceFile);

        IaCaseDocumentsApiClient iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(objectMapper, mockMvc);

        PreSubmitCallbackResponseForTest response = iaCaseDocumentsApiClient.aboutToStart(
            callback()
                .event(Event.PAY_AND_SUBMIT_APPEAL)
                .caseDetails(someCaseDetailsWith()
                                 .state(State.APPEAL_STARTED)
                                 .caseData(anAsylumCase()
                                     .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                                     .with(APPEAL_TYPE, "refusalOfEu")
                                     .with(DECISION_HEARING_FEE_OPTION, "decisionWithHearing")
                                 )
                )
        );

        assertEquals("140", response.getAsylumCase()
                    .read(FEE_WITH_HEARING, String.class).orElse("140"));
        assertEquals(
            PAYMENT_PENDING,
            response.getAsylumCase().read(PAYMENT_STATUS, PaymentStatus.class).orElse(null));
        assertEquals("some-appeal-reference-number",
                     response.getAsylumCase().read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
    }
}
