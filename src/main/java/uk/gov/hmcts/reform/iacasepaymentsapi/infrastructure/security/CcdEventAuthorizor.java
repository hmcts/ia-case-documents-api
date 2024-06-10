package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.Event;

public class CcdEventAuthorizor {

    private final Map<String, List<Event>> roleEventAccess;
    private final AuthorizedRolesProvider authorizedRolesProvider;

    public CcdEventAuthorizor(
        Map<String, List<Event>> roleEventAccess,
        AuthorizedRolesProvider authorizedRolesProvider
    ) {
        this.roleEventAccess = roleEventAccess;
        this.authorizedRolesProvider = authorizedRolesProvider;
    }

    public void throwIfNotAuthorized(Event event) {

        List<String> requiredRoles = getRequiredRoles(event);
        Set<String> userRoles = authorizedRolesProvider.getRoles();

        if (requiredRoles.isEmpty()
            || userRoles.isEmpty()
            || Collections.disjoint(requiredRoles, userRoles)) {

            throw new AccessDeniedException("Event '" + event.toString() + "' not allowed");
        }
    }

    private List<String> getRequiredRoles(Event event) {

        return roleEventAccess
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().contains(event))
            .map(Map.Entry::getKey)
            .toList();
    }
}
