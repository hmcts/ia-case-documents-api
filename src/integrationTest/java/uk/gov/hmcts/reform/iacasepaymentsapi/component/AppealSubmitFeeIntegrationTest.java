package uk.gov.hmcts.reform.iacasepaymentsapi.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.ORAL_FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAY_FOR_THE_APPEAL;

import groovy.util.logging.Slf4j;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;

@Slf4j
public class AppealSubmitFeeIntegrationTest extends SpringBootIntegrationTest {

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void should_retrieve_the_fee_amount_for_the_appeal() throws Exception {

        PreSubmitCallbackResponseForTest response = iaCasePaymentsApiClient.aboutToSubmit(callback()
            .event(Event.START_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_STARTED)
                .caseData(anAsylumCase()
                              .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                              .with(PAY_FOR_THE_APPEAL, "payLater")
                              .with(APPEAL_TYPE, "refusalOfEu"))));

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(APPEAL_FEE_DESC, String.class),
            Optional.of("The fee for this type of appeal with a hearing is £140.00"));
        assertEquals(asylumCase.read(ORAL_FEE_AMOUNT_FOR_DISPLAY), Optional.of("£140.00"));
    }


    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void should_make_payment_for_the_appeal() throws Exception {

        PreSubmitCallbackResponseForTest response = iaCasePaymentsApiClient.aboutToSubmit(callback()
            .event(Event.SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                              .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                              .with(PAY_FOR_THE_APPEAL, "payNow")
                              .with(ACCOUNT_NUMBER, "PBA0066906")
                              .with(CUSTOMER_REFERENCE, "CustRef123")
                              .with(PAYMENT_DESCRIPTION, "A test pba payment")
                              .with(APPEAL_TYPE, "refusalOfEu")
                              .with(FEE_CODE, "FEE0238")
                              .with(FEE_DESCRIPTION, "Appeal determined with a hearing")
                              .with(FEE_VERSION, 2)
                              .with(FEE_AMOUNT, 140.00))));

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(PAYMENT_REFERENCE, String.class),
            Optional.of("RC-1590-6786-1063-9996"));
        assertEquals(asylumCase.read(PAYMENT_STATUS, String.class), Optional.of("Success"));
    }
}
