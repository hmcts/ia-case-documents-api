package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();

}
