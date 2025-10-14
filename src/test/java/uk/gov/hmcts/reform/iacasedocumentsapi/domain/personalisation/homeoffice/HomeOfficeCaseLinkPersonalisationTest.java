package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@ExtendWith(MockitoExtension.class)
class HomeOfficeCaseLinkPersonalisationTest {

    private final String beforeListingTemplateId = "beforeListingTemplateId";
    private final String afterListingTemplateId = "afterListingTemplateId";
    private final String iaExUiFrontendUrl = "http://localhost";
    private final String linkAppealEmailAddress = "someEmail@com";
    private final String customerServicesTelephone = "555 555 555";
    private final String customerServicesEmail = "cust.services@example.com";
    @Mock
    private AppealService appealService;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private HomeOfficeCaseLinkPersonalisation personalisation;

    @BeforeEach
    public void setUp() {
        personalisation = new HomeOfficeCaseLinkPersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            linkAppealEmailAddress,
            iaExUiFrontendUrl,
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
        assertTrue(personalisation.getRecipientsList(asylumCase).contains(linkAppealEmailAddress));
    }

    @Test
    public void getReferenceId() {
        long caseId = 12345L;
        assertEquals(caseId + "_CASE_LINK_APPEAL_HOME_OFFICE", personalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> personalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }


    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void getPersonalisation(YesOrNo isAda) {
        String appealReferenceNumber = "someReferenceNumber";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String appellantGivenNames = "someAppellantGivenNames";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        String appellantFamilyName = "someAppellantFamilyName";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String homeOfficeRefNumber = "someHomeOfficeRefNumber";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        String ariaListingReference = "LP/12345/2019";
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(personalisation);

        Map<String, String> personalisation = this.personalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
                ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void get_personalisation_when_values_are_no_present(YesOrNo isAda) {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(personalisation);

        Map<String, String> personalisation = this.personalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
        assertEquals("", personalisation.get("ariaListingReference"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
        assertEquals(isAda.equals(YesOrNo.YES)
                ? "Accelerated detained appeal"
                : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

}
