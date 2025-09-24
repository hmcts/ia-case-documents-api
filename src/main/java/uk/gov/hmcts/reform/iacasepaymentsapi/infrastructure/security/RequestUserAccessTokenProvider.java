package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class RequestUserAccessTokenProvider implements AccessTokenProvider {

    public String getAccessToken() {
        return tryGetAccessToken()
            .orElseThrow(() -> new IllegalStateException("Request access token not present"));
    }

    public Optional<String> tryGetAccessToken() {

        if (RequestContextHolder.getRequestAttributes() == null) {
            throw new IllegalStateException("No current HTTP request");
        }

        return Optional
            .ofNullable((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            .map(ServletRequestAttributes::getRequest)
            .map(request -> request.getHeader(AUTHORIZATION));

    }
}
