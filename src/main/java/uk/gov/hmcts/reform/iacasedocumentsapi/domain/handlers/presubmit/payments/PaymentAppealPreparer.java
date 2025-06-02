package uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.presubmit.payments;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.AG;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_PBA_ACCOUNTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_EJP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_ACCOUNT_LIST;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionOption.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.payment.PaymentStatus.PAYMENT_PENDING;

import feign.FeignException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RefDataService;

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

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
            && Arrays.asList(
            Event.PAYMENT_APPEAL,
            Event.PAY_AND_SUBMIT_APPEAL,
            Event.PAY_FOR_APPEAL,
            Event.RECORD_REMISSION_DECISION
        ).contains(callback.getEvent()))
            || isWaysToPay(callbackStage, callback);
    }

    // No payments for EJP Cases
    private boolean isWaysToPay(PreSubmitCallbackStage callbackStage,
                                Callback<AsylumCase> callback) {

        List<Event> waysToPayEvents = List.of(
            Event.SUBMIT_APPEAL,
            Event.GENERATE_SERVICE_REQUEST,
            Event.RECORD_REMISSION_DECISION
        );

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && waysToPayEvents.contains(callback.getEvent())
               && isHuEaEuPaAgAda(callback.getCaseDetails().getCaseData())
               && !isEjpCase(callback.getCaseDetails().getCaseData());
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

        if (hasNoRemission(asylumCase) && isLegalRepJourney(asylumCase)) {

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
                    .toList();

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

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);

        if (callback.getEvent() != Event.RECORD_REMISSION_DECISION
            || (callback.getEvent() == Event.RECORD_REMISSION_DECISION
            && asylumCase.read(PAYMENT_STATUS).isEmpty())) {
            asylumCase.write(PAYMENT_STATUS, PAYMENT_PENDING);
        }

        Optional<YesOrNo> hasServiceRequestAlready = asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class);

        if (hasServiceRequestAlready.isEmpty()) {
            asylumCase.write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.NO);
        }

        if (isWaysToPay(callbackStage, callback)
            && hasServiceRequestAlready.orElse(YesOrNo.NO) != YesOrNo.YES
            && hasNoRemission(asylumCase)
            && isAdmin != YesOrNo.YES) {
            asylumCase.write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isLegalRepJourney(AsylumCase asylumCase) {
        return asylumCase.read(JOURNEY_TYPE, JourneyType.class)
            .map(journey -> journey == JourneyType.REP)
            .orElse(true);
    }

    private boolean isHuEaEuPaAgAda(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            boolean isNonAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
                .orElse(YesOrNo.NO).equals(YesOrNo.NO);
            return isNonAda && (List.of(HU, EA, EU, PA, AG).contains(appealType));
        }
        return false;
    }

    private boolean hasNoRemission(AsylumCase asylumCase) {
        Optional<RemissionType> optRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<RemissionDecision> optionalRemissionDecision =
            asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        return (!isRemissionExists(optRemissionType) && !hasAipJourneyRemission(asylumCase))
            || isDecisionPartiallyApprovedOrRejected(optionalRemissionDecision);
    }

    // This method uses the isEjp field which is set yes for EJP when a case is saved or no if paper form
    private boolean isEjpCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_EJP, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

    private boolean isRemissionExists(Optional<RemissionType> remissionType) {
        return remissionType.isPresent()
            && remissionType.get() != RemissionType.NO_REMISSION;
    }

    private boolean hasAipJourneyRemission(AsylumCase asylumCase) {
        Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
        Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);

        return (remissionOption.isPresent() && remissionOption.get() != NO_REMISSION)
            || (helpWithFeesOption.isPresent() && helpWithFeesOption.get() != WILL_PAY_FOR_APPEAL);
    }

    private boolean isDecisionPartiallyApprovedOrRejected(Optional<RemissionDecision> remissionDecision) {
        return remissionDecision
            .map(decision -> decision == RemissionDecision.PARTIALLY_APPROVED || decision == RemissionDecision.REJECTED
        ).orElse(false);
    }
}
