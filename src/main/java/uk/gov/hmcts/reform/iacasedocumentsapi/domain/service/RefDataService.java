package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.RefDataApi;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@Service
@Slf4j
public class RefDataService {

    private final RefDataApi refDataApi;
    private final uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.IdamService idamService;
    private final RequestUserAccessTokenProvider userAuthorizationProvider;
    private final AuthTokenGenerator serviceAuthorizationProvider;

    public RefDataService(
        RefDataApi refDataApi,
        RequestUserAccessTokenProvider userAuthorizationProvider,
        AuthTokenGenerator serviceAuthorizationProvider,
        IdamService idamService
    ) {
        this.refDataApi = refDataApi;
        this.userAuthorizationProvider = userAuthorizationProvider;
        this.serviceAuthorizationProvider = serviceAuthorizationProvider;
        this.idamService = idamService;
    }

    public OrganisationResponse getOrganisationResponse() {

        String userToken = userAuthorizationProvider.getAccessToken();
        UserInfo userInfo = idamService.getUserInfo(userToken);

        return refDataApi.findOrganisation(
            userToken,
            serviceAuthorizationProvider.generate(),
            userInfo.getEmail());
    }
}
