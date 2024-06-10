package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.LegRepAddressUk;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationEntityResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.OrganisationResponse;
import uk.gov.hmcts.reform.iacasepaymentsapi.domain.entities.fee.SuperUser;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@ExtendWith(MockitoExtension.class)
class RefDataServiceTest {
    @Mock
    private RefDataApi refDataApi;
    @Mock
    private RequestUserAccessTokenProvider userAuthorizationProvider;
    @Mock
    private AuthTokenGenerator serviceAuthorizationProvider;
    @Mock
    private IdamService idamService;
    @Mock
    private UserInfo userInfo;

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String townCity = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private RefDataService refDataService;

    @BeforeEach
    public void setUp() {
        refDataService = new RefDataService(
                refDataApi,
                userAuthorizationProvider,
                serviceAuthorizationProvider,
                idamService);
    }

    @Test
    void should_get_ref_data_organisation() {
        List<LegRepAddressUk> addresses = new ArrayList<>();
        LegRepAddressUk legRepAddressUk =  new LegRepAddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country,
            Arrays.asList("A","B")
        );
        addresses.add(legRepAddressUk);
        OrganisationEntityResponse organisationEntityResponse = new
            OrganisationEntityResponse("orgId", "orgName", "active",
                        "sraId", "sraReg", "CO123", "some-url",
                        new SuperUser("John", "Smith", "jsmith@test.com"),
                        Arrays.asList("PBA123456", "PBA765432"), addresses);

        OrganisationResponse mockResponse = new OrganisationResponse(organisationEntityResponse);

        when(userAuthorizationProvider.getAccessToken()).thenReturn("some-user-token");
        when(idamService.getUserInfo(any())).thenReturn(userInfo);
        when(userInfo.getEmail()).thenReturn("jsmith@test.com");
        when(refDataApi.findOrganisation(anyString(), any(), anyString())).thenReturn(
                mockResponse);

        OrganisationResponse orgResponse = refDataService.getOrganisationResponse();

        Assertions.assertNotNull(orgResponse);
        Assertions.assertEquals(mockResponse, orgResponse);

        Assertions.assertEquals("orgId", mockResponse.getOrganisationEntityResponse().getOrganisationIdentifier());
        Assertions.assertEquals("orgName", mockResponse.getOrganisationEntityResponse().getName());
        Assertions.assertEquals("active", mockResponse.getOrganisationEntityResponse().getStatus());
        Assertions.assertEquals("sraId", mockResponse.getOrganisationEntityResponse().getSraId());
        Assertions.assertEquals("sraReg", mockResponse.getOrganisationEntityResponse().getSraRegulated());
        Assertions.assertEquals("CO123", mockResponse.getOrganisationEntityResponse().getCompanyNumber());
        Assertions.assertEquals("some-url", mockResponse.getOrganisationEntityResponse().getCompanyUrl());
        Assertions.assertEquals("PBA123456", mockResponse.getOrganisationEntityResponse().getPaymentAccount().get(0));
        Assertions.assertEquals("PBA765432", mockResponse.getOrganisationEntityResponse().getPaymentAccount().get(1));

        AddressUk addressUk =  new AddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country
        );
        Assertions.assertEquals(addressLine1, addressUk.getAddressLine1().get());
        Assertions.assertEquals(addressLine2, addressUk.getAddressLine2().get());
        Assertions.assertEquals(addressLine3, addressUk.getAddressLine3().get());
        Assertions.assertEquals(postCode, addressUk.getPostCode().get());
        Assertions.assertEquals(townCity, addressUk.getPostTown().get());
        Assertions.assertEquals(county, addressUk.getCounty().get());
        Assertions.assertEquals(country, addressUk.getCountry().get());
    }
}
