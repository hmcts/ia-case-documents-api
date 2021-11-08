package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class FeeLookupHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Mock private FeeService feeService;

    private FeeLookupHandler feeLookupHandler;

    @BeforeEach
    void setUp() {

        feeLookupHandler = new FeeLookupHandler(feeService);
    }

    @ParameterizedTest
    @MethodSource("handlingShouldReturnFeeDetailsData")
    void handling_should_return_fee_details(Event event, String feeType, Fee fee, String hearingFeeOption) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingFeeOption));
        when(feeService.getFee(FeeType.valueOf(feeType))).thenReturn(fee);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feeLookupHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getData()).isEqualTo(asylumCase);

        if (hearingFeeOption.equals("decisionWithHearing")) {
            verify(asylumCase, times(1)).write(FEE_WITH_HEARING, fee.getAmountAsString());
        } else {
            verify(asylumCase, times(1)).write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
        }

        String feeAmountInPence =
            String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100")));
        verify(asylumCase, times(1)).write(FEE_AMOUNT_GBP, feeAmountInPence);

    }

    @ParameterizedTest
    @MethodSource("handlingShouldReturnFeeDetailsData")
    void handling_should_error_for_null_fee(Event event, String feeType, Fee fee, String hearingFeeOption) {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(event);

        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of(hearingFeeOption));
        when(feeService.getFee(FeeType.valueOf(feeType))).thenReturn(null);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            feeLookupHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertThat(callbackResponse.getErrors()).isNotEmpty();
        assertThat(callbackResponse.getErrors()).contains("Cannot retrieve the fee from fees-register.");
    }

    private static Stream<Arguments> handlingShouldReturnFeeDetailsData() {

        Fee feeWithHearing = new Fee("FEE0123", "Fee with hearing", "1", new BigDecimal("140"));
        Fee feeWithoutHearing = new Fee("FEE0123", "Fee without hearing", "1", new BigDecimal("80"));

        return Stream.of(
            Arguments.of(Event.START_APPEAL, "FEE_WITH_HEARING", feeWithHearing, "decisionWithHearing"),
            Arguments.of(Event.START_APPEAL, "FEE_WITHOUT_HEARING", feeWithoutHearing, "decisionWithoutHearing"),
            Arguments.of(Event.EDIT_APPEAL, "FEE_WITH_HEARING", feeWithHearing, "decisionWithHearing"),
            Arguments.of(Event.EDIT_APPEAL, "FEE_WITHOUT_HEARING", feeWithoutHearing, "decisionWithoutHearing")
        );
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> feeLookupHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.PAY_AND_SUBMIT_APPEAL);
        assertThatThrownBy(() -> feeLookupHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = feeLookupHandler.canHandle(callbackStage, callback);

                if (Arrays.asList(Event.START_APPEAL, Event.EDIT_APPEAL).contains(event)
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> feeLookupHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feeLookupHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feeLookupHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> feeLookupHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
