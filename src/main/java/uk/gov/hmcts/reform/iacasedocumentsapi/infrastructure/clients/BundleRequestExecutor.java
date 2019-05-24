package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.BundleCaseData;
import uk.gov.hmcts.reform.logging.exception.AlertLevel;

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

        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<Callback<BundleCaseData>> requestEntity = new HttpEntity<>(payload, headers);

        PreSubmitCallbackResponse<BundleCaseData> response;

        try {
            response =
                restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
                        }
                    ).getBody();

        } catch (HttpClientErrorException e) {

            log.error(e.getResponseBodyAsString());

            throw new DocumentServiceResponseException(
                AlertLevel.P2,
                "Couldn't create bundle using API: " + endpoint
                + "With Http Status: " + e.getStatusText()
                + " With response body" + e.getResponseBodyAsString(),
                e
            );
        }
        catch (RestClientException e) {

            log.error(e.getMessage());

            throw new DocumentServiceResponseException(
                AlertLevel.P2,
                "Couldn't create bundle using API: " + endpoint,
                e
            );
        }

        return response;

    }

}