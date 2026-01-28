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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;

@Slf4j
@Service
public class BundleRequestExecutor {

    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";

    private final RestTemplate restTemplate;
    private final AuthTokenGenerator serviceAuthTokenGenerator;
    private final UserDetailsProvider userDetailsProvider;

    public BundleRequestExecutor(RestTemplate restTemplate, AuthTokenGenerator serviceAuthTokenGenerator, UserDetailsProvider userDetailsProvider) {
        this.restTemplate = restTemplate;
        this.serviceAuthTokenGenerator = serviceAuthTokenGenerator;
        this.userDetailsProvider = userDetailsProvider;
    }

    public PreSubmitCallbackResponse<BundleCaseData> post(
        final Callback<BundleCaseData> payload,
        final String endpoint
    ) {
        log.info("BundleRequestExecutor.post: Starting POST request to endpoint={}", endpoint);

        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        log.info("BundleRequestExecutor.post: Generating service authorization token");
        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();

        log.info("BundleRequestExecutor.post: Retrieving user details");
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();
        log.info("BundleRequestExecutor.post: User details retrieved, userId={}", userDetails.getId());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<Callback<BundleCaseData>> requestEntity = new HttpEntity<>(payload, headers);

        PreSubmitCallbackResponse<BundleCaseData> response;

        try {
            log.info("BundleRequestExecutor.post: Executing REST exchange to endpoint={}", endpoint);
            response =
                restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
                        }
                    ).getBody();
            log.info("BundleRequestExecutor.post: REST exchange completed successfully for endpoint={}", endpoint);

        } catch (RestClientResponseException e) {
            log.info("BundleRequestExecutor.post: REST exchange failed for endpoint={}, statusCode={}, responseBody={}",
                endpoint, e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new DocumentServiceResponseException(
                "Couldn't create bundle using API: " + endpoint,
                e
            );
        }

        return response;

    }

}
