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

        requireNonNull(payload, "payload must not be null");
        requireNonNull(endpoint, "endpoint must not be null");

        final String serviceAuthorizationToken = serviceAuthTokenGenerator.generate();
        final UserDetails userDetails = userDetailsProvider.getUserDetails();
        final String accessToken = userDetails.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.AUTHORIZATION, accessToken);
        headers.set(SERVICE_AUTHORIZATION, serviceAuthorizationToken);

        HttpEntity<Callback<BundleCaseData>> requestEntity = new HttpEntity<>(payload, headers);

        PreSubmitCallbackResponse<BundleCaseData> response;

        try {

            HttpEntity<String> responseString = restTemplate.exchange(
                "http://em-ccdorc-ia-case-api-pr-1569.preview.platform.hmcts.net",
                HttpMethod.GET,
                requestEntity,
                String.class,
                new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
                }
            );

            log.info("Response String" + responseString);

            HttpEntity<String> responseString2 = restTemplate.exchange(
                "http://em-ccdorc-ia-case-api-pr-1569.preview.platform.hmcts.net/api/stitch-ccd-bundles",
                HttpMethod.GET,
                requestEntity,
                String.class,
                new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
                }
            );

            log.info("Response String2" + responseString2);

            response =
                restTemplate
                    .exchange(
                        endpoint,
                        HttpMethod.POST,
                        requestEntity,
                        new ParameterizedTypeReference<PreSubmitCallbackResponse<BundleCaseData>>() {
                        }
                    ).getBody();

        } catch (RestClientResponseException e) {
            log.info("e: " + e);
            throw new DocumentServiceResponseException(
                "Couldn't create bundle using API: " + endpoint,
                e
            );
        }

        return response;

    }

}
