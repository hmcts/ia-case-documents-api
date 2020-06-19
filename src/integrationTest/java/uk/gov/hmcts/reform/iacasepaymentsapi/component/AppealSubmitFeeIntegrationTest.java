package uk.gov.hmcts.reform.iacasepaymentsapi.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacasepaymentsapi.component.testutils.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PBA_NUMBER;

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

        PreSubmitCallbackResponseForTest response = iaCasePaymentsApiClient.aboutToStart(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                              .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                              .with(APPEAL_TYPE, "refusalOfEu"))));

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(APPEAL_FEE_HEARING_DESC, String.class),
            Optional.of("The fee for this type of appeal with a hearing is £140.00"));
        assertEquals(asylumCase.read(FEE_HEARING_AMOUNT_FOR_DISPLAY), Optional.of("£140.00"));
        assertEquals(asylumCase.read(APPEAL_FEE_WITHOUT_HEARING_DESC, String.class),
                     Optional.of("The fee for this type of appeal without a hearing is £80.00"));
        assertEquals(asylumCase.read(FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY), Optional.of("£80.00"));
    }

    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void should_make_payment_for_the_appeal_with_Hearing() throws Exception {

        PreSubmitCallbackResponseForTest response = iaCasePaymentsApiClient.aboutToSubmit(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(PBA_NUMBER, "PBA0066906")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "CustRef123")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(DECISION_HEARING_FEE_OPTION, "decisionWithHearing"))));

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(PAYMENT_REFERENCE, String.class),
            Optional.of("RC-1590-6786-1063-9996"));
        assertEquals(asylumCase.read(PAYMENT_STATUS, String.class), Optional.of("Paid"));
    }

    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    public void should_make_payment_for_the_appeal_without_Hearing() throws Exception {

        PreSubmitCallbackResponseForTest response = iaCasePaymentsApiClient.aboutToSubmit(callback()
            .event(Event.PAYMENT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(State.APPEAL_SUBMITTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(PBA_NUMBER, "PBA0066906")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "CustRef123")
                    .with(APPEAL_TYPE, "refusalOfEu")
                    .with(DECISION_HEARING_FEE_OPTION, "decisionWithoutHearing"))));

        AsylumCase asylumCase = response.getAsylumCase();

        assertNotNull(response);
        assertNotNull(asylumCase);
        assertEquals(asylumCase.read(PAYMENT_REFERENCE, String.class),
                     Optional.of("RC-1590-6786-1063-9996"));
        assertEquals(asylumCase.read(PAYMENT_STATUS, String.class), Optional.of("Paid"));
    }
}
