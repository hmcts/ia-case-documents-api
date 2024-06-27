package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.S2STokenValidator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemUserProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CcdDataServiceTest {
    @Mock
    private CcdDataApi ccdDataApi;
    @Mock
    private SystemTokenGenerator systemTokenGenerator;
    @Mock
    private SystemUserProvider systemUserProvider;
    @Mock
    private AuthTokenGenerator serviceAuthorization;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private S2STokenValidator s2STokenValidator;

    private static final String TOKEN = "token";
    private static final String BEARER_TOKEN = "Bearer token";
    private static final String SERVICE_TOKEN = "Bearer serviceToken";
    private static final String USER_ID = "userId";
    private static final String EVENT_TOKEN = "eventToken";
    private static final long CASE_ID = 1234;
    private static final String JURISDICTION = "IA";
    private static final String CASE_TYPE = "Asylum";
    private static final String EVENT_ID = "updatePaymentStatus";
    private static final String VALID_S2S_TOKEN = "VALID_S2S_TOKEN";
    private static final String INVALID_S2S_TOKEN = "INVALID_S2S_TOKEN";
    private static final String BAD_REQUEST_ERROR_MESSAGE =
        "400 BAD_REQUEST \"Payment reference not found for the caseId: 1234\"";
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "HU/50004/2021";
    private static final String PAYMENT_REFERENCE_KEY = "paymentReference";
    private static final String PAYMENT_REFERENCE_VALUE = "RC-1627-5070-9329-7815";
    private static final String INCORRECT_PAYMENT_REFERENCE_VALUE = "RC-1627-5070-9329-7823";
    private static final String PAYMENT_STATUS_SUCCESS_VALUE = "Success";
    private static final String PAYMENT_STATUS_PAID_VALUE = "Paid";
    private static final String PAYMENT_STATUS_FAILED_VALUE = "Failed";
    private static final String CALLBACK_COMPLETED = "CALLBACK_COMPLETED";
    private final CaseMetaData caseMetaDataSuccess = getCaseMetaData(PAYMENT_STATUS_SUCCESS_VALUE, PAYMENT_REFERENCE_VALUE);
    private final CaseMetaData caseMetaDataPaid = getCaseMetaData(PAYMENT_STATUS_PAID_VALUE, PAYMENT_REFERENCE_VALUE);
    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {
        ccdDataService =
            new CcdDataService(
                ccdDataApi,
                systemTokenGenerator,
                systemUserProvider,
                serviceAuthorization,
                s2STokenValidator
            );
        setupCommonMocks();
    }

    @Test
    void service_should_throw_on_unable_to_generate_system_user_token() {
        when(systemTokenGenerator.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaDataSuccess, false, VALID_S2S_TOKEN)
        );
    }

    @Test
    void service_should_throw_on_unable_to_generate_s2s_token() {
        when(serviceAuthorization.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaDataSuccess, false, VALID_S2S_TOKEN)
        );
    }


    @Test
    void service_should_throw_on_unable_to_fetch_system_user_id() {
        when(systemUserProvider.getSystemUserId(BEARER_TOKEN)).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaDataSuccess, false, VALID_S2S_TOKEN)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "PAYMENT_STATUS_SUCCESS_VALUE, PAYMENT_REFERENCE_VALUE, false",
        "PAYMENT_STATUS_PAID_VALUE, PAYMENT_REFERENCE_VALUE, true"
    })
    void service_should_update_the_payment_status_for_the_case_id(String paymentStatus, String paymentReference, boolean isWaysToPay) {
        CaseDataContent caseDataContent = getCaseDataContent(paymentStatus);
        setupEventAndSubmitMocks(paymentReference, caseDataContent);
        CaseMetaData caseMetaData = getCaseMetaData(paymentStatus, paymentReference);
        SubmitEventDetails submitEventDetails =
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                caseMetaData, isWaysToPay, VALID_S2S_TOKEN));
        assertPaymentDetails(submitEventDetails);
        verifyInteractionsWithMocks(caseDataContent);
    }

    @Test
    void service_should_error_on_incorrect_payment_reference() {
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class))
            .thenReturn(Optional.of(PAYMENT_REFERENCE_VALUE));

        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_STATUS_SUCCESS_VALUE);
        CaseMetaData caseMetaData = getCaseMetaData(PAYMENT_STATUS_SUCCESS_VALUE, INCORRECT_PAYMENT_REFERENCE_VALUE);
        setupEventAndSubmitMocks(PAYMENT_REFERENCE_VALUE, caseDataContent);
        assertThatThrownBy(() -> ccdDataService.updatePaymentStatus(caseMetaData, false, VALID_S2S_TOKEN))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining(BAD_REQUEST_ERROR_MESSAGE);
        verify(ccdDataApi, times(1))
            .startEvent(BEARER_TOKEN, SERVICE_TOKEN, USER_ID,
                        JURISDICTION, CASE_TYPE, String.valueOf(CASE_ID), EVENT_ID
            );
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_error_on_invalid_ccd_case_reference() {
        when(
            ccdDataApi.startEvent(
                BEARER_TOKEN, SERVICE_TOKEN, USER_ID,
                JURISDICTION, CASE_TYPE, String.valueOf(CASE_ID), EVENT_ID
            )).thenThrow(FeignException.class);
        assertThrows(FeignException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaDataSuccess, false, VALID_S2S_TOKEN));
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @Test
    void service_should_update_payment_if_is_waysToPay() {
        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_STATUS_PAID_VALUE);
        setupEventAndSubmitMocks(PAYMENT_REFERENCE_VALUE, caseDataContent);
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());

        SubmitEventDetails submitEventDetails =
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                caseMetaDataPaid, true, VALID_S2S_TOKEN));
        assertPaymentDetails(submitEventDetails);
        verifyInteractionsWithMocks(caseDataContent);
    }

    @Test
    void service_should_throw_exception_from_invalid_s2s_token() {
        doThrow(AccessDeniedException.class).when(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
        assertThrows(AccessDeniedException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaDataSuccess, false, INVALID_S2S_TOKEN));
        verify(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
    }

    @Test
    void service_should_throw_exception_if_no_appeal_type() {
        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_STATUS_PAID_VALUE);
        setupEventAndSubmitMocks(PAYMENT_REFERENCE_VALUE, caseDataContent);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_TYPE, AppealType.class)).thenReturn(Optional.empty());
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaDataSuccess, true, VALID_S2S_TOKEN),
                     "No appeal type in case data for case: " + CASE_ID
        );
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void service_should_not_throw_exception_if_validation_passes_PA_pay_later(State state) {
        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_STATUS_PAID_VALUE);
        setupEventAndSubmitMocks(PAYMENT_REFERENCE_VALUE, caseDataContent);
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(caseDetails.getState()).thenReturn(state);
        when(asylumCase.read(
            AsylumCaseDefinition.APPEAL_TYPE,
            AppealType.class
        )).thenReturn(Optional.of(AppealType.PA));
        when(asylumCase.read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of(
            "payLater"));
        when(asylumCase.read(
            AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION,
            String.class
        )).thenReturn(Optional.of("none"));
        assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
            caseMetaDataPaid, true, VALID_S2S_TOKEN));

        when(asylumCase.read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)).thenReturn(Optional.of(
            "none"));
        when(asylumCase.read(
            AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION,
            String.class
        )).thenReturn(Optional.of("payLater"));
        assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
            caseMetaDataPaid, true, VALID_S2S_TOKEN));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "APPEAL_STARTED",
        "APPEAL_SUBMITTED",
        "APPEAL_STARTED_BY_ADMIN",
        "PENDING_PAYMENT"
    })
    void service_should_not_throw_exception_if_validation_passes_non_PA_pay_later(State state) {
        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_STATUS_PAID_VALUE);
        setupEventAndSubmitMocks(PAYMENT_REFERENCE_VALUE, caseDataContent);
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        for (AppealType appealType : AppealType.values()) {
            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(
                AsylumCaseDefinition.APPEAL_TYPE,
                AppealType.class
            )).thenReturn(Optional.of(appealType));
            when(asylumCase.read(
                AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION,
                String.class
            )).thenReturn(Optional.of("none"));
            when(asylumCase.read(AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class)).thenReturn(
                Optional.of("none"));
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                caseMetaDataPaid, true, VALID_S2S_TOKEN));
        }
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "APPEAL_STARTED",
        "APPEAL_SUBMITTED",
        "APPEAL_STARTED_BY_ADMIN",
        "PENDING_PAYMENT"
    }, mode = EnumSource.Mode.EXCLUDE)
    void service_should_throw_exception_if_validation_fails(State state) {
        CaseDataContent caseDataContent = getCaseDataContent(PAYMENT_STATUS_PAID_VALUE);
        setupEventAndSubmitMocks(PAYMENT_REFERENCE_VALUE, caseDataContent);
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class)).thenReturn(Optional.empty());
        for (AppealType appealType : AppealType.values()) {
            when(caseDetails.getState()).thenReturn(state);
            when(asylumCase.read(
                AsylumCaseDefinition.APPEAL_TYPE,
                AppealType.class
            )).thenReturn(Optional.of(appealType));
            when(asylumCase.read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class))
                .thenReturn(Optional.of("none"));
            when(asylumCase.read(AsylumCaseDefinition.PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class))
                .thenReturn(Optional.of("none"));
            assertThrows(IllegalStateException.class, () -> ccdDataService.updatePaymentStatus(
                caseMetaDataPaid, true, VALID_S2S_TOKEN),
                         appealType.getValue() + " appeal payment should not be made at "
                             + state.toString() + " state for case: " + CASE_ID
            );
        }
    }

    private StartEventDetails getStartEventResponse(String paymentReference) {
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getJurisdiction()).thenReturn(JURISDICTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_REFERENCE, String.class))
            .thenReturn(Optional.of(paymentReference));
        when(asylumCase.read(AsylumCaseDefinition.PAYMENT_STATUS, String.class))
            .thenReturn(Optional.of(PAYMENT_STATUS_FAILED_VALUE));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_TYPE, AppealType.class))
            .thenReturn(Optional.of(AppealType.HU));
        return new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, EVENT_TOKEN, caseDetails);
    }

    private SubmitEventDetails getSubmitEventResponse() {
        Map<String, Object> data = new HashMap<>();
        data.put("appealReferenceNumber", APPEAL_REFERENCE_NUMBER_VALUE);
        data.put(PAYMENT_REFERENCE_KEY, PAYMENT_REFERENCE_VALUE);
        data.put(AsylumCaseDefinition.PAYMENT_STATUS.value(), PAYMENT_STATUS_SUCCESS_VALUE);
        return new SubmitEventDetails(CASE_ID, JURISDICTION, State.APPEAL_SUBMITTED, data,
                                      200, CALLBACK_COMPLETED
        );
    }

    private CaseDataContent getCaseDataContent(String paymentStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put(AsylumCaseDefinition.PAYMENT_STATUS.value(), paymentStatus);
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.UPDATE_PAYMENT_STATUS.toString());
        return new CaseDataContent(String.valueOf(CASE_ID), data, eventData, EVENT_TOKEN, true);
    }

    private CaseMetaData getCaseMetaData(String paymentStatus, String paymentReference) {
        return new CaseMetaData(Event.UPDATE_PAYMENT_STATUS, JURISDICTION, CASE_TYPE, CASE_ID,
                                paymentStatus, paymentReference
        );
    }

    private void setupCommonMocks() {
        when(systemTokenGenerator.generate()).thenReturn(TOKEN);
        when(serviceAuthorization.generate()).thenReturn(SERVICE_TOKEN);
        when(systemUserProvider.getSystemUserId(BEARER_TOKEN)).thenReturn(USER_ID);
    }

    private void verifyInteractionsWithMocks(CaseDataContent caseDataContent) {
        verify(ccdDataApi, times(1))
            .startEvent(BEARER_TOKEN, SERVICE_TOKEN, USER_ID,
                        JURISDICTION, CASE_TYPE, String.valueOf(CASE_ID), EVENT_ID
            );
        verify(ccdDataApi, times(1))
            .submitEvent(BEARER_TOKEN, SERVICE_TOKEN, String.valueOf(CASE_ID), caseDataContent);
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    private void setupEventAndSubmitMocks(String paymentReference, CaseDataContent caseDataContent) {
        StartEventDetails startEventResponse = getStartEventResponse(paymentReference);
        when(ccdDataApi.startEvent(
            BEARER_TOKEN, SERVICE_TOKEN, USER_ID, JURISDICTION, CASE_TYPE,
            String.valueOf(CASE_ID), EVENT_ID
        )).thenReturn(startEventResponse);
        when(ccdDataApi.submitEvent(BEARER_TOKEN, SERVICE_TOKEN, String.valueOf(CASE_ID),
                                    caseDataContent
        )).thenReturn(getSubmitEventResponse());
    }

    private static void assertPaymentDetails(SubmitEventDetails submitEventDetails) {
        assertNotNull(submitEventDetails);
        assertEquals(CASE_ID, submitEventDetails.getId());
        assertEquals(JURISDICTION, submitEventDetails.getJurisdiction());
        assertEquals(PAYMENT_STATUS_SUCCESS_VALUE, submitEventDetails.getData().get("paymentStatus"));
        assertEquals(PAYMENT_REFERENCE_VALUE, submitEventDetails.getData().get(PAYMENT_REFERENCE_KEY));
        assertEquals(200, submitEventDetails.getCallbackResponseStatusCode());
        assertEquals(CALLBACK_COMPLETED, submitEventDetails.getCallbackResponseStatus());
    }
}
