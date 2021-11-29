package uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.oauth2;

import feign.FeignException;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.SystemUserProvider;

@Component
public class IdamSystemUserProvider implements SystemUserProvider {

    private final IdamApi idamApi;

    public IdamSystemUserProvider(IdamApi idamApi) {
        this.idamApi = idamApi;
    }

    @Override
    public String getSystemUserId(String userToken) {

        try {

            return idamApi.userInfo(userToken).getUid();

        } catch (FeignException ex) {

            throw new IdentityManagerResponseException("Could not get system user id from IDAM", ex);
        }

    }
}
