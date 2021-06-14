package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITHOUT_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.FEE_WITH_HEARING;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;

@Component
public class PaymentAppealPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final RefDataService refDataService;
    private final FeeService feeService;

    public PaymentAppealPreparer(
        RefDataService refDataService,
        FeeService feeService
    ) {
        this.feeService = feeService;
        this.refDataService = refDataService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
                && Arrays.asList(
                    Event.PAYMENT_APPEAL,
                    Event.PAY_AND_SUBMIT_APPEAL,
                    Event.PAY_FOR_APPEAL,
                    Event.RECORD_REMISSION_DECISION)
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

        Optional<RemissionType> optRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        if (optRemissionType.isPresent()
            && optRemissionType.get() == RemissionType.NO_REMISSION) {

            List<String> accountsFromOrg = refDataService.getOrganisationResponse()
                .getOrganisationEntityResponse().getPaymentAccount();

            if (accountsFromOrg.isEmpty()) {
                PreSubmitCallbackResponse<AsylumCase> response =
                    new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
                response.addErrors(Collections.singleton("There are no payment accounts"));

                return response;
            }

            List<Value> accountListElements = accountsFromOrg
                .stream()
                .map(idValue -> new Value(idValue, idValue))
                .collect(Collectors.toList());

            DynamicList accountList = new DynamicList(accountListElements.get(0), accountListElements);
            asylumCase.write(PAYMENT_ACCOUNT_LIST, accountList);
        }

        Optional<String> decisionHearingFeeOption = asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class);
        if (decisionHearingFeeOption.isPresent()) {

            if (decisionHearingFeeOption.get().equals("decisionWithHearing")) {

                Fee feeWithHearing = feeService.getFee(FeeType.FEE_WITH_HEARING);
                if ((feeWithHearing == null)) {
                    PreSubmitCallbackResponse<AsylumCase> response =
                        new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
                    response.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));

                    return response;
                }
                asylumCase.write(FEE_WITH_HEARING, feeWithHearing.getAmountAsString());
            } else if (decisionHearingFeeOption.get().equals("decisionWithoutHearing")) {

                Fee feeWithoutHearing = feeService.getFee(FeeType.FEE_WITHOUT_HEARING);
                if ((feeWithoutHearing == null)) {
                    PreSubmitCallbackResponse<AsylumCase> response =
                        new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());
                    response.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));

                    return response;
                }
                asylumCase.write(FEE_WITHOUT_HEARING, feeWithoutHearing.getAmountAsString());
            }
        }
        asylumCase.write(PAYMENT_STATUS, PAYMENT_PENDING);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
