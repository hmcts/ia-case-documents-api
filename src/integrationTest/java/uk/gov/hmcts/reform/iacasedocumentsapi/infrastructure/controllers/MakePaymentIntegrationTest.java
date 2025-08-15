package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.IaCaseDocumentsApiClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithPaymentStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithRefDataStub;
import uk.gov.hmcts.reform.iacasedocumentsapi.component.testutils.WithServiceAuthStub;


class MakePaymentIntegrationTest extends SpringBootIntegrationTest
        implements WithServiceAuthStub, WithFeeStub, WithPaymentStub, WithIdamStub, WithRefDataStub {

    @org.springframework.beans.factory.annotation.Value("classpath:organisation-response.json")
    Resource resourceFile;

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void executionEndpoint() throws Exception {

        addServiceAuthStub(server, "ia");
        addFeesRegisterStub(server);
        addPaymentStub(server);
        addUserInfoStub(server);
        addRefDataStub(server, resourceFile);

        IaCaseDocumentsApiClient iaCaseDocumentsApiClient = new IaCaseDocumentsApiClient(objectMapper, mockMvc);

        PreSubmitCallbackResponseForTest response = iaCaseDocumentsApiClient.aboutToSubmit(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_FAMILY_NAME, "some-appellant-family-name")
                    .with(LEGAL_REP_REFERENCE_NUMBER, "some-legal-reference")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(DECISION_HEARING_FEE_OPTION, "decisionWithHearing")
                    .with(PAYMENT_ACCOUNT_LIST,
                          new DynamicList(new Value("PBA1234567", "PBA1234588"), null))
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "A123456/003"))));

        assertEquals(PAID, response.getAsylumCase().read(PAYMENT_STATUS, PaymentStatus.class).orElse(null));
        assertEquals(new DynamicList(new Value("PBA1234567", "PBA1234588"), null),
                     response.getAsylumCase().read(PAYMENT_ACCOUNT_LIST, DynamicList.class)
                         .orElse(null));
        assertEquals("RC-1590-6786-1063-9996", response.getAsylumCase()
                            .read(PAYMENT_REFERENCE, String.class).orElse(""));
        assertEquals("29 May 2020", response.getAsylumCase()
            .read(PAYMENT_DATE, String.class).orElse(""));
        assertEquals("14000", response.getAsylumCase().read(FEE_AMOUNT_GBP, String.class)
                            .orElse("14000"));
        assertEquals("2", response.getAsylumCase().read(FEE_VERSION, String.class).orElse(""));


        PreSubmitCallbackResponseForTest responseNoHearing = iaCaseDocumentsApiClient.aboutToSubmit(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_FAMILY_NAME, "some-appellant-family-name")
                    .with(LEGAL_REP_REFERENCE_NUMBER, "some-legal-reference")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing")
                    .with(PAYMENT_ACCOUNT_LIST,
                          new DynamicList(new Value("PBA1234567", "PBA1234588"), null))
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "A123456/003"))));

        assertEquals(PAID, responseNoHearing.getAsylumCase()
                        .read(PAYMENT_STATUS, PaymentStatus.class).orElse(null));
        assertEquals(new DynamicList(new Value("PBA1234567", "PBA1234588"), null),
                     responseNoHearing.getAsylumCase().read(PAYMENT_ACCOUNT_LIST, DynamicList.class)
                         .orElse(null));
        assertEquals("RC-1590-6786-1063-9996", responseNoHearing.getAsylumCase()
                        .read(PAYMENT_REFERENCE, String.class).orElse(""));
        assertEquals("8000", responseNoHearing.getAsylumCase()
                        .read(FEE_AMOUNT_GBP, String.class).orElse("8000"));
        assertEquals("2", responseNoHearing.getAsylumCase().read(FEE_VERSION, String.class).orElse(""));
    }
}
