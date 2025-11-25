package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.idam.IdentityManagerResponseException;

@ExtendWith(MockitoExtension.class)
public class SystemUserAccessTokenProviderTest {

    @Mock
    private IdamApi idamApi;

    private SystemUserAccessTokenProvider systemUserAccessTokenProvider;

    private final String systemUsername = "test-system-user";
    private final String systemPassword = "test-system-password";
    private final String systemUserScope = "openid profile authorities acr roles create-user manage-user search-user";
    private final String idamRedirectUrl = "http://localhost:3002/oauth2/callback";
    private final String idamClientId = "test-client-id";
    private final String idamClientSecret = "test-client-secret";

    @BeforeEach
    public void setUp() {
        systemUserAccessTokenProvider = new SystemUserAccessTokenProvider(
            idamApi,
            systemUsername,
            systemPassword,
            systemUserScope,
            idamRedirectUrl,
            idamClientId,
            idamClientSecret
        );
    }

    @Test
    public void get_access_token_from_idam_successfully() {
        String expectedAccessToken = "test-access-token";
        String expectedBearerToken = "Bearer " + expectedAccessToken;
        Token tokenResponse = new Token(expectedAccessToken, "test-scope");

        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(tokenResponse);

        String actualAccessToken = systemUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedBearerToken, actualAccessToken);
    }

    @Test
    public void try_get_access_token_from_idam_successfully() {
        String expectedAccessToken = "test-access-token";
        String expectedBearerToken = "Bearer " + expectedAccessToken;
        Token tokenResponse = new Token(expectedAccessToken, "test-scope");

        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(tokenResponse);

        Optional<String> optionalAccessToken = systemUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optionalAccessToken.isPresent());
        assertEquals(expectedBearerToken, optionalAccessToken.get());
    }

    @Test
    public void get_access_token_throws_exception_when_idam_returns_error() {
        when(idamApi.token(org.mockito.ArgumentMatchers.any()))
            .thenThrow(FeignException.class);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.getAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void try_get_access_token_throws_exception_when_idam_returns_error() {
        when(idamApi.token(org.mockito.ArgumentMatchers.any()))
            .thenThrow(FeignException.class);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.tryGetAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void get_access_token_throws_exception_when_token_response_is_null() {
        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.getAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void try_get_access_token_throws_exception_when_token_response_is_null() {
        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(null);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.tryGetAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void get_access_token_throws_exception_when_access_token_is_null() {
        Token tokenResponse = new Token(null, "test-scope");

        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(tokenResponse);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.getAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void try_get_access_token_throws_exception_when_access_token_is_null() {
        Token tokenResponse = new Token(null, "test-scope");

        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(tokenResponse);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.tryGetAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void get_access_token_throws_exception_when_access_token_is_empty() {
        Token tokenResponse = new Token("", "test-scope");

        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(tokenResponse);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.getAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }

    @Test
    public void try_get_access_token_throws_exception_when_access_token_is_empty() {
        Token tokenResponse = new Token("", "test-scope");

        when(idamApi.token(org.mockito.ArgumentMatchers.any())).thenReturn(tokenResponse);

        assertThatThrownBy(() -> systemUserAccessTokenProvider.tryGetAccessToken())
            .isExactlyInstanceOf(IdentityManagerResponseException.class)
            .hasMessage("Could not get system user token from IDAM");
    }
}
