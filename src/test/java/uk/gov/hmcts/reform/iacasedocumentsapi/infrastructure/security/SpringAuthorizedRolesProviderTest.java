package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
public class SpringAuthorizedRolesProviderTest {

    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;

    private final AuthorizedRolesProvider authorizedRolesProvider = new SpringAuthorizedRolesProvider();

    @BeforeEach
    public void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void should_return_empty_list_when_authentication_is_null() {

        assertEquals(Collections.emptySet(), authorizedRolesProvider.getRoles());
    }

    @Test
    public void should_return_empty_list_when_authorities_are_empty_null() {

        when(securityContext.getAuthentication()).thenReturn(authentication);
        assertEquals(Collections.emptySet(), authorizedRolesProvider.getRoles());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_return_empty_list_when_authorities_return_some_roles() {
        List grantedAuthorities = Lists.newArrayList(new SimpleGrantedAuthority("ccd-role"), new SimpleGrantedAuthority("ccd-admin"));
        when(authentication.getAuthorities()).thenReturn(grantedAuthorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        assertEquals(Sets.newHashSet("ccd-role", "ccd-admin"), authorizedRolesProvider.getRoles());
    }

    @After
    public void cleanUp() {
        SecurityContextHolder.clearContext();
    }
}
