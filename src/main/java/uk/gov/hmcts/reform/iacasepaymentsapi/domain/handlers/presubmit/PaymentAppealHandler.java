package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PBA_NUMBER;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Currency;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;

@Component
public class PaymentAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;
    private Fee fee;
    private final PaymentService paymentService;

    public PaymentAppealHandler(
        FeeService feeService,
        PaymentService paymentService
    ) {
        this.feeService = feeService;
        this.paymentService = paymentService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                && (callback.getEvent() == Event.PAYMENT_APPEAL);
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

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.NO);

        if (appealType.equals(AppealType.EA)
            || appealType.equals(AppealType.HU)
            || appealType.equals(AppealType.PA)) {

            String hearingFeeOption = asylumCase
                .read(DECISION_HEARING_FEE_OPTION, String.class)
                .orElse("");

            Fee feeSelected = null;

            if (hearingFeeOption.equals(DECISION_WITH_HEARING.value())) {
                feeSelected = feeService.getFee(FeeType.FEE_WITH_HEARING);
                asylumCase.write(
                    PAYMENT_DESCRIPTION, "The fee for this type of appeal with a hearing is £"
                        + feeSelected.getCalculatedAmount());
            } else if (hearingFeeOption.equals(DECISION_WITHOUT_HEARING.value())) {
                feeSelected = feeService.getFee(FeeType.FEE_WITHOUT_HEARING);
                asylumCase.write(
                    PAYMENT_DESCRIPTION, "The fee for this type of appeal without a hearing is £"
                        + feeSelected.getCalculatedAmount());
            }

            if (feeSelected == null) {
                PreSubmitCallbackResponse<AsylumCase> response =
                    new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
                response.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));

                return response;
            }

            writeFeeDetailsToCaseData(asylumCase, feeSelected);

        }
        String pbaAccountNumber = asylumCase.read(PBA_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("PBA account number is not present"));
        String paymentDescription = asylumCase.read(PAYMENT_DESCRIPTION, String.class)
            .orElseThrow(() -> new IllegalStateException("Payment description is not present"));
        String customerReference = asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)
            .orElseThrow(() -> new IllegalStateException("Customer payment reference is not present"));
        String feeCode = asylumCase.read(FEE_CODE, String.class)
            .orElseThrow(() -> new IllegalStateException("Fee code is not present"));
        String feeDescription = asylumCase.read(FEE_DESCRIPTION, String.class)
            .orElseThrow(() -> new IllegalStateException("Fee description is not present"));
        String feeVersion = asylumCase.read(FEE_VERSION, String.class)
            .orElseThrow(() -> new IllegalStateException("Fee version is not present"));
        BigDecimal feeAmount = asylumCase.read(FEE_AMOUNT, BigDecimal.class)
            .orElseThrow(() -> new IllegalStateException("Fee amount is not present"));

        CreditAccountPayment creditAccountPayment = new CreditAccountPayment(
            pbaAccountNumber,
            feeAmount,
            null,
            Long.toString(callback.getCaseDetails().getId()),
            Currency.GBP,
            customerReference,
            paymentDescription,
            Arrays.asList(
                new Fee(
                    feeCode,
                    feeDescription,
                    feeVersion,
                    feeAmount
                ))
        );
        PaymentResponse paymentResponse = makePayment(creditAccountPayment);
        asylumCase.write(PAYMENT_STATUS, (paymentResponse.getStatus().equals("Success") ?  "Paid" : "Payment due"));
        asylumCase.write(PAYMENT_REFERENCE, paymentResponse.getReference());

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void writeFeeDetailsToCaseData(AsylumCase asylumCase, Fee fee) {

        asylumCase.write(FEE_CODE, fee.getCode());
        asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
        asylumCase.write(FEE_VERSION, fee.getVersion());
        asylumCase.write(FEE_AMOUNT, fee.getCalculatedAmount().toString());
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
    }

    private PaymentResponse makePayment(CreditAccountPayment creditAccountPayment) {

        return paymentService.creditAccountPayment(creditAccountPayment);
    }
}
