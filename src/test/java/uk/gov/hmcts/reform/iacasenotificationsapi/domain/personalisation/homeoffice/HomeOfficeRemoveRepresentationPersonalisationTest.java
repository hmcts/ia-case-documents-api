package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class HomeOfficeRemoveRepresentationPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AppealService appealService;
    @Mock
    CustomerServicesProvider customerServicesProvider;

    private final Long caseId = 12345L;
    private final String templateIdBeforeListing = "beforeTemplateId";
    private final String templateIdAfterListing = "afterTemplateId";
    private final String iaExUiFrontendUrl = "http://somefrontendurl";
    private final String appealReferenceNumber = "someReferenceNumber";
    private final String ariaListingReference = "someReferenceNumber";
    private final String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";
    private final String homeOfficeEmail = "apchomeoffice@example.com";

    private final String addressLine1 = "A";
    private final String addressLine2 = "B";
    private final String addressLine3 = "C";
    private final String postTown = "D";
    private final String county = "E";
    private final String postCode = "F";
    private final String country = "G";

    private AddressUk addressUk = new AddressUk(
        addressLine1,
        addressLine2,
        addressLine3,
        postTown,
        county,
        postCode,
        country
    );

    private HomeOfficeRemoveRepresentationPersonalisation
        homeOfficeRemoveRepresentationPersonalisation;

    @BeforeEach
    public void setUp() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        homeOfficeRemoveRepresentationPersonalisation =
            new HomeOfficeRemoveRepresentationPersonalisation(
                templateIdBeforeListing,
                templateIdAfterListing,
                homeOfficeEmail,
                iaExUiFrontendUrl,
                appealService,
                customerServicesProvider
            );
    }

    @Test
    void should_return_given_email_address() {
        assertTrue(homeOfficeRemoveRepresentationPersonalisation.getRecipientsList(asylumCase)
            .contains(homeOfficeEmail));
    }

    @Test
    void should_return_given_template_id() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);

        assertEquals(templateIdBeforeListing, homeOfficeRemoveRepresentationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);

        assertEquals(templateIdAfterListing, homeOfficeRemoveRepresentationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    void should_return_given_reference_id() {
        assertEquals(caseId + "_REMOVE_REPRESENTATION_HOME_OFFICE",
            homeOfficeRemoveRepresentationPersonalisation.getReferenceId(caseId));
    }

    @Test
    void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            homeOfficeRemoveRepresentationPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    void should_return_correctly_formatted_company_address() {

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("A, B, C, D, E, F, G", homeOfficeRemoveRepresentationPersonalisation.formatCompanyAddress(asylumCase));
    }

    @Test
    void should_return_correctly_formatted_company_address_for_missing_fields() {

        AddressUk addressUk = new AddressUk(
            "",
            "",
            "",
            "",
            "",
            "",
            ""
        );

        when(asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class)).thenReturn(Optional.of(addressUk));

        assertEquals("", homeOfficeRemoveRepresentationPersonalisation.formatCompanyAddress(asylumCase));
    }

}
