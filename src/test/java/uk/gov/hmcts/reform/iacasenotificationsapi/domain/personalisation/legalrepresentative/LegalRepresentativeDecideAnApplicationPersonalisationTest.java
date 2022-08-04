package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeDecideAnApplicationPersonalisationTest {

    private final String legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId =
        "grantedBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId =
        "grantedAfterListTemplateId";
    private final String legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId =
        "grantedOtherPartyBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId =
        "grantedOtherPartyAfterListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId =
        "refusedBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId =
        "refusedBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId =
        "refusedBeforeListTemplateId";
    private final String legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId =
        "refusedBeforeListTemplateId";
    @Mock
    AsylumCase asylumCase;
    @Mock
    CustomerServicesProvider customerServicesProvider;
    @Mock
    MakeAnApplicationService makeAnApplicationService;
    @Mock
    MakeAnApplication makeAnApplication;
    private Long caseId = 12345L;
    private String iaExUiFrontendUrl = "http://localhost";
    private String legalRepEmailAddress = "legalRep@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someReferenceNumber";
    private String legalRepRefNumber = "somelegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";
    private String customerServicesTelephone = "555 555 555";
    private String customerServicesEmail = "cust.services@example.com";
    private String legalRepUser = "caseworker-ia-legalrep-solicitor";
    private String homeOfficeUser = "caseworker-ia-homeofficelart";

    private LegalRepresentativeDecideAnApplicationPersonalisation legalRepresentativeDecideAnApplicationPersonalisation;

    @BeforeEach
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(legalRepRefNumber));
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class))
            .thenReturn(Optional.of(legalRepEmailAddress));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);
        when((makeAnApplicationService.getMakeAnApplication(asylumCase))).thenReturn(Optional.of(makeAnApplication));

        legalRepresentativeDecideAnApplicationPersonalisation =
            new LegalRepresentativeDecideAnApplicationPersonalisation(
                legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId,
                legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId,
                legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
                iaExUiFrontendUrl,
                customerServicesProvider,
                makeAnApplicationService
            );
    }

    @Test
    public void should_return_given_template_id() {
        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getApplicantRole()).thenReturn(legalRepUser);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");

        assertEquals(legalRepresentativeDecideAnApplicationGrantedBeforeListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
        when(makeAnApplication.getState()).thenReturn("listing");
        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        assertEquals(legalRepresentativeDecideAnApplicationGrantedAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedBeforeListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        when(makeAnApplication.getState()).thenReturn("listing");
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getApplicantRole()).thenReturn(homeOfficeUser);
        when(makeAnApplication.getDecision()).thenReturn("Granted");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertEquals(legalRepresentativeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        when(makeAnApplication.getState()).thenReturn("listing");
        assertEquals(legalRepresentativeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(false);
        when(makeAnApplication.getDecision()).thenReturn("Refused");
        when(makeAnApplication.getState()).thenReturn("appealSubmitted");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));

        when(makeAnApplicationService.isApplicationListed(any(State.class))).thenReturn(true);
        when(makeAnApplication.getState()).thenReturn("listing");
        assertEquals(legalRepresentativeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
            legalRepresentativeDecideAnApplicationPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_DECIDE_AN_APPLICATION_LEGAL_REPRESENTATIVE",
            legalRepresentativeDecideAnApplicationPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case() {
        assertTrue(legalRepresentativeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase)
            .contains(legalRepEmailAddress));
    }

    @Test
    public void should_throw_exception_when_cannot_find_email_address_for_legal_rep() {
        when(asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> legalRepresentativeDecideAnApplicationPersonalisation.getRecipientsList(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("legalRepresentativeEmailAddress is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> legalRepresentativeDecideAnApplicationPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            legalRepresentativeDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

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

        when(makeAnApplication.getDecisionReason()).thenReturn("No Reason Given");
        when(makeAnApplication.getDecisionMaker()).thenReturn("Judge");
        when(makeAnApplication.getType()).thenReturn("Other");

        when(makeAnApplicationService.getMakeAnApplication(asylumCase)).thenReturn(Optional.of(makeAnApplication));

        Map<String, String> personalisation =
            legalRepresentativeDecideAnApplicationPersonalisation.getPersonalisation(asylumCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
        assertEquals("Other", personalisation.get("applicationType"));
        assertEquals("No Reason Given", personalisation.get("applicationDecisionReason"));
        assertEquals("Judge", personalisation.get("decisionMaker"));
        assertEquals(iaExUiFrontendUrl, personalisation.get("linkToOnlineService"));

        assertEquals(customerServicesTelephone, customerServicesProvider.getCustomerServicesTelephone());
        assertEquals(customerServicesEmail, customerServicesProvider.getCustomerServicesEmail());
    }
}
