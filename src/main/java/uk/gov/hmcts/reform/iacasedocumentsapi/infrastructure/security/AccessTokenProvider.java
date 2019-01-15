package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import java.util.Optional;

public interface AccessTokenProvider {

    String getAccessToken();

    Optional<String> tryGetAccessToken();
}
