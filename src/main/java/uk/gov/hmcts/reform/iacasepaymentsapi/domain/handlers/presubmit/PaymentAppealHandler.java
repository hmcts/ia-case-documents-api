package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DATE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ERROR_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_FAILED_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PBA_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_DUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
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
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.Service;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.PaymentService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.config.PaymentProperties;

@Component
@Slf4j
public class PaymentAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeeService feeService;
    private final PaymentService paymentService;
    private final RefDataService refDataService;
    private final PaymentProperties paymentProperties;
    private final ObjectMapper objectMapper;

    public PaymentAppealHandler(
        FeeService feeService,
        PaymentService paymentService,
        RefDataService refDataService,
        PaymentProperties paymentProperties,
        ObjectMapper objectMapper
    ) {
        this.feeService = feeService;
        this.paymentService = paymentService;
        this.refDataService = refDataService;
        this.paymentProperties = paymentProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
               && (callback.getEvent() == Event.PAYMENT_APPEAL
                   || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("AppealType is not present"));
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.NO);

        Fee feeSelected = getFeeTypeWriteAppealPaymentDetailsToCaseData(appealType, asylumCase);

        log.info("Fee: {}", feeSelected);

        if (feeSelected != null) {

            writeFeeDetailsToCaseData(asylumCase, feeSelected);

            DynamicList pbaAccountNumber = asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class)
                .orElseThrow(() -> new IllegalStateException("PBA account number is not present"));

            String paymentDescription = asylumCase.read(PAYMENT_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("Payment description is not present"));

            String orgName = refDataService.getOrganisationResponse().getOrganisationEntityResponse().getName();
            String caseId = String.valueOf(callback.getCaseDetails().getId());

            String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("Appeal reference number is not present"));
            String customerReference = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("Legal rep reference number is not present"));

            CreditAccountPayment creditAccountPayment = new CreditAccountPayment(
                pbaAccountNumber.getValue().getCode(),
                feeSelected.getCalculatedAmount(),
                appealReferenceNumber,
                caseId,
                Currency.GBP,
                customerReference,
                paymentDescription,
                orgName,
                Service.IAC,
                paymentProperties.getSiteId(),
                Arrays.asList(feeSelected)
            );

            log.info("CreditAccountPayment: {}", creditAccountPayment);

            PaymentResponse paymentResponse = makePayment(creditAccountPayment);

            writePaymentResponseStatusToCaseData(paymentResponse, asylumCase);

            asylumCase.write(PAYMENT_REFERENCE, paymentResponse.getReference());
            asylumCase.write(PBA_NUMBER, pbaAccountNumber.getValue().getCode());

            String pattern = "d MMM yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            asylumCase.write(PAYMENT_DATE, simpleDateFormat.format(paymentResponse.getDateCreated()));
        } else {
            throw new IllegalStateException("Cannot retrieve the fee from fees-register.");
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void writeFeeDetailsToCaseData(AsylumCase asylumCase, Fee fee) {

        asylumCase.write(FEE_CODE, fee.getCode());
        asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
        asylumCase.write(FEE_VERSION, fee.getVersion());
        asylumCase.write(FEE_AMOUNT, fee.getCalculatedAmount().toString());
        asylumCase.write(FEE_AMOUNT_FOR_DISPLAY, fee.getFeeForDisplay());
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
    }

    public PaymentResponse makePayment(CreditAccountPayment creditAccountPayment) {

        requireNonNull(creditAccountPayment, "creditAccountPayment must not be null");

        try {
            return paymentService.creditAccountPayment(creditAccountPayment);
        } catch (FeignException fe) {
            log.error("Payment failed: {}", fe.getMessage());
            try {
                return objectMapper.readValue(fe.contentUTF8(), PaymentResponse.class);
            } catch (JsonProcessingException je) {
                log.error("Error parsing the failed payment response: {}", je.getMessage());
            }
        }
        return null;
    }

    public void writePaymentResponseStatusToCaseData(PaymentResponse paymentResponse, AsylumCase asylumCase) {

        requireNonNull(paymentResponse, "paymentResponse must not be null");

        if (paymentResponse.getStatus().equals("Failed")) {

            asylumCase.write(PAYMENT_STATUS, FAILED);
            asylumCase.write(PAYMENT_FAILED_FOR_DISPLAY, "Pending");

            asylumCase.write(PAYMENT_ERROR_CODE, paymentResponse
                .getStatusHistories()
                .get(paymentResponse.getStatusHistories().size() - 1)
                .getErrorCode());

            asylumCase.write(PAYMENT_ERROR_MESSAGE, paymentResponse
                .getStatusHistories()
                .get(paymentResponse.getStatusHistories().size() - 1)
                .getErrorMessage());

        } else {
            asylumCase.write(
                PAYMENT_STATUS,
                (paymentResponse.getStatus().equals("Success") ? PAID : PAYMENT_DUE)
            );
            asylumCase.clear(PAYMENT_FAILED_FOR_DISPLAY);
        }
    }

    public Fee getFeeTypeWriteAppealPaymentDetailsToCaseData(AppealType appealType, AsylumCase asylumCase) {

        if (appealType.equals(AppealType.EA)
            || appealType.equals(AppealType.HU)
            || appealType.equals(AppealType.PA)) {

            String hearingFeeOption = asylumCase
                .read(DECISION_HEARING_FEE_OPTION, String.class).orElse("");

            if (hearingFeeOption.equals(DECISION_WITH_HEARING.value())) {

                asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");
                return feeService.getFee(FeeType.FEE_WITH_HEARING);

            } else if (hearingFeeOption.equals(DECISION_WITHOUT_HEARING.value())) {

                asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
                return feeService.getFee(FeeType.FEE_WITHOUT_HEARING);
            }
        }
        return null;
    }
}
