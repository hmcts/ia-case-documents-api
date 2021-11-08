package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HAS_PBA_ACCOUNTS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;

import feign.FeignException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.RefDataService;

@Slf4j
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
            Event.RECORD_REMISSION_DECISION
        ).contains(callback.getEvent());
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

        PreSubmitCallbackResponse<AsylumCase> response =
            new PreSubmitCallbackResponse<>(callback.getCaseDetails().getCaseData());

        Optional<RemissionType> optRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<RemissionDecision> optionalRemissionDecision =
            asylumCase.read(REMISSION_DECISION, RemissionDecision.class);
        if ((optRemissionType.isPresent() && optRemissionType.get() == RemissionType.NO_REMISSION)
            || optRemissionType.isEmpty()
            || (optionalRemissionDecision.isPresent() && optionalRemissionDecision.get() == RemissionDecision.REJECTED)
        ) {

            try {

                List<String> accountsFromOrg = refDataService.getOrganisationResponse()
                    .getOrganisationEntityResponse().getPaymentAccount();

                if (accountsFromOrg.isEmpty()) {
                    response.addErrors(Collections.singleton("There are no payment accounts"));

                    return response;
                }

                List<Value> accountListElements = accountsFromOrg
                    .stream()
                    .map(idValue -> new Value(idValue, idValue))
                    .collect(Collectors.toList());

                DynamicList accountList = new DynamicList(accountListElements.get(0), accountListElements);
                asylumCase.write(PAYMENT_ACCOUNT_LIST, accountList);

            } catch (FeignException fe) {

                log.error("Error in calling Reference data service.");
                asylumCase.write(HAS_PBA_ACCOUNTS, YesOrNo.NO);
                ExceptionUtils.printRootCauseStackTrace(fe);
            }
        }

        Fee fee = FeesHelper.findFeeByHearingType(feeService, asylumCase);
        if (isNull(fee)) {

            response.addErrors(Collections.singleton("Cannot retrieve the fee from fees-register."));
            return response;
        }
        asylumCase.write(PAYMENT_STATUS, PAYMENT_PENDING);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

}
