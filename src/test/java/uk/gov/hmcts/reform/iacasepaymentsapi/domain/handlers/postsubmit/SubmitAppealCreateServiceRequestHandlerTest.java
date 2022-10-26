package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.postsubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType.NO_REMISSION;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit.ErrorHandler;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.ServiceRequestService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class SubmitAppealCreateServiceRequestHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;
    @Mock private ServiceRequestService serviceRequestService;
    @Mock private ServiceRequestResponse serviceRequestResponse;

    @Mock private ErrorHandler<AsylumCase> errorHandling;

    private SubmitAppealCreateServiceRequestHandler submitAppealCreateServiceRequestHandler;

    @BeforeEach
    public void setUp() {
        submitAppealCreateServiceRequestHandler =
            new SubmitAppealCreateServiceRequestHandler(serviceRequestService, feeService, Optional.of(errorHandling)
        );

        lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);
        lenient().when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
    }

    @Test
    void should_generate_service_request_when_can_handle_ea_appeal() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);

        PostSubmitCallbackResponse callbackResponse =
            submitAppealCreateServiceRequestHandler.handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
    }

    @Test
    void should_generate_service_request_when_can_handle_hu_appeal() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);

        PostSubmitCallbackResponse callbackResponse =
            submitAppealCreateServiceRequestHandler.handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
    }

    @Test
    void should_generate_service_request_when_can_handle_pa_appeal() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);

        PostSubmitCallbackResponse callbackResponse =
            submitAppealCreateServiceRequestHandler.handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
    }

    @Test
    void should_not_generate_service_request_when_flag_set_and_payment_status_paid() throws Exception {

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.YES));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAID));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);

        PostSubmitCallbackResponse callbackResponse =
            submitAppealCreateServiceRequestHandler.handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, never()).createServiceRequest(callback, feeWithHearing);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> submitAppealCreateServiceRequestHandler
            .handle(PostSubmitCallbackStage.CCD_SUBMITTED, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PostSubmitCallbackStage callbackStage : PostSubmitCallbackStage.values()) {

                boolean canHandle = submitAppealCreateServiceRequestHandler.canHandle(callbackStage, callback);

                if ((Arrays.asList(
                    Event.SUBMIT_APPEAL,
                    Event.GENERATE_SERVICE_REQUEST,
                    Event.RECORD_REMISSION_DECISION).contains(callback.getEvent())
                     && callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED)
                    || isWaysToPay(callbackStage, callback, true)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    private boolean isWaysToPay(PostSubmitCallbackStage callbackStage,
                                Callback<AsylumCase> callback,
                                boolean isLegalRepJourney) {
        return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
               && (callback.getEvent() == Event.SUBMIT_APPEAL
                   || callback.getEvent() == Event.GENERATE_SERVICE_REQUEST
                   || callback.getEvent() == Event.RECORD_REMISSION_DECISION)
               && isLegalRepJourney;
    }
}
