package uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.security.oauth2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.domain.service.IdamService;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
class IdamSystemUserProviderTest {

    @Mock
    private IdamApi idamApi;
    @Mock
    private IdamService idamService;
    @Mock
    private UserInfo userInfo;

    private final String token = "Bearer someHash";

    @Test
    void should_return_correct_user_id() {

        String expectedUserId = "someUserID";
        when(userInfo.getUid()).thenReturn(expectedUserId);
        when(idamService.getUserInfo(token)).thenReturn(userInfo);

        IdamSystemUserProvider idamSystemUserProvider = new IdamSystemUserProvider(idamService);

        String userId = idamSystemUserProvider.getSystemUserId(token);

        assertEquals(expectedUserId, userId);

        verify(idamService).getUserInfo(token);
    }

    @Test
    void should_throw_exception_when_auth_service_unavailable() {

        when(idamService.getUserInfo(token)).thenThrow(FeignException.class);

        IdamSystemUserProvider idamSystemUserProvider = new IdamSystemUserProvider(idamService);

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            () -> idamSystemUserProvider.getSystemUserId(token)
        );
        assertEquals("Could not get system user id from IDAM", thrown.getMessage());
    }
}

