package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.AsylumCaseDefinition.PAYMENT_STATUS;

import feign.FeignException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.CaseMetaData;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseDataContent;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.StartEventDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.entities.ccd.SubmitEventDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.clients.CcdDataApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.S2STokenValidator;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.SystemUserProvider;

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
    private static final String BAD_REQUEST_ERROR_MESSAGE =
        "400 BAD_REQUEST \"Payment reference not found for the caseId: 1234\"";
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
    private static final String APPEAL_REFERENCE_NUMBER_VALUE = "HU/50004/2021";
    private static final String PAYMENT_REFERENCE_KEY = "paymentReference";
    private static final String PAYMENT_REFERENCE_VALUE = "RC-1627-5070-9329-7815";
    private static final String INCORRECT_PAYMENT_REFERENCE_VALUE = "RC-1627-5070-9329-7823";
    private static final String PAYMENT_STATUS_SUCCESS_VALUE = "Success";
    private static final String PAYMENT_STATUS_PAID_VALUE = "Paid";
    private static final String PAYMENT_STATUS_FAILED_VALUE = "Failed";
    private static final String CALLBACK_COMPLETED = "CALLBACK_COMPLETED";

    private CcdDataService ccdDataService;

    @BeforeEach
    void setUp() {
        ccdDataService =
            new CcdDataService(
                ccdDataApi,
                systemTokenGenerator,
                systemUserProvider,
                serviceAuthorization,
                s2STokenValidator,
                false
            );
        setupCommonMocks();
    }

    @Test
    void service_should_throw_on_unable_to_generate_system_user_token() {
        when(systemTokenGenerator.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> systemTokenGenerator.generate());
    }

    @Test
    void service_should_throw_on_unable_to_generate_s2s_token() {
        when(serviceAuthorization.generate()).thenThrow(IdentityManagerResponseException.class);
        assertThrows(IdentityManagerResponseException.class, () -> serviceAuthorization.generate());
    }

    @Test
    void service_should_throw_on_unable_to_fetch_system_user_id() {
        when(systemUserProvider.getSystemUserId(BEARER_TOKEN)).thenThrow(IdentityManagerResponseException.class);
        CaseMetaData caseMetaData = getCaseMetaData(PAYMENT_STATUS_SUCCESS_VALUE, PAYMENT_REFERENCE_VALUE);
        assertThrows(IdentityManagerResponseException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaData, false, VALID_S2S_TOKEN));
    }

    @Test
    void service_should_error_on_incorrect_payment_reference() {
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.of(PAYMENT_REFERENCE_VALUE));
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
        CaseMetaData caseMetaData = getCaseMetaData(PAYMENT_STATUS_SUCCESS_VALUE, PAYMENT_REFERENCE_VALUE);
        assertThrows(FeignException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaData, false, VALID_S2S_TOKEN));
        verify(s2STokenValidator).checkIfServiceIsAllowed(VALID_S2S_TOKEN);
    }

    @ParameterizedTest
    @CsvSource({
        "PAYMENT_STATUS_SUCCESS_VALUE, PAYMENT_REFERENCE_VALUE, false",
        "PAYMENT_STATUS_PAID_VALUE, PAYMENT_REFERENCE_VALUE, true"
    })
    void service_should_update_the_payment_status_for_the_case_id(String paymentStatus, String paymentReference, boolean isWaysToPay) {
        CaseDataContent caseDataContent = getCaseDataContent(paymentStatus);
        setupEventAndSubmitMocks(paymentReference, caseDataContent);
        SubmitEventDetails submitEventDetails =
            assertDoesNotThrow(() -> ccdDataService.updatePaymentStatus(
                getCaseMetaData(paymentStatus, paymentReference), isWaysToPay, VALID_S2S_TOKEN));
        assertPaymentDetails(submitEventDetails);
        verifyInteractionsWithMocks(caseDataContent);
    }

    @Test
    void service_should_throw_exception_from_invalid_s2s_token() {
        doThrow(AccessDeniedException.class).when(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
        CaseMetaData caseMetaData = getCaseMetaData(PAYMENT_STATUS_SUCCESS_VALUE, PAYMENT_REFERENCE_VALUE);
        assertThrows(AccessDeniedException.class, () -> ccdDataService.updatePaymentStatus(
            caseMetaData, false, INVALID_S2S_TOKEN));
        verify(s2STokenValidator).checkIfServiceIsAllowed(INVALID_S2S_TOKEN);
    }

    private StartEventDetails getStartEventResponse(String paymentReference) {
        when(caseDetails.getId()).thenReturn(CASE_ID);
        when(caseDetails.getState()).thenReturn(State.APPEAL_SUBMITTED);
        when(caseDetails.getJurisdiction()).thenReturn(JURISDICTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(
            APPEAL_REFERENCE_NUMBER_VALUE));
        when(asylumCase.read(PAYMENT_REFERENCE, String.class)).thenReturn(Optional.of(paymentReference));
        when(asylumCase.read(PAYMENT_STATUS, String.class)).thenReturn(Optional.of(PAYMENT_STATUS_FAILED_VALUE));
        return new StartEventDetails(Event.UPDATE_PAYMENT_STATUS, EVENT_TOKEN, caseDetails);
    }

    private SubmitEventDetails getSubmitEventResponse() {
        Map<String, Object> data = new HashMap<>();
        data.put("appealReferenceNumber", APPEAL_REFERENCE_NUMBER_VALUE);
        data.put(PAYMENT_REFERENCE_KEY, PAYMENT_REFERENCE_VALUE);
        data.put(PAYMENT_STATUS.value(), PAYMENT_STATUS_SUCCESS_VALUE);
        return new SubmitEventDetails(CASE_ID, JURISDICTION, State.APPEAL_SUBMITTED, data,
                                      200, CALLBACK_COMPLETED
        );
    }

    private CaseDataContent getCaseDataContent(String paymentStatus) {
        Map<String, Object> data = new HashMap<>();
        data.put(PAYMENT_STATUS.value(), paymentStatus);
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("id", Event.UPDATE_PAYMENT_STATUS.toString());
        return new CaseDataContent(String.valueOf(CASE_ID), data, eventData, EVENT_TOKEN, true);
    }

    private CaseMetaData getCaseMetaData(String paymentStatus, String paymentReference) {
        return new CaseMetaData(Event.UPDATE_PAYMENT_STATUS, JURISDICTION, CASE_TYPE, CASE_ID,
                                paymentStatus, paymentReference
        );
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

    private void setupCommonMocks() {
        when(systemTokenGenerator.generate()).thenReturn(TOKEN);
        when(serviceAuthorization.generate()).thenReturn(SERVICE_TOKEN);
        when(systemUserProvider.getSystemUserId(BEARER_TOKEN)).thenReturn(USER_ID);
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
