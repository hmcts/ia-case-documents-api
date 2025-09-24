package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.AG;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_TYPE_CHANGED_WITH_REFUND_FLAG;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REFUND_CONFIRMATION_APPLIED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.SERVICE_REQUEST_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionOption.NO_REMISSION;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.ServiceRequestService;

@Component
@Slf4j
public class CreateServiceRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final ServiceRequestService serviceRequestService;
    private final FeeService feeService;

    public CreateServiceRequestHandler(
        ServiceRequestService serviceRequestService,
        FeeService feeService) {
        this.serviceRequestService = serviceRequestService;
        this.feeService = feeService;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.GENERATE_SERVICE_REQUEST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Fee fee = FeesHelper.findFeeByHearingType(feeService, asylumCase);

        YesOrNo requestFeeRemissionFlagForServiceRequest =
            asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)
            .orElse(YesOrNo.NO);

        PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);

        if (isWaysToPay(callback)
            && hasNoRemission(asylumCase)
            && requestFeeRemissionFlagForServiceRequest != YesOrNo.YES
            && (paymentStatus != PaymentStatus.PAID
            || asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES))
            && isAdmin != YesOrNo.YES) {
            ServiceRequestResponse serviceRequestResponse = serviceRequestService.createServiceRequest(callback, fee);
            asylumCase.write(SERVICE_REQUEST_REFERENCE, serviceRequestResponse.getServiceRequestReference());
            asylumCase.write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
            //Reseting the flag after service request is made. To make another service request decision type need to
            //be changed and 'refund' as fee tribunal action selected.
            asylumCase.clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
            asylumCase.clear(REFUND_CONFIRMATION_APPLIED);
        } else {
            log.warn("Skipping Service Request creation for the appeal. case reference: {}, paymentStatus: {}, "
                         + "requestFeeRemissionFlagForServiceRequest: {}", callback.getCaseDetails().getId(),
                     paymentStatus, requestFeeRemissionFlagForServiceRequest);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isWaysToPay(Callback<AsylumCase> callback) {

        List<Event> waysToPayEvents = List.of(Event.SUBMIT_APPEAL,
                                              Event.GENERATE_SERVICE_REQUEST,
                                              Event.RECORD_REMISSION_DECISION);

        return waysToPayEvents.contains(callback.getEvent())
               && isHuEaEuPaAg(callback.getCaseDetails().getCaseData());
    }

    private boolean isHuEaEuPaAg(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return List.of(HU, EA, EU, PA, AG).contains(appealType);
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

    private boolean isRemissionExists(Optional<RemissionType> remissionType) {
        return remissionType.isPresent() && remissionType.get() != RemissionType.NO_REMISSION;
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
