package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeMakeAnApplicationPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock AppealService appealService;
    @Mock MakeAnApplicationService makeAnApplicationService;
    @Mock CustomerServicesProvider customerServicesProvider;
    @Mock UserDetailsProvider userDetailsProvider;
    @Mock UserDetails userDetails;


    private Long caseId = 12345L;
    private String makeAnApplicationLegalRepBeforeListingTemplateId = "beforeListTemplateId";
    private String makeAnApplicationLegalRepAfterListingTemplateId = "afterListTemplateId";
    private String makeAnApplicationLegalRepOtherPartyBeforeListingTemplateId = "otherPartyBeforeListTemplateId";
    private String makeAnApplicationLegalRepOtherPartyAfterListingTemplateId = "otherPartyAfterListTemplateId";

    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String applicationType = "someApplicationType";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String legalRepUser = "caseworker-ia-legalrep-solicitor";
    private String homeOfficeUser = "caseworker-ia-homeofficelart";

    private LegalRepresentativeMakeAnApplicationPersonalisation legalRepresentativeMakeAnApplicationPersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when(makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase)).thenReturn(applicationType);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        legalRepresentativeMakeAnApplicationPersonalisation = new LegalRepresentativeMakeAnApplicationPersonalisation(
            makeAnApplicationLegalRepBeforeListingTemplateId,
            makeAnApplicationLegalRepAfterListingTemplateId,
            makeAnApplicationLegalRepOtherPartyBeforeListingTemplateId,
            makeAnApplicationLegalRepOtherPartyAfterListingTemplateId,
            iaExUiFrontendUrl,
            customerServicesProvider,
            appealService,
            userDetailsProvider,
            makeAnApplicationService
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(userDetails.getRoles()).thenReturn(
                Arrays.asList(legalRepUser)
        );
        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(makeAnApplicationLegalRepBeforeListingTemplateId, legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(makeAnApplicationLegalRepAfterListingTemplateId, legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(userDetails.getRoles()).thenReturn(
                Arrays.asList(homeOfficeUser)
        );

        when(appealService.isAppealListed(asylumCase)).thenReturn(false);
        assertEquals(makeAnApplicationLegalRepOtherPartyBeforeListingTemplateId, legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(appealService.isAppealListed(asylumCase)).thenReturn(true);
        assertEquals(makeAnApplicationLegalRepOtherPartyAfterListingTemplateId, legalRepresentativeMakeAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_MAKE_AN_APPLICATION_LEGAL_REPRESENTATIVE", legalRepresentativeMakeAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase).contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeMakeAnApplicationPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> legalRepresentativeMakeAnApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = legalRepresentativeMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase)).thenReturn("");

        Map<String, String> personalisation = legalRepresentativeMakeAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
