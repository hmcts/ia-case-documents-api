package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class S2STokenValidator {

    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String BEARER = "Bearer ";

    @Value("${idam.s2s-authorised.services}")
    private final List<String> iaS2sAuthorisedServices;

    private final AuthTokenValidator authTokenValidator;

    public void checkIfServiceIsAllowed(String token) {
        String serviceName = authenticate(token);
        if (!Objects.nonNull(serviceName)) {
            throw new AccessDeniedException("Service name from S2S token ('ServiceAuthorization' header) is null");
        }
        if (!iaS2sAuthorisedServices.contains(serviceName)) {
            log.error("Service name '{}' was not recognised for S2S authentication. Please check s2s-authorised.services in application.yaml", serviceName);
            throw new AccessDeniedException("Service name from S2S token ('ServiceAuthorization' header) is not recognised.");
        }
    }

    private String authenticate(String authHeader) {
        String bearerAuthToken = getBearerToken(authHeader);
        return authTokenValidator.getServiceName(bearerAuthToken);
    }

    private String getBearerToken(String token) {
        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }

}
