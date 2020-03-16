package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam;

import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;

import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class IdamAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    public static final String REGISTRATION_ID = "oidc";

    static final String TOKEN_NAME = "tokenName";

    private final RestTemplate restTemplate;

    private final ClientRegistrationRepository clientRegistrationRepository;

    public IdamAuthoritiesConverter(RestTemplate restTemplate, ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {

        List<GrantedAuthority> authorities = new ArrayList<>();
        if (jwt.containsClaim(TOKEN_NAME) && jwt.getClaim(TOKEN_NAME).equals(ACCESS_TOKEN)) {
            log.info("processing token for user: {}", jwt.getClaimAsString("sub"));
            authorities.addAll(extractAuthorityFromClaims(getUserInfo(jwt.getTokenValue())));
        }
        return authorities;
    }

    private Map<String, Object> getUserInfo(String authorization) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID);
        String userInfoEndpointUri = registration.getProviderDetails().getUserInfoEndpoint().getUri();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + authorization);

        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        Map<String, Object> response;

        try {

            response = restTemplate
                .exchange(
                    userInfoEndpointUri,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }
                ).getBody();

        } catch (RestClientException ex) {
            if (ex instanceof HttpClientErrorException) {
                log.error(
                    "IDAM authentication error, response code: {}, response body: {}",
                    ((HttpClientErrorException) ex).getRawStatusCode(),
                    ((HttpClientErrorException) ex).getResponseBodyAsString()
                );
            }
            throw new IdentityManagerResponseException("Could not get user details from IDAM", ex);
        }

        return response;
    }

    private List<GrantedAuthority> extractAuthorityFromClaims(Map<String, Object> claims) {
        return Optional.ofNullable(claims.get("roles"))
            .filter(roles -> roles instanceof List<?>)
            .map(roles -> (List<?>) roles)
            .orElse(new ArrayList<>())
            .stream()
            .map(String::valueOf)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

}
