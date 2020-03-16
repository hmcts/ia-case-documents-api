package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.AccessTokenProvider;

public class OpenIdUserDetailsProvider implements UserDetailsProvider {

    private final AccessTokenProvider accessTokenProvider;
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String detailsOpenIdUri;

    public OpenIdUserDetailsProvider(
        AccessTokenProvider accessTokenProvider,
        RestTemplate restTemplate,
        String baseUrl,
        String detailsOpenIdUri
    ) {
        requireNonNull(baseUrl);
        requireNonNull(detailsOpenIdUri);

        this.accessTokenProvider = accessTokenProvider;
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
        this.detailsOpenIdUri = detailsOpenIdUri;
    }

    public IdamUserDetails getUserDetails() {

        final String accessToken = accessTokenProvider.getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, accessToken);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        Map<String, Object> response;

        try {

            response =
                restTemplate
                    .exchange(
                        baseUrl + detailsOpenIdUri,
                        HttpMethod.GET,
                        requestEntity,
                        new ParameterizedTypeReference<Map<String, Object>>() {
                        }
                    ).getBody();

        } catch (RestClientResponseException ex) {

            throw new IdentityManagerResponseException(
                "Could not get user details with IDAM",
                ex
            );
        }

        return getOpenIdUserDetails(accessToken, response);
    }

    private IdamUserDetails getOpenIdUserDetails(String accessToken, Map<String, Object> response) {

        Stream
            .of("uid", "roles", "sub", "given_name", "family_name")
            .forEach(field ->
                Optional
                    .ofNullable(response.get(field))
                    .orElseThrow(() -> new IllegalStateException("OpenId user details missing '" + field + "' field"))
            );

        return new IdamUserDetails(
            accessToken,
            String.valueOf(response.get("uid")),
            castRolesToList(response.get("roles")),
            (String) response.get("sub"),
            (String) response.get("given_name"),
            (String) response.get("family_name")
        );
    }

    private List<String> castRolesToList(Object untypedRoles) {
        return Optional.ofNullable(untypedRoles)
            .filter(roles -> roles instanceof List<?>)
            .map(roles -> (List<?>) roles)
            .orElse(new ArrayList<>())
            .stream()
            .map(String::valueOf)
            .collect(Collectors.toList());
    }
}
