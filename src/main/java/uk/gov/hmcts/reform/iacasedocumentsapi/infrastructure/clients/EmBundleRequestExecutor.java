package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.EmBundleRequest;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

@Slf4j
@Service
public class EmBundleRequestExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public EmBundleRequestExecutor(
        RestTemplate restTemplate,
        AuthTokenGenerator serviceAuthTokenGenerator,
        UserDetailsProvider userDetailsProvider
    ) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    public PreSubmitCallbackResponse<AsylumCase> post(
        final Callback<AsylumCase> payload,
        final String endpoint
    ) {
        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        log.info("Posting EM Bundle Request: caseID {}", payload.getCaseDetails().getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        EmBundleRequest<AsylumCase> emBundleRequest = new EmBundleRequest<>(payload);
        HttpEntity<EmBundleRequest<AsylumCase>> requestEntity = new HttpEntity<>(emBundleRequest, headers);

        PreSubmitCallbackResponse<AsylumCase> response;

        try {
            response =
                restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<AsylumCase>>() {
                        }
                    ).getBody();
        } catch (RestClientResponseException e) {
            throw new DocumentServiceResponseException(
                "Couldn't create bundle using API: " + endpoint,
                e
            );
        }

        log.info("Posted EM Bundle Request: caseID {}", payload.getCaseDetails().getId());

        return response;
    }
}
