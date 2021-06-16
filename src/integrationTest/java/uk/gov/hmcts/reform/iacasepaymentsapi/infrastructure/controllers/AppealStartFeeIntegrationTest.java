package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithIdamStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithRefDataStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithServiceAuthStub;

public class AppealStartFeeIntegrationTest extends SpringBootIntegrationTest
        implements WithServiceAuthStub, WithFeeStub, WithIdamStub, WithRefDataStub {

    @org.springframework.beans.factory.annotation.Value("classpath:organisation-response.json")
    Resource resourceFile;

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void executionEndpoint(@WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class)
                                          WireMockServer server) throws Exception {

        addServiceAuthStub(server);
        addUserInfoStub(server);
        addFeesRegisterStub(server);
        addRefDataStub(server, resourceFile);

        IaCasePaymentApiClient iaCasePaymentApiClient = new IaCasePaymentApiClient(mockMvc);

        PreSubmitCallbackResponseForTest response = iaCasePaymentApiClient.aboutToStart(
            callback()
                .event(Event.PAY_AND_SUBMIT_APPEAL)
                .caseDetails(someCaseDetailsWith()
                                 .state(null)
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
            response.getAsylumCase().read(PAYMENT_STATUS, PaymentStatus.class).get());
        assertEquals("some-appeal-reference-number",
                     response.getAsylumCase().read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
    }
}
