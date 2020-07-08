package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@Service
@Slf4j
public class RefDataService {

    private final RefDataApi refDataApi;
    private final IdamApi idamApi;
    private final RequestUserAccessTokenProvider userAuthorizationProvider;
    private final AuthTokenGenerator serviceAuthorizationProvider;

    public RefDataService(
        RefDataApi refDataApi,
        RequestUserAccessTokenProvider userAuthorizationProvider,
        AuthTokenGenerator serviceAuthorizationProvider,
        IdamApi idamApi
    ) {
        this.refDataApi = refDataApi;
        this.idamApi = idamApi;
        this.userAuthorizationProvider = userAuthorizationProvider;
        this.serviceAuthorizationProvider = serviceAuthorizationProvider;
    }

    public OrganisationResponse getOrganisationResponse() {

        String userToken = userAuthorizationProvider.getAccessToken();
        UserInfo userInfo = idamApi.userInfo("Bearer " + userToken);

        return refDataApi.findOrganisation(
            userAuthorizationProvider.getAccessToken(),
            serviceAuthorizationProvider.generate(),
            userInfo.getEmail());
    }
}
