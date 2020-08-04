package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String homeOfficeRefNumber = "homeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String otherPartyGrantedTemplateId = "otherPartyGrantedTemplateId";
    private String otherPartyPartiallyGrantedTemplateId = "otherPartyPartiallyGrantedTemplateId";
    private String otherPartyNotAdmittedTemplateId = "otherPartyNotAdmittedTemplateId";
    private String otherPartyRefusedTemplateId = "otherPartyRefusedTemplateId";


    private FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
    private FtpaDecisionOutcomeType notAdmitted = FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
    private FtpaDecisionOutcomeType refused = FtpaDecisionOutcomeType.FTPA_REFUSED;

    private LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation;

    @Before
    public void setup() {
        legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation = new LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation(
            otherPartyGrantedTemplateId,
            otherPartyPartiallyGrantedTemplateId,
            otherPartyNotAdmittedTemplateId,
            otherPartyRefusedTemplateId,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(granted));
        assertEquals(otherPartyGrantedTemplateId, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(partiallyGranted));
        assertEquals(otherPartyPartiallyGrantedTemplateId, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(notAdmitted));
        assertEquals(otherPartyNotAdmittedTemplateId, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(refused));
        assertEquals(otherPartyRefusedTemplateId, legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_RESPONDENT", legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeRefNumber"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", ariaListingReference)
            .put("homeOfficeRefNumber", homeOfficeRefNumber)
            .build();
    }

}
