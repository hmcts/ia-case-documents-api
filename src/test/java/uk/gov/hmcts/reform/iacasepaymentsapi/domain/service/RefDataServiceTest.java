package uk.gov.hmcts.reform.iacasepaymentsapi.domain.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.RefDataApi;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.clients.model.idam.UserInfo;
import uk.gov.hmcts.reform.iacasepaymentsapi.infrastructure.security.RequestUserAccessTokenProvider;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RefDataServiceTest {
    @Mock
    private RefDataApi refDataApi;
    @Mock
    private RequestUserAccessTokenProvider userAuthorizationProvider;
    @Mock
    private AuthTokenGenerator serviceAuthorizationProvider;
    @Mock
    private IdamApi idamApi;
    @Mock
    private UserInfo userInfo;

    private String companyName = "LBC";
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
                idamApi);
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
        when(idamApi.userInfo(any())).thenReturn(userInfo);
        when(userInfo.getEmail()).thenReturn("jsmith@test.com");
        when(refDataApi.findOrganisation(anyString(), any(), anyString())).thenReturn(
                mockResponse);

        OrganisationResponse orgResponse = refDataService.getOrganisationResponse();

        assertNotNull(orgResponse);
        assertEquals(mockResponse, orgResponse);

        assertEquals("orgId", mockResponse.getOrganisationEntityResponse().getOrganisationIdentifier());
        assertEquals("orgName", mockResponse.getOrganisationEntityResponse().getName());
        assertEquals("active", mockResponse.getOrganisationEntityResponse().getStatus());
        assertEquals("sraId", mockResponse.getOrganisationEntityResponse().getSraId());
        assertEquals("sraReg", mockResponse.getOrganisationEntityResponse().getSraRegulated());
        assertEquals("CO123", mockResponse.getOrganisationEntityResponse().getCompanyNumber());
        assertEquals("some-url", mockResponse.getOrganisationEntityResponse().getCompanyUrl());
        assertEquals("PBA123456", mockResponse.getOrganisationEntityResponse().getPaymentAccount().get(0));
        assertEquals("PBA765432", mockResponse.getOrganisationEntityResponse().getPaymentAccount().get(1));

        AddressUk addressUk =  new AddressUk(
            addressLine1,
            addressLine2,
            addressLine3,
            townCity,
            county,
            postCode,
            country
        );
        assertEquals(addressLine1, addressUk.getAddressLine1().get());
        assertEquals(addressLine2, addressUk.getAddressLine2().get());
        assertEquals(addressLine3, addressUk.getAddressLine3().get());
        assertEquals(postCode, addressUk.getPostCode().get());
        assertEquals(townCity, addressUk.getPostTown().get());
        assertEquals(county, addressUk.getCounty().get());
        assertEquals(country, addressUk.getCountry().get());
    }
}
