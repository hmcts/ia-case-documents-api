package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_AMOUNT;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_CODE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_DESCRIPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_PAYMENT_APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_VERSION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
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
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationEntityResponse;
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
                    Event.PAY_AND_SUBMIT_APPEAL,
                    Event.PAYMENT_APPEAL)
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

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        long caseId = callback.getCaseDetails().getId();

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(
                () -> new IllegalStateException("AppealType is not present for caseId: " + caseId)
            );
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.NO);

        Fee feeSelected = getFeeTypeWriteAppealPaymentDetailsToCaseData(appealType, asylumCase);

        log.info("Selected Fee for caseId: {}, {}", caseId, feeSelected);

        if (feeSelected != null) {

            writeFeeDetailsToCaseData(asylumCase, feeSelected);

            String pbaAccountNumber = asylumCase.read(PAYMENT_ACCOUNT_LIST, DynamicList.class)
                .map(DynamicList::getValue)
                .map(Value::getCode)
                .orElseThrow(
                    () -> new IllegalStateException("PBA account number is not present for caseId: " + caseId)
                );

            log.info("Fetching Organisation data for caseId: {}", caseId);
            OrganisationEntityResponse organisationResponse = refDataService
                .getOrganisationResponse()
                .getOrganisationEntityResponse();

            String paymentDescription = asylumCase.read(PAYMENT_DESCRIPTION, String.class)
                .orElseThrow(
                    () -> new IllegalStateException("Payment description is not present for caseId: " + caseId)
                );

            String orgName = organisationResponse.getName();
            List<String> pbaList = organisationResponse.getPaymentAccount();

            String pbaNumber = pbaList
                .stream()
                .filter(pba -> pba.equals(pbaAccountNumber))
                .findAny()
                .orElseThrow(
                    () -> new IllegalStateException("PBA account number is not valid for caseId: " + caseId)
                );

            String appealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(
                    () -> new IllegalStateException("Appeal reference number is not present for caseId: " + caseId)
                );
            String customerReference = asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)
                .orElseThrow(
                    () -> new IllegalStateException("Legal rep reference number is not present for caseId: " + caseId)
                );

            CreditAccountPayment creditAccountPayment = new CreditAccountPayment(
                pbaNumber,
                feeSelected.getCalculatedAmount(),
                appealReferenceNumber,
                String.valueOf(caseId),
                Currency.GBP,
                customerReference,
                paymentDescription,
                orgName,
                Service.IAC,
                paymentProperties.getSiteId(),
                Arrays.asList(feeSelected)
            );

            log.info("CreditAccountPayment for caseId: {}, payment object: {}", caseId, creditAccountPayment);

            PaymentResponse paymentResponse = makePayment(creditAccountPayment);

            log.info(
                "PaymentResponse for caseId: {}, payment response ref: {}, status: {}, PBA: {}",
                caseId,
                paymentResponse.getReference(),
                paymentResponse.getStatus(),
                pbaNumber
            );

            writePaymentResponseStatusToCaseData(paymentResponse, asylumCase);

            asylumCase.write(PAYMENT_REFERENCE, paymentResponse.getReference());
            asylumCase.write(PBA_NUMBER, pbaNumber);

            String pattern = "d MMM yyyy";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            asylumCase.write(PAYMENT_DATE, simpleDateFormat.format(paymentResponse.getDateCreated()));
        } else {
            throw new IllegalStateException("Cannot retrieve the fee from fees-register for caseId: " + caseId);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void writeFeeDetailsToCaseData(AsylumCase asylumCase, Fee fee) {

        String feeAmountInPence =
            String.valueOf(new BigDecimal(fee.getAmountAsString()).multiply(new BigDecimal("100")));
        asylumCase.write(FEE_CODE, fee.getCode());
        asylumCase.write(FEE_DESCRIPTION, fee.getDescription());
        asylumCase.write(FEE_VERSION, fee.getVersion());
        asylumCase.write(FEE_AMOUNT, feeAmountInPence);
        asylumCase.write(FEE_PAYMENT_APPEAL_TYPE, YesOrNo.YES);
    }

    public PaymentResponse makePayment(CreditAccountPayment creditAccountPayment) {

        requireNonNull(creditAccountPayment, "creditAccountPayment must not be null");

        try {
            log.info("Sending payment request for caseId: {}", creditAccountPayment.getCcdCaseNumber());
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
                (paymentResponse.getStatus().equals("Success") ? PAID : PAYMENT_PENDING)
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

                Fee feeWithHearing = feeService.getFee(FeeType.FEE_WITH_HEARING);
                asylumCase.write(FEE_WITH_HEARING, feeWithHearing.getAmountAsString());
                asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined with a hearing");

                return feeWithHearing;

            } else if (hearingFeeOption.equals(DECISION_WITHOUT_HEARING.value())) {

                Fee feeWithoutHearing = feeService.getFee(FeeType.FEE_WITHOUT_HEARING);
                asylumCase.write(PAYMENT_DESCRIPTION, "Appeal determined without a hearing");
                asylumCase.write(FEE_WITHOUT_HEARING, feeWithoutHearing.getAmountAsString());
                return feeWithoutHearing;
            }
        }
        return null;
    }
}
