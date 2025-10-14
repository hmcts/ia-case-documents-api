package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_REMADE_RULE_32;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;

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

    private LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation
        legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation;

    @BeforeEach
    public void setup() {
        legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation =
            new LegalRepresentativeFtpaApplicationDecisionRespondentPersonalisation(
                otherPartyGrantedTemplateId,
                otherPartyPartiallyGrantedTemplateId,
                otherPartyNotAdmittedTemplateId,
                otherPartyRefusedTemplateId,
                otherPartyReheardTemplateId,
                allowedTemplateId,
                dismissedTemplateId,
                personalisationProvider
            );
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertThatThrownBy(
            () -> legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionOutcomeType is not present");
    }

    @Test
    public void should_return_given_template_id() {

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(otherPartyGrantedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        assertEquals(otherPartyPartiallyGrantedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        assertEquals(otherPartyNotAdmittedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(reheard));
        assertEquals(otherPartyReheardTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(allowed));
        assertEquals(allowedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(dismissed));
        assertEquals(dismissedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        assertEquals(otherPartyRefusedTemplateId,
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_RESPONDENT",
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_of_all_information_given(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation);
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation =
            legalRepresentativeFtpaApplicationDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
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
