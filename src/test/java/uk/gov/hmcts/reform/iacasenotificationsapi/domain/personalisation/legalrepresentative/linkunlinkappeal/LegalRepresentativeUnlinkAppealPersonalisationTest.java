package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative.linkunlinkappeal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeUnlinkAppealPersonalisationTest {

    @Mock
    private AppealService appealService;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    @Mock
    private EmailAddressFinder emailAddressFinder;

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";

    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";

    private LegalRepresentativeUnlinkAppealPersonalisation personalisation;

    @Before
    public void setUp() {
        personalisation = new LegalRepresentativeUnlinkAppealPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            iaExUiFrontendUrl,
            emailAddressFinder,
            customerServicesProvider,
            appealService);
    }


    @Test
    public void getTemplateId() {
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(beforeListingTemplateId, personalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(afterListingTemplateId, personalisation.getTemplateId(asylumCase));
    }

    @Test
    public void getRecipientsList() {
        String emailAddress = "legal@example.com";
        when(emailAddressFinder.getLegalRepEmailAddress(eq(asylumCase))).thenReturn(emailAddress);

        assertTrue(personalisation.getRecipientsList(asylumCase).contains(emailAddress));
    }

    @Test
    public void getReferenceId() {
        long caseId = 12345L;
        assertEquals(caseId + "_LEGAL_REPRESENTATIVE_UNLINK_APPEAL", personalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }


    @Test
    public void getPersonalisation() {
        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String appellantGivenNames = "someAppellantGivenNames";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        String appellantFamilyName = "someAppellantFamilyName";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String legalRefNumber = "someLegalRefNumber";
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRefNumber));
        String ariaListingReference = "LP/12345/2019";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        Map<String, String> personalisation = this.personalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(legalRefNumber, personalisation.get("legalRepReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void get_personalisation_when_values_are_no_present() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);

        Map<String, String> personalisation = this.personalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("legalRepReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

}