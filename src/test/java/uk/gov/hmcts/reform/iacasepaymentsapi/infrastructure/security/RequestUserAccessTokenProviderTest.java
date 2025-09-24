package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ExtendWith(MockitoExtension.class)
class RequestUserAccessTokenProviderTest {

    @Mock private HttpServletRequest httpServletRequest;

    private RequestUserAccessTokenProvider requestUserAccessTokenProvider;

    @BeforeEach
    public void setUp() {

        requestUserAccessTokenProvider = new RequestUserAccessTokenProvider();

        RequestContextHolder.setRequestAttributes(
            new ServletRequestAttributes(httpServletRequest)
        );
    }

    @Test
    void should_throw_if_no_http_request_present() {

        assertThatThrownBy(requestUserAccessTokenProvider::getAccessToken)
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("Request access token not present");

    }

    @Test
    void should_throw_if_not_authorization_present() {

        RequestContextHolder.resetRequestAttributes();

        assertThatThrownBy(() -> requestUserAccessTokenProvider.tryGetAccessToken())
            .hasMessage("No current HTTP request")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void get_access_token_from_http_request() {

        String expectedAccessToken = "access-token";

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(expectedAccessToken);

        String actualAccessToken = requestUserAccessTokenProvider.getAccessToken();

        assertEquals(expectedAccessToken, actualAccessToken);
    }

    @Test
    void try_get_access_from_http_request() {
        String expectedAccessToken = "access-token";

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(expectedAccessToken);

        Optional<String> optActualAccessToken = requestUserAccessTokenProvider.tryGetAccessToken();

        assertTrue(optActualAccessToken.isPresent());
        assertEquals(expectedAccessToken, optActualAccessToken.get());
    }

    @Test
    void try_get_missing_access_token_from_http_request_returns_empty() {

        when(httpServletRequest.getHeader(AUTHORIZATION)).thenReturn(null);

        Optional<String> optionalAccessToken = requestUserAccessTokenProvider.tryGetAccessToken();

        assertFalse(optionalAccessToken.isPresent());
    }

}
