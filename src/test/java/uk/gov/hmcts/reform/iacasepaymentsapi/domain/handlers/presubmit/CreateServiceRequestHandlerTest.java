package uk.gov.hmcts.reform.iacasepaymentsapi.domain.handlers.presubmit;

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
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.DECISION_TYPE_CHANGED_WITH_REFUND_FLAG;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.HAS_SERVICE_REQUEST_ALREADY;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REFUND_CONFIRMATION_APPLIED;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.SERVICE_REQUEST_REFERENCE;
import static uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType.NO_REMISSION;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.FeeType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.PaymentStatus;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.service.FeeService;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.ServiceRequestService;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateServiceRequestHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    @Mock private FeeService feeService;
    @Mock private ServiceRequestService serviceRequestService;
    @Mock private ServiceRequestResponse serviceRequestResponse;

    private CreateServiceRequestHandler createServiceRequestHandler;

    @BeforeEach
    public void setUp() {
        createServiceRequestHandler =
            new CreateServiceRequestHandler(serviceRequestService, feeService
        );

        lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_can_handle_ea_appeal(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_can_handle_hu_appeal(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_can_handle_eu_appeal(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.EU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_can_handle_pa_appeal(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    @Test
    void should_not_generate_service_request_when_flag_set_and_payment_status_paid() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.YES));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAID));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, never()).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, never()).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        when(callback.getEvent()).thenReturn(Event.START_APPEAL);
        assertThatThrownBy(() -> createServiceRequestHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = createServiceRequestHandler.canHandle(callbackStage, callback);

                if ((callback.getEvent() == Event.GENERATE_SERVICE_REQUEST
                     && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    || isWaysToPay(callbackStage, callback, true)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_generate_service_request_when_case_is_DC_or_RP() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.DC));
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(0)).createServiceRequest(callback, feeWithHearing);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.RP));
        callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(0)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, never()).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @Test
    void should_not_generate_service_request_when_no_appeal_type() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(0)).createServiceRequest(callback, feeWithHearing);
    }


    @Test
    void should_not_generate_service_request_when_remission_not_rejected() {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.APPROVED));
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class))
            .thenReturn(Optional.of(PaymentStatus.PAYMENT_PENDING));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(0)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, never()).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, never()).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_remission_is_rejected(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.REJECTED));
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_remission_is_partially_approved(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.of(RemissionDecision.PARTIALLY_APPROVED));
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    @ParameterizedTest
    @MethodSource("providePaymentParameterValues")
    void should_generate_service_request_when_remission_type_is_noRemission(PaymentStatus paymentStatus, YesOrNo refundConfirmationApplied) {

        when(callback.getEvent()).thenReturn(Event.GENERATE_SERVICE_REQUEST);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.HU));
        when(asylumCase.read(REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.NO_REMISSION));
        when(asylumCase.read(REMISSION_DECISION, RemissionDecision.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.class)).thenReturn(Optional.of(
            YesOrNo.NO));
        when(asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)).thenReturn(Optional.of(paymentStatus));
        when(asylumCase.read(REFUND_CONFIRMATION_APPLIED, YesOrNo.class)).thenReturn(Optional.of(refundConfirmationApplied));
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        Fee feeWithHearing =
            new Fee("FEE0001", "Fee with hearing", "1", new BigDecimal("140"));
        when(asylumCase.read(DECISION_HEARING_FEE_OPTION, String.class)).thenReturn(Optional.of("decisionWithHearing"));
        when(feeService.getFee(FeeType.FEE_WITH_HEARING)).thenReturn(feeWithHearing);
        when(serviceRequestService.createServiceRequest(callback, feeWithHearing)).thenReturn(serviceRequestResponse);
        when(serviceRequestResponse.getServiceRequestReference()).thenReturn("serviceRequestResponse");

        PreSubmitCallbackResponse callbackResponse =
            createServiceRequestHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, times(1)).write(SERVICE_REQUEST_REFERENCE, "serviceRequestResponse");
        verify(asylumCase, times(1)).write(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.YES);

        assertNotNull(callbackResponse);
        verify(serviceRequestService, times(1)).createServiceRequest(callback, feeWithHearing);
        verify(asylumCase, times(1)).clear(DECISION_TYPE_CHANGED_WITH_REFUND_FLAG);
        verify(asylumCase, times(1)).clear(REFUND_CONFIRMATION_APPLIED);
    }

    private boolean isWaysToPay(PreSubmitCallbackStage callbackStage,
                                Callback<AsylumCase> callback,
                                boolean isLegalRepJourney) {
        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.GENERATE_SERVICE_REQUEST
               && isLegalRepJourney;
    }

    private static Stream<Arguments> providePaymentParameterValues() {
        return Stream.of(
            Arguments.of(PaymentStatus.PAYMENT_PENDING, YesOrNo.NO),
            Arguments.of(PaymentStatus.PAID, YesOrNo.YES)
        );
    }
}
