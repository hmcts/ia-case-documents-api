package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeFtpaApplicationDecisionAppellantPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock EmailAddressFinder emailAddressFinder;

    private Long caseId = 12345L;
    private String homeOfficeEmailAddressFtpaGranted = "homeoffice-granted@example.com";
    private String homeOfficeEmailAddressFtpaRefused = "homeoffice-refused@example.com";
    private String homeOfficeEmailAddress = "homeoffice@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String ariaListingReference = "ariaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String otherPartyGrantedTemplateId = "otherPartyGrantedTemplateId";
    private String otherPartyPartiallyGrantedTemplateId = "otherPartyPartiallyGrantedTemplateId";
    private String otherPartyNotAdmittedTemplateId = "otherPartyNotAdmittedTemplateId";
    private String otherPartyRefusedTemplateId = "otherPartyRefusedTemplateId";
    private String otherPartyReheardTemplateId = "otherPartyReheardTemplateId";
    private String allowedTemplateId = "allowedTemplateId";
    private String dismissedTemplateId = "dismissedTemplateId";

    private FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
    private FtpaDecisionOutcomeType notAdmitted = FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
    private FtpaDecisionOutcomeType refused = FtpaDecisionOutcomeType.FTPA_REFUSED;
    private FtpaDecisionOutcomeType reheard = FtpaDecisionOutcomeType.FTPA_REHEARD35;
    private FtpaDecisionOutcomeType remade = FtpaDecisionOutcomeType.FTPA_REMADE32;
    private FtpaDecisionOutcomeType allowed = FtpaDecisionOutcomeType.FTPA_ALLOWED;
    private FtpaDecisionOutcomeType dismissed = FtpaDecisionOutcomeType.FTPA_DISMISSED;

    private HomeOfficeFtpaApplicationDecisionAppellantPersonalisation homeOfficeFtpaApplicationDecisionAppellantPersonalisation;

    @Before
    public void setup() {

        when(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase)).thenReturn(homeOfficeEmailAddress);

        homeOfficeFtpaApplicationDecisionAppellantPersonalisation = new HomeOfficeFtpaApplicationDecisionAppellantPersonalisation(
            otherPartyGrantedTemplateId,
            otherPartyPartiallyGrantedTemplateId,
            otherPartyNotAdmittedTemplateId,
            otherPartyRefusedTemplateId,
            otherPartyReheardTemplateId,
            allowedTemplateId,
            dismissedTemplateId,
            personalisationProvider,
            homeOfficeEmailAddressFtpaGranted,
            homeOfficeEmailAddressFtpaRefused,
            emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaAppellantDecisionOutcomeType is not present");
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(granted));
        assertEquals(otherPartyGrantedTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(partiallyGranted));
        assertEquals(otherPartyPartiallyGrantedTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(notAdmitted));
        assertEquals(otherPartyNotAdmittedTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(reheard));
        assertEquals(otherPartyReheardTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(allowed));
        assertEquals(allowedTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(dismissed));
        assertEquals(dismissedTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(refused));
        assertEquals(otherPartyRefusedTemplateId, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_APPELLANT", homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_for_granted_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_GRANTED));

        assertTrue(homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressFtpaGranted));
    }

    @Test
    public void should_return_given_email_address_for_partially_granted_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED));

        assertTrue(homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressFtpaGranted));
    }

    @Test
    public void should_return_given_email_address_for_refused_appeal_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REFUSED));

        assertTrue(homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase).contains(homeOfficeEmailAddressFtpaRefused));
    }

    @Test
    public void should_return_given_email_address_for_reheard35_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REHEARD35));

        assertEquals(Collections.singleton(homeOfficeEmailAddress), homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_email_address_for_reheard32_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REHEARD32));

        assertEquals(Collections.singleton(homeOfficeEmailAddress), homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_email_address_for_remade32_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REMADE32));

        assertEquals(Collections.singleton(homeOfficeEmailAddress), homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_the_ftpa_application_decision_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REMADE32));

        assertEquals(FtpaDecisionOutcomeType.FTPA_REMADE32, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getFtpaApplicationDecision(asylumCase));
    }

    @Test
    public void should_return_the_ftpa_application_rj_decision_outcome() {
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(FtpaDecisionOutcomeType.FTPA_REMADE32));

        assertEquals(FtpaDecisionOutcomeType.FTPA_REMADE32, homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getFtpaApplicationDecision(asylumCase));
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = homeOfficeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeRefNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("homeOfficeReferenceNumber", homeOfficeRefNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("homeOfficeRefNumber", homeOfficeRefNumber)
            .put("ariaListingReference", ariaListingReference)
            .build();
    }
}
