package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;

@ExtendWith(MockitoExtension.class)
class CcdEventAuthorizorTest {

    @Mock
    private AuthorizedRolesProvider authorizedRolesProvider;

    private final String role = "caseworker-ia";
    private Map<String, List<Event>> roleEventAccess;
    private CcdEventAuthorizor ccdEventAuthorizor;
    private static final String EVENT_NOT_ALLOWED = "Event 'unknown' not allowed";

    @BeforeEach
    void setUp() {
        roleEventAccess = new ImmutableMap.Builder<String, List<Event>>()
            .put(role, newArrayList(Event.UNKNOWN))
            .build();
        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);
    }

    @Test
    void should_not_throw_exception_when_event_is_allowed() {
        when(authorizedRolesProvider.getRoles()).thenReturn(newHashSet(role));
        ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN);
        verify(authorizedRolesProvider, times(1)).getRoles();
    }

    @Test
    void should_throw_exception_when_provider_returns_empty_list() {
        when(authorizedRolesProvider.getRoles()).thenReturn(newHashSet());
        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );
        assertEquals(EVENT_NOT_ALLOWED, thrown.getMessage());
    }

    @ParameterizedTest
    @MethodSource("provideScenariosForDeniedAccess")
    void should_throw_exception_when_access_map_is_incorrect(Map<String, List<Event>> roleEventAccess, String expectedMessage) {
        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);
        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );
        assertEquals(expectedMessage, thrown.getMessage());
    }

    private static Stream<Arguments> provideScenariosForDeniedAccess() {
        return Stream.of(
            Arguments.of(new ImmutableMap.Builder<String, List<Event>>().build(), EVENT_NOT_ALLOWED),
            Arguments.of(new ImmutableMap.Builder<String, List<Event>>().put("caseworker-ia", newArrayList()).build(), EVENT_NOT_ALLOWED)
        );
    }
}
