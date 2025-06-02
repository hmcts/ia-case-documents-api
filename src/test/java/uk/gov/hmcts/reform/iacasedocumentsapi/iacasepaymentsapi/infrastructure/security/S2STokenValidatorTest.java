package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;

@ExtendWith(MockitoExtension.class)
class S2STokenValidatorTest {

    private static final List<String> IA_S2S_AUTH_SERVICES = List.of("iac", "payment_app");

    @Mock
    private AuthTokenValidator authTokenValidator;

    private S2STokenValidator s2STokenValidator;

    @BeforeEach
    public void setup() {
        s2STokenValidator = new S2STokenValidator(IA_S2S_AUTH_SERVICES, authTokenValidator);
    }

    @Test
    void givenServiceNameIsValid() {
        when(authTokenValidator.getServiceName("Bearer payment_app")).thenReturn("payment_app");
        s2STokenValidator.checkIfServiceIsAllowed("payment_app");
        verify(authTokenValidator).getServiceName("Bearer payment_app");
    }

    @Test
    void givenServiceNameIsNullFromToken() {
        when(authTokenValidator.getServiceName("Bearer TestService")).thenReturn(null);
        assertThrows(AccessDeniedException.class, () -> s2STokenValidator.checkIfServiceIsAllowed("TestService"));
    }

    @Test
    void givenServiceNameCouldNotBeFound() {
        when(authTokenValidator.getServiceName("Bearer TestService")).thenReturn("SERVICE_NAME_SHOULD_NOT_BE_FOUND");
        assertThrows(AccessDeniedException.class, () -> s2STokenValidator.checkIfServiceIsAllowed("TestService"));
    }

    @Test
    void givenServiceNameIsEmptyFromToken() {
        assertThrows(AccessDeniedException.class, () -> s2STokenValidator.checkIfServiceIsAllowed(""));
    }

    @Test
    void givenTokenDoesNotStartWithBearer() {
        String tokenWithoutBearer = "payment_app";
        when(authTokenValidator.getServiceName("Bearer " + tokenWithoutBearer)).thenReturn(tokenWithoutBearer);
        s2STokenValidator.checkIfServiceIsAllowed(tokenWithoutBearer);
        verify(authTokenValidator).getServiceName("Bearer " + tokenWithoutBearer);
    }
}
