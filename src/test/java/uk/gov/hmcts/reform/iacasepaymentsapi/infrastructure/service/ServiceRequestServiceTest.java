package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.ServiceRequestApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.IdentityManagerResponseException;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class ServiceRequestServiceTest {

    private static final String CODE = "some-code";
    private static final String DESCRIPTION = "some-description";
    private static final String VERSION = "some-version";
    private static final BigDecimal CALCULATED_AMOUNT = new BigDecimal(80);
    private static final String APPELLANT_GIVEN_NAMES = "Name";
    private static final String APPELLANT_FAMILY_NAMES = "Surname";
    private static final String CALLBACK_URL = "some-callback-url";
    private static final String APPEAL_REFERENCE_NUMBER = "EA/00001/01";
    private static final long CASE_ID = 1111222233334444L;

    @Mock SystemTokenGenerator systemTokenGenerator;
    @Mock AuthTokenGenerator serviceAuthorization;
    @Mock ServiceRequestApi serviceRequestApi;

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;
    private String token = "token";
    private String serviceToken = "Bearer serviceToken";
    private static final String ERROR_TEST_MESSAGE = "error log test message";


    ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> serviceTokenCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<ServiceRequestRequest> serviceRequestRequestArgumentCaptor = ArgumentCaptor
        .forClass(ServiceRequestRequest.class);

    private Fee fee;

    @InjectMocks
    private ServiceRequestService serviceRequestService;

    @BeforeEach
    void setup() {
        serviceRequestService = new ServiceRequestService(systemTokenGenerator,
                                                          serviceAuthorization,
                                                          serviceRequestApi,
                                                          CALLBACK_URL);

        fee = new Fee(CODE, DESCRIPTION, VERSION, CALCULATED_AMOUNT);
    }

    @Test
    void service_should_throw_on_unable_to_generate_system_user_token() {

        when(systemTokenGenerator.generate()).thenThrow(IdentityManagerResponseException.class);

        assertThrows(IdentityManagerResponseException.class, () -> systemTokenGenerator.generate());
    }

    @Test
    void service_should_throw_on_unable_to_generate_s2s_token() {

        when(systemTokenGenerator.generate()).thenReturn("aSystemUserToken");
        when(serviceAuthorization.generate()).thenThrow(IdentityManagerResponseException.class);

        assertThrows(IdentityManagerResponseException.class, () -> serviceAuthorization.generate());
    }

    @Test
    void should_create_request_for_service_request_and_get_response_for_service_request() throws Exception {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of(APPELLANT_GIVEN_NAMES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(APPELLANT_FAMILY_NAMES));
        when(caseDetails.getId()).thenReturn(CASE_ID);

        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        ServiceRequestResponse expectedResponse = ServiceRequestResponse.builder().serviceRequestReference("1234").build();
        when(serviceRequestApi.createServiceRequest(eq(token), eq(serviceToken), any(ServiceRequestRequest.class)))
            .thenReturn(expectedResponse);

        ServiceRequestResponse actualResponse = serviceRequestService.createServiceRequest(callback, fee);

        assertEquals(expectedResponse, actualResponse);
        verify(serviceRequestApi, times(1)).createServiceRequest(tokenCaptor.capture(),
                                                                 serviceTokenCaptor.capture(),
                                                                 serviceRequestRequestArgumentCaptor.capture());

        assertEquals("token", tokenCaptor.getValue());
        assertEquals("Bearer serviceToken", serviceTokenCaptor.getValue());

        ServiceRequestRequest actual = serviceRequestRequestArgumentCaptor.getValue();
        assertEquals("some-callback-url", actual.getCallBackUrl());
        assertEquals("payment", actual.getCasePaymentRequest().getAction());
        assertEquals("Name Surname", actual.getCasePaymentRequest().getResponsibleParty());
        assertEquals("EA/00001/01", actual.getCaseReference());
        assertEquals("1111222233334444", actual.getCcdCaseNumber());
        assertEquals("some-version", actual.getFees()[0].getVersion());
        assertEquals("some-code", actual.getFees()[0].getCode());
        assertEquals(1, actual.getFees()[0].getVolume());
        assertEquals(80, actual.getFees()[0].getCalculatedAmount().intValue());
    }

    @Test
    void should_throw_feign_exception_service_request_api_throws() throws Exception {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of(APPELLANT_GIVEN_NAMES));
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(APPEAL_REFERENCE_NUMBER));
        when(asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of(APPELLANT_FAMILY_NAMES));
        when(caseDetails.getId()).thenReturn(CASE_ID);

        when(systemTokenGenerator.generate()).thenReturn(token);
        when(serviceAuthorization.generate()).thenReturn(serviceToken);
        when(serviceRequestApi.createServiceRequest(eq(token), eq(serviceToken), any(ServiceRequestRequest.class)))
            .thenThrow(FeignException.class);

        assertThrows(FeignException.class, () -> serviceRequestService.createServiceRequest(callback, fee));
        verify(serviceRequestApi, times(1)).createServiceRequest(tokenCaptor.capture(),
                                                                 serviceTokenCaptor.capture(),
                                                                 serviceRequestRequestArgumentCaptor.capture());

        assertEquals("token", tokenCaptor.getValue());
        assertEquals("Bearer serviceToken", serviceTokenCaptor.getValue());
    }

    @Test
    void should_return_null_in_recover_method() throws Exception {
        var ex = mock(FeignException.class);
        when(ex.getMessage()).thenReturn(ERROR_TEST_MESSAGE);
        ServiceRequestResponse response = serviceRequestService.recover(ex, callback, fee);
        assertNull(response);
    }
}
