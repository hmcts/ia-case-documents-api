package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasepaymentsapi.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import ru.lanwen.wiremock.ext.WiremockResolver;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.IaCasePaymentApiClient;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.StaticPortWiremockFactory;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithFeeStub;
import uk.gov.hmcts.reform.iacasepaymentsapi.testutils.WithServiceAuthStub;

public class AppealStartFeeIntegrationTest extends SpringBootIntegrationTest
        implements WithServiceAuthStub, WithFeeStub {

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void executionEndpoint(@WiremockResolver.Wiremock(factory = StaticPortWiremockFactory.class)
                                          WireMockServer server) throws Exception {

        addServiceAuthStub(server);
        addFeesRegisterStub(server);

        IaCasePaymentApiClient iaCasePaymentApiClient = new IaCasePaymentApiClient(mockMvc);

        PreSubmitCallbackResponseForTest response = iaCasePaymentApiClient.aboutToStart(
            callback()
                .event(Event.START_APPEAL)
                .caseDetails(someCaseDetailsWith()
                                 .state(null)
                                 .caseData(anAsylumCase()
                                               .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                                               .with(APPEAL_TYPE, "refusalOfEu")))
        );

        assertEquals("£140", response.getAsylumCase()
                    .read(FEE_HEARING_AMOUNT_FOR_DISPLAY, String.class).orElse(""));
        assertEquals("£80", response.getAsylumCase()
                    .read(FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY, String.class).orElse(""));
        assertEquals("The fee for this type of appeal with a hearing is £140",
                     response.getAsylumCase().read(APPEAL_FEE_HEARING_DESC, String.class).orElse(""));
        assertEquals("The fee for this type of appeal without a hearing is £80",
                     response.getAsylumCase().read(APPEAL_FEE_WITHOUT_HEARING_DESC, String.class).orElse(""));
        assertEquals("Payment due",
                     response.getAsylumCase().read(PAYMENT_STATUS, String.class).orElse(""));
        assertEquals("some-appeal-reference-number",
                     response.getAsylumCase().read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
    }

}
