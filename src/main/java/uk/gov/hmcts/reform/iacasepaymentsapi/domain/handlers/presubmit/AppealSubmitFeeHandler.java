package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
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
import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.CreditAccountPayment;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Currency;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;

@Component
public class AppealSubmitFeeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final PaymentService paymentService;

    public AppealSubmitFeeHandler(
        PaymentService paymentService
    ) {
        this.paymentService = paymentService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL;
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

        String payForTheAppeal = asylumCase.read(PAY_FOR_THE_APPEAL, String.class)
            .orElseThrow(() -> new IllegalStateException("Pay for the appeal is not present"));

        if (payForTheAppeal.equals("payNow")) {
            String pbaAccountNumber = asylumCase.read(ACCOUNT_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("PBA account number is not present"));
            String paymentDescription = asylumCase.read(PAYMENT_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("Payment description is not present"));
            String customerReference = asylumCase.read(CUSTOMER_REFERENCE, String.class)
                .orElseThrow(() -> new IllegalStateException("Customer payment reference is not present"));
            String feeCode = asylumCase.read(FEE_CODE, String.class)
                .orElseThrow(() -> new IllegalStateException("Fee code is not present"));
            String feeDescription = asylumCase.read(FEE_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("Fee description is not present"));
            Integer feeVersion = asylumCase.read(FEE_VERSION, Integer.class)
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
            asylumCase.write(PAYMENT_STATUS, paymentResponse.getStatus());
            asylumCase.write(PAYMENT_REFERENCE, paymentResponse.getReference());
        } else {
            asylumCase.write(PAYMENT_STATUS, "Payment due");
        }


        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private PaymentResponse makePayment(CreditAccountPayment creditAccountPayment) {

        return paymentService.creditAccountPayment(creditAccountPayment);
    }
}
