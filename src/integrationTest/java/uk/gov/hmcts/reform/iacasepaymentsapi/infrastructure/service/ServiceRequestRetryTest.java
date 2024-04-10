package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.Application;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.Fee;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.payment.ServiceRequestRequest;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.ServiceRequestApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.service.exceptions.PaymentServiceRequestException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@SpringBootTest(classes = Application.class, webEnvironment = MOCK)
@ActiveProfiles("integration")
public class ServiceRequestRetryTest {
    private static final String APPELLANT_GIVEN_NAMES = "Name";
    private static final String APPELLANT_FAMILY_NAMES = "Surname";
    private static final String APPEAL_REFERENCE_NUMBER = "EA/00001/01";
    private static final long CASE_ID = 1111222233334444L;
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Autowired
    private ServiceRequestService serviceRequestService;
    @MockBean
    private ServiceRequestApi serviceRequestApi;
    @MockBean SystemTokenGenerator systemTokenGenerator;
    @MockBean AuthTokenGenerator serviceAuthorization;
    @MockBean private Callback<AsylumCase> callback;
    @MockBean private CaseDetails<AsylumCase> caseDetails;
    @MockBean private AsylumCase asylumCase;
    private String token = "token";
    private String serviceToken = "Bearer serviceToken";
    private Fee fee = mock(Fee.class);
    ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<String> serviceTokenCaptor = ArgumentCaptor.forClass(String.class);
    ArgumentCaptor<ServiceRequestRequest> serviceRequestRequestArgumentCaptor = ArgumentCaptor
            .forClass(ServiceRequestRequest.class);

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void shouldRetryThreeTimesOnFeignExceptionThenReturnNull() {
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
                .thenThrow(FeignException.FeignClientException.class);
        long start = System.currentTimeMillis();
        assertThrows(PaymentServiceRequestException.class, () -> serviceRequestService.createServiceRequest(callback, fee));
        long end = System.currentTimeMillis();
        verify(serviceRequestApi, times(3)).createServiceRequest(tokenCaptor.capture(),
                serviceTokenCaptor.capture(),
                serviceRequestRequestArgumentCaptor.capture());
        assertTrue("Response time should be greater than 6s as retries are 3s apart, but is " + (end - start),
                end - start > 6000);
    }
}
