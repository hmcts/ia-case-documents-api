package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;

@Component
public class AppealFeeDetailsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;

    public AppealFeeDetailsHandler(FeeService feeService) {
        this.feeService = feeService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                    Event.START_APPEAL,
                    Event.EDIT_APPEAL)
                   .contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        Optional<String> decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);
        if (decisionHearingFeeOption.isPresent()) {

            Fee fee = null;
            if (decisionHearingFeeOption.get().equals("decisionWithHearing")) {

                fee = getFee(FeeType.FEE_WITH_HEARING, callbackResponse);
                if (fee == null) {

                    callbackResponse.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));
                    return callbackResponse;
                }

                asylumCase.write(FEE_WITH_HEARING, fee.getAmountAsString());
                asylumCase.write(FEE_AMOUNT,
                    String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100"))));
            } else if (decisionHearingFeeOption.get().equals("decisionWithoutHearing")) {

                fee = getFee(FeeType.FEE_WITHOUT_HEARING, callbackResponse);
                if (fee == null) {

                    callbackResponse.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));
                    return callbackResponse;
                }
                asylumCase.write(FEE_WITHOUT_HEARING, fee.getAmountAsString());
                asylumCase.write(FEE_AMOUNT,
                    String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100"))));
            }
        }

        return callbackResponse;
    }

    private Fee getFee(FeeType feeType, PreSubmitCallbackResponse<AsylumCase> callbackResponse) {

        return feeService.getFee(feeType);
    }
}
