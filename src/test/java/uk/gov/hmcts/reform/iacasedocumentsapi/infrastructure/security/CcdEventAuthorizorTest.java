package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CcdEventAuthorizorTest {

    @Mock
    private AuthorizedRolesProvider authorizedRolesProvider;

    private final String role = "caseworker-ia";
    private Map<String, List<Event>> roleEventAccess;
    private CcdEventAuthorizor ccdEventAuthorizor;
    private static final String EVENT_NOT_ALLOWED = "Event 'unknown' not allowed";

    @BeforeEach
    public void setUp() {

        ccdEventAuthorizor =
            new CcdEventAuthorizor(
                ImmutableMap
                    .<String, List<Event>>builder()
                    .put("caseworker-role", List.of(Event.REQUEST_RESPONDENT_REVIEW, Event.SEND_DIRECTION))
                    .put("legal-role", List.of(Event.SUBMIT_APPEAL, Event.BUILD_CASE))
                    .put(role, List.of(Event.UNKNOWN))
                    .build(),
                authorizedRolesProvider
            );
    }

    @Test
    public void does_not_throw_access_denied_exception_if_role_is_allowed_access_to_event() {

        when(authorizedRolesProvider.getRoles()).thenReturn(
            Sets.newHashSet("some-unrelated-role", "legal-role")
        );

        assertThatCode(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .doesNotThrowAnyException();

        when(authorizedRolesProvider.getRoles()).thenReturn(
            Sets.newHashSet("caseworker-role", "some-unrelated-role")
        );

        assertThatCode(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .doesNotThrowAnyException();
    }

    @Test
    public void throw_access_denied_exception_if_role_not_allowed_access_to_event() {

        when(authorizedRolesProvider.getRoles()).thenReturn(
            Sets.newHashSet("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        when(authorizedRolesProvider.getRoles()).thenReturn(
            Sets.newHashSet("some-unrelated-role", "legal-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .hasMessage("Event 'sendDirection' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void throw_access_denied_exception_if_event_not_configured() {

        when(authorizedRolesProvider.getRoles()).thenReturn(
            Sets.newHashSet("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UPLOAD_RESPONDENT_EVIDENCE))
            .hasMessage("Event 'uploadRespondentEvidence' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void throw_access_denied_exception_if_user_has_no_roles() {

        when(authorizedRolesProvider.getRoles()).thenReturn(
            Collections.emptySet()
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    void should_not_throw_exception_when_event_is_allowed() {
        when(authorizedRolesProvider.getRoles()).thenReturn(Set.of(role));
        ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN);
        verify(authorizedRolesProvider, times(1)).getRoles();
    }

    @Test
    void should_throw_exception_when_provider_returns_empty_list() {
        when(authorizedRolesProvider.getRoles()).thenReturn(Collections.emptySet());
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
            Arguments.of(new ImmutableMap.Builder<String, List<Event>>().put("caseworker-ia", Collections.emptyList()).build(), EVENT_NOT_ALLOWED)
        );
    }

    @Test
    public void should_throw_exception_when_access_map_for_role_is_empty() {

        Map<String, List<Event>> roleEventAccess = new ImmutableMap.Builder<String, List<Event>>()
            .put(role, newArrayList())
            .build();

        ccdEventAuthorizor = new CcdEventAuthorizor(roleEventAccess, authorizedRolesProvider);

        AccessDeniedException thrown = assertThrows(
            AccessDeniedException.class,
            () -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UNKNOWN)
        );
        assertEquals("Event 'unknown' not allowed", thrown.getMessage());
    }
}
