package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.EmBundleRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@SuppressWarnings("unchecked")
class EmBundleRequestExecutorTest {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String ENDPOINT = "http://endpoint";
    private static final String SERVICE_TOKEN = randomAlphabetic(32);
    private static final String ACCESS_TOKEN = randomAlphabetic(32);

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private RestTemplate restTemplate;

    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;
    @Mock private Callback<AsylumCase> callback;
    @Mock private PreSubmitCallbackResponse<AsylumCase> callbackResponse;
    @Mock private ResponseEntity<PreSubmitCallbackResponse<AsylumCase>> responseEntity;


    private EmBundleRequestExecutor emBundleRequestExecutor;

    @BeforeEach
    void setUp() {
        emBundleRequestExecutor = new EmBundleRequestExecutor(
            restTemplate,
            serviceAuthTokenGenerator,
            userDetailsProvider
        );

        when(serviceAuthTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(ACCESS_TOKEN);
    }

    @Test
    void should_invoke_endpoint_with_given_payload_and_return_200_with_no_errors() {

        when(restTemplate
            .exchange(
                any(String.class),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )
        ).thenReturn(responseEntity);
        when(responseEntity.getBody()).thenReturn(callbackResponse);

        PreSubmitCallbackResponse<AsylumCase> response =
            emBundleRequestExecutor.post(
                callback,
                ENDPOINT

            );

        assertThat(response).isNotNull().isEqualTo(callbackResponse);

        ArgumentCaptor<HttpEntity> requestEntityCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        verify(restTemplate).exchange(
            eq(ENDPOINT),
            eq(HttpMethod.POST),
            requestEntityCaptor.capture(),
            any(ParameterizedTypeReference.class)
        );

        HttpEntity actualRequestEntity = requestEntityCaptor.getValue();

        final String actualContentTypeHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE);
        final String actualAcceptHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.ACCEPT);
        final String actualServiceAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(SERVICE_AUTHORIZATION);
        final String actualAuthorizationHeader = actualRequestEntity.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        final EmBundleRequest actualPostBody = (EmBundleRequest) actualRequestEntity.getBody();

        EmBundleRequest emBundleRequest = new EmBundleRequest(callback);

        assertThat(actualContentTypeHeader).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(actualAcceptHeader).isEqualTo(MediaType.APPLICATION_JSON_VALUE);
        assertThat(actualServiceAuthorizationHeader).isEqualTo(SERVICE_TOKEN);
        assertThat(actualAuthorizationHeader).isEqualTo(ACCESS_TOKEN);
        assertThat(actualPostBody).isEqualTo(emBundleRequest);
        assertThat(actualPostBody.getCaseTypeId()).isEqualTo("Asylum");
        assertThat(actualPostBody.getJurisdictionId()).isEqualTo("IA");
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> emBundleRequestExecutor.post(null, ENDPOINT))
            .hasMessage("payload must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> emBundleRequestExecutor.post(callback, null))
            .hasMessage("endpoint must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_handle_http_server_exception_when_calling_api() {

        HttpServerErrorException underlyingException = mock(HttpServerErrorException.class);

        when(restTemplate
            .exchange(
                eq(ENDPOINT),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> emBundleRequestExecutor.post(callback, ENDPOINT))
            .isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessageContaining("Couldn't create bundle using API")
            .hasCause(underlyingException);

    }

    @Test
    void should_handle_http_client_exception_when_calling_api() {
        HttpClientErrorException underlyingException = mock(HttpClientErrorException.class);

        when(restTemplate
            .exchange(
                eq(ENDPOINT),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            )).thenThrow(underlyingException);

        assertThatThrownBy(() -> emBundleRequestExecutor.post(callback, ENDPOINT))
            .isExactlyInstanceOf(DocumentServiceResponseException.class)
            .hasMessageContaining("Couldn't create bundle using API")
            .hasCause(underlyingException);
    }


}
