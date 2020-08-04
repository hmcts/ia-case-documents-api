package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

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
public class HomeOfficeFtpaApplicationDecisionRespondentPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String homeOfficeEmailAddress = "homeoffice-allowed@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String ariaListingReference = "ariaListingReference";
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

    private HomeOfficeFtpaApplicationDecisionRespondentPersonalisation homeOfficeFtpaApplicationDecisionRespondentPersonalisation;

    @Before
    public void setup() {
        homeOfficeFtpaApplicationDecisionRespondentPersonalisation = new HomeOfficeFtpaApplicationDecisionRespondentPersonalisation(
            applicantGrantedTemplateId,
            applicantPartiallyGrantedTemplateId,
            applicantNotAdmittedTemplateId,
            applicantRefusedTemplateId,
            personalisationProvider,
            homeOfficeEmailAddress
        );
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(granted));
        assertEquals(applicantGrantedTemplateId, homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(partiallyGranted));
        assertEquals(applicantPartiallyGrantedTemplateId, homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(notAdmitted));
        assertEquals(applicantNotAdmittedTemplateId, homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)).thenReturn(Optional.of(refused));
        assertEquals(applicantRefusedTemplateId, homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT", homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_of_all_information_given() {
        when(personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = homeOfficeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

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
