package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();
}
