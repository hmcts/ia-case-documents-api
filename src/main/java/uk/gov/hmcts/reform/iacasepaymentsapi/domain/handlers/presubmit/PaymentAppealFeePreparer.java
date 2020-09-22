package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_FEE_WITHOUT_HEARING_DESC;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_DUE;

import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
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
public class PaymentAppealFeePreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;

    public PaymentAppealFeePreparer(
        FeeService feeService
    ) {
        this.feeService = feeService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && (callback.getEvent() == Event.START_APPEAL
                    || callback.getEvent() == Event.EDIT_APPEAL
                    || callback.getEvent() == Event.PAYMENT_APPEAL
                    || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL));
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

        if ((callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL || callback.getEvent() == Event.PAYMENT_APPEAL)
            && isNotValidAppealType(asylumCase)) {

            throw new IllegalStateException("AppealType is not valid");
        }

        Fee feeHearing = feeService.getFee(FeeType.FEE_WITH_HEARING);
        Fee feeWithoutHearing = feeService.getFee(FeeType.FEE_WITHOUT_HEARING);

        if ((feeHearing == null) || (feeWithoutHearing == null)) {
            PreSubmitCallbackResponse<AsylumCase> response =
                new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
            response.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));

            return response;
        }

        asylumCase.write(
            APPEAL_FEE_HEARING_DESC,
            "The fee for an appeal with a hearing is " + feeHearing.getFeeForDisplay()
        );
        asylumCase.write(FEE_HEARING_AMOUNT_FOR_DISPLAY, feeHearing.getFeeForDisplay());
        asylumCase.write(
            APPEAL_FEE_WITHOUT_HEARING_DESC,
            "The fee for an appeal without a hearing is " + feeWithoutHearing.getFeeForDisplay()
        );
        asylumCase.write(FEE_WITHOUT_HEARING_AMOUNT_FOR_DISPLAY, feeWithoutHearing.getFeeForDisplay());
        asylumCase.write(PAYMENT_STATUS, PAYMENT_DUE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public boolean isNotValidAppealType(AsylumCase asylumCase) {

        boolean isNotValidAppealType = true;

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElse(null);

        if (appealType != null) {
            if (appealType.equals(AppealType.EA)
                || appealType.equals(AppealType.HU)
                || appealType.equals(AppealType.PA)) {

                isNotValidAppealType = false;
            }
        }
        return isNotValidAppealType;
    }
}
