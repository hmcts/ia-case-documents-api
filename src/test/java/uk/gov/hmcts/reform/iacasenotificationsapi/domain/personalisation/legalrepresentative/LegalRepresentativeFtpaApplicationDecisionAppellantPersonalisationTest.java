package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;

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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;
    @Mock GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    private Long caseId = 12345L;
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String legalRepReferenceNumber = "someLegalRepRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicantGrantedTemplateId = "applicantGrantedTemplateId";
    private String applicantPartiallyGrantedTemplateId = "applicantPartiallyGrantedTemplateId";
    private String applicantNotAdmittedTemplateId = "applicantNotAdmittedTemplateId";
    private String applicantRefusedTemplateId = "applicantRefusedTemplateId";


    private FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
    private FtpaDecisionOutcomeType notAdmitted = FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
    private FtpaDecisionOutcomeType refused = FtpaDecisionOutcomeType.FTPA_REFUSED;

    private LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation;

    @Before
    public void setup() {
        legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation = new LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation(
            applicantGrantedTemplateId,
            applicantPartiallyGrantedTemplateId,
            applicantNotAdmittedTemplateId,
            applicantRefusedTemplateId,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(granted));
        assertEquals(applicantGrantedTemplateId, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(partiallyGranted));
        assertEquals(applicantPartiallyGrantedTemplateId, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(notAdmitted));
        assertEquals(applicantNotAdmittedTemplateId, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(refused));
        assertEquals(applicantRefusedTemplateId, legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT", legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", ariaListingReference)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .build();
    }

}
