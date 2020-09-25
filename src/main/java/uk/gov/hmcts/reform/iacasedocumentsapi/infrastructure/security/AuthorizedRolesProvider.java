package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();

}
