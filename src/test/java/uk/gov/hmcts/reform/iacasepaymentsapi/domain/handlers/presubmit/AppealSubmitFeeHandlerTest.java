package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.times;
import static org.mockito.BDDMockito.verify;
import static org.mockito.BDDMockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.CUSTOMER_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAY_FOR_THE_APPEAL;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AppealSubmitFeeHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private PaymentService paymentService;

    private AppealSubmitFeeHandler appealSubmitFeeHandler;

    @BeforeEach
    public void setUp() {
        appealSubmitFeeHandler = new AppealSubmitFeeHandler(paymentService);
    }

    @Test
    void should_call_payment_api_on_pay_now() throws Exception {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getId()).thenReturn(Long.valueOf("112233445566"));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(PAY_FOR_THE_APPEAL, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(ACCOUNT_NUMBER, String.class)).thenReturn(Optional.of("PBA12345678"));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Some description"));
        when(asylumCase.read(CUSTOMER_REFERENCE, String.class)).thenReturn(Optional.of("CUST001"));

        when(asylumCase.read(FEE_CODE, String.class)).thenReturn(Optional.of("FEE0238"));
        when(asylumCase.read(FEE_DESCRIPTION, String.class))
            .thenReturn(Optional.of("Appeal determined with a hearing"));
        when(asylumCase.read(FEE_VERSION, Integer.class)).thenReturn(Optional.of(new Integer("2")));
        when(asylumCase.read(FEE_AMOUNT, BigDecimal.class)).thenReturn(Optional.of(new BigDecimal("140.00")));

        when(paymentService.creditAccountPayment(any(CreditAccountPayment.class)))
            .thenReturn(new PaymentResponse("RC-1590-6748-2373-9129", new Date(),
                                            "Success", "2020-1590674823325", null
            ));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = appealSubmitFeeHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);

        AsylumCase asylumCase = callbackResponse.getData();

        verify(asylumCase, times(1))
            .write(PAYMENT_REFERENCE, "RC-1590-6748-2373-9129");
        verify(asylumCase, times(1))
            .write(PAYMENT_STATUS, "Success");
    }

    @Test
    void should_throw_when_no_account_number_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(asylumCase.read(ACCOUNT_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(PAY_FOR_THE_APPEAL, String.class)).thenReturn(Optional.of("payNow"));

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("PBA account number is not present");
    }

    @Test
    void should_throw_when_no_payment_description_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(asylumCase.read(ACCOUNT_NUMBER, String.class)).thenReturn(Optional.of("PBA12345678"));
        when(asylumCase.read(PAY_FOR_THE_APPEAL, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Payment description is not present");
    }

    @Test
    void should_throw_when_no_payment_reference_is_present() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);

        when(asylumCase.read(ACCOUNT_NUMBER, String.class)).thenReturn(Optional.of("PBA12345678"));
        when(asylumCase.read(PAY_FOR_THE_APPEAL, String.class)).thenReturn(Optional.of("payNow"));
        when(asylumCase.read(PAYMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("Some description"));
        when(asylumCase.read(CUSTOMER_REFERENCE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Customer payment reference is not present");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> appealSubmitFeeHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmitFeeHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> appealSubmitFeeHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = appealSubmitFeeHandler.canHandle(callbackStage, callback);

                if ((event == Event.SUBMIT_APPEAL)
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }
}
