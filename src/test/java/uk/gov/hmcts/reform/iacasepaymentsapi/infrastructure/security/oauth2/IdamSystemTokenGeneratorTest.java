package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.Token;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class IdamSystemTokenGeneratorTest {

    @Mock
    private IdamApi idamApi;

    @Mock
    private Token token;

    private final String systemUserName = "systemUserName";
    private final String systemUserPass = "systemUserPass";
    private final String idamRedirectUrl = "http://idamRedirectUrl";
    private final String systemUserScope = "systemUserScope";
    private final String idamClientId = "idamClientId";
    private final String idamClientSecret = "idamClientSecret";

    @Test
    void should_return_correct_token_from_idam() {

        String expectedToken = "systemUserTokenHash";

        when(token.getAccessToken()).thenReturn(expectedToken);
        when(idamApi.token(any(Map.class))).thenReturn(token);

        IdamSystemTokenGenerator idamSystemTokenGenerator = new IdamSystemTokenGenerator(
            systemUserName,
            systemUserPass,
            idamRedirectUrl,
            systemUserScope,
            idamClientId,
            idamClientSecret,
            idamApi
        );

        String idamToken = idamSystemTokenGenerator.generate();

        assertEquals(expectedToken, idamToken);

        ArgumentCaptor<Map<String, ?>> requestFormCaptor = ArgumentCaptor.forClass(Map.class);
        verify(idamApi).token(requestFormCaptor.capture());

        Map<String, ?> actualRequestEntity = requestFormCaptor.getValue();

        assertEquals(newArrayList("password"), actualRequestEntity.get("grant_type"));
        assertEquals(newArrayList(idamRedirectUrl), actualRequestEntity.get("redirect_uri"));
        assertEquals(newArrayList(idamClientId), actualRequestEntity.get("client_id"));
        assertEquals(newArrayList(idamClientSecret), actualRequestEntity.get("client_secret"));
        assertEquals(newArrayList(systemUserName), actualRequestEntity.get("username"));
        assertEquals(newArrayList(systemUserPass), actualRequestEntity.get("password"));
        assertEquals(newArrayList(systemUserScope), actualRequestEntity.get("scope"));

    }

    @Test
    void should_throw_exception_when_auth_service_unavailable() {

        when(idamApi.token(any(Map.class))).thenThrow(FeignException.class);

        IdamSystemTokenGenerator idamSystemTokenGenerator = new IdamSystemTokenGenerator(
            systemUserName,
            systemUserPass,
            idamRedirectUrl,
            systemUserScope,
            idamClientId,
            idamClientSecret,
            idamApi
        );

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            idamSystemTokenGenerator::generate
        );
        assertEquals("Could not get system user token from IDAM", thrown.getMessage());
    }
}
