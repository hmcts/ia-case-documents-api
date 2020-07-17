package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PaymentAppealFeePreparerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;

    private PaymentAppealFeePreparer paymentAppealFeePreparer;

    @BeforeEach
    void setUp() {
        paymentAppealFeePreparer = new PaymentAppealFeePreparer(feeService);
    }

    @Test
    void should_retrieve_the_fee_with_hearing_for_appeal_type_EA() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING))
            .thenReturn(new Fee(
                "FEE0238",
                "Appeal determined with a hearing",
                "2",
                new BigDecimal("140")
            ));
        when(feeService.getFee(FeeType.FEE_WITHOUT_HEARING))
            .thenReturn(new Fee(
                "FEE0456",
                "Appeal determined without a hearing",
                "2",
                new BigDecimal("80")
            ));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealFeePreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1))
            .write(FEE_HEARING_AMOUNT_FOR_DISPLAY, "£140");
        verify(asylumCase, times(1))
            .write(APPEAL_FEE_HEARING_DESC, "The fee for this type of appeal with a hearing is £140");
        verify(asylumCase, times(1))
            .write(FEE_HEARING_AMOUNT_FOR_DISPLAY, "£140");
    }

    @Test
    void should_retrieve_the_fee_without_hearing_for_appeal_type_EA() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING))
            .thenReturn(new Fee(
                "FEE0238",
                "Appeal determined with a hearing",
                "2",
                new BigDecimal("140")
            ));
        when(feeService.getFee(FeeType.FEE_WITHOUT_HEARING))
            .thenReturn(new Fee(
                "FEE0456",
                "Appeal determined without a hearing",
                "2",
                new BigDecimal("80")
            ));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealFeePreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1))
            .write(FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY, "£80");
        verify(asylumCase, times(1))
            .write(APPEAL_FEE_WITHOUT_HEARING_DESC, "The fee for this type of appeal without a hearing is £80");
        verify(asylumCase, times(1))
            .write(FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY, "£80");
    }

    @Test
    void should_return_error_when_fee_with_hearing_does_not_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealFeePreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getErrors().contains("Cannot retrieve the fee from fees-register."));
    }

    @Test
    void should_return_error_when_fee_without_hearing_does_not_exists() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        when(feeService.getFee(FeeType.FEE_WITH_HEARING))
            .thenReturn(new Fee(
                "FEE0238",
                "Appeal determined with a hearing",
                "2",
                new BigDecimal("140")
            ));
        when(feeService.getFee(FeeType.FEE_WITHOUT_HEARING)).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealFeePreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertTrue(callbackResponse.getErrors().contains("Cannot retrieve the fee from fees-register."));
    }

    @Test
    void should_allow_valid_Appeal_type_for_payment_appeal_Event() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.PAYMENT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING))
            .thenReturn(new Fee(
                "FEE0238",
                "Appeal determined with a hearing",
                "2",
                new BigDecimal("140")
            ));
        when(feeService.getFee(FeeType.FEE_WITHOUT_HEARING))
            .thenReturn(new Fee(
                "FEE0456",
                "Appeal determined without a hearing",
                "2",
                new BigDecimal("80")
            ));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse = paymentAppealFeePreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        assertNotNull(callbackResponse);

        verify(asylumCase, times(1))
            .write(FEE_HEARING_AMOUNT_FOR_DISPLAY, "£140");
        verify(asylumCase, times(1))
            .write(APPEAL_FEE_HEARING_DESC, "The fee for this type of appeal with a hearing is £140");
        verify(asylumCase, times(1))
            .write(FEE_HEARING_AMOUNT_FOR_DISPLAY, "£140");
    }

    @Test
    void should_allow_valid_payment_Appeal_type() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        assertFalse(paymentAppealFeePreparer.isNotValidAppealType(asylumCase));
    }

    @Test
    void should_Not_allow_invalid_payment_Appeal_type() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));
        assertTrue(paymentAppealFeePreparer.isNotValidAppealType(asylumCase));
    }

    @Test
    void should_Not_allow_invalid_Appeal_type() {

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        assertTrue(paymentAppealFeePreparer.isNotValidAppealType(asylumCase));
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> paymentAppealFeePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentAppealFeePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentAppealFeePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> paymentAppealFeePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> paymentAppealFeePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> paymentAppealFeePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = paymentAppealFeePreparer.canHandle(callbackStage, callback);

                if (((event == Event.START_APPEAL) || (event == Event.EDIT_APPEAL) || (event == Event.PAYMENT_APPEAL))
                    && (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }
}
