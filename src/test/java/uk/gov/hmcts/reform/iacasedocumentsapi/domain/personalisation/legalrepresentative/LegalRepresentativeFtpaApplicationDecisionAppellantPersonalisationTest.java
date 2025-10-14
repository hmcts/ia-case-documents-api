package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_REMADE_RULE_32;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.time.ZonedDateTime;
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
public class LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    DueDateService dueDateService;
    @Mock
    PersonalisationProvider personalisationProvider;

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
    private String applicantReheardTemplateId = "otherPartyReheardTemplateId";
    private String allowedTemplateId = "allowedTemplateId";
    private String dismissedTemplateId = "dismissedTemplateId";

    private int calendarDaysToWaitInCountry = 14;
    private int calendarDaysToWaitOutOfCountry = 28;
    private int workingDaysaysToWaitAda = 5;


    private FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
    private FtpaDecisionOutcomeType notAdmitted = FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
    private FtpaDecisionOutcomeType refused = FtpaDecisionOutcomeType.FTPA_REFUSED;
    private FtpaDecisionOutcomeType reheard = FtpaDecisionOutcomeType.FTPA_REHEARD35;
    private FtpaDecisionOutcomeType allowed = FtpaDecisionOutcomeType.FTPA_ALLOWED;
    private FtpaDecisionOutcomeType remade = FtpaDecisionOutcomeType.FTPA_REMADE32;
    private FtpaDecisionOutcomeType dismissed = FtpaDecisionOutcomeType.FTPA_DISMISSED;

    private LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation
        legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation;

    @BeforeEach
    public void setup() {
        legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation =
            new LegalRepresentativeFtpaApplicationDecisionAppellantPersonalisation(
                applicantGrantedTemplateId,
                applicantPartiallyGrantedTemplateId,
                applicantNotAdmittedTemplateId,
                applicantRefusedTemplateId,
                applicantReheardTemplateId,
                allowedTemplateId,
                dismissedTemplateId,
                calendarDaysToWaitInCountry,
                calendarDaysToWaitOutOfCountry,
                workingDaysaysToWaitAda,
                dueDateService,
                personalisationProvider
            );
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertThatThrownBy(
            () -> legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaAppellantDecisionOutcomeType is not present");
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(applicantGrantedTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        assertEquals(applicantPartiallyGrantedTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        assertEquals(applicantNotAdmittedTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(reheard));
        assertEquals(applicantReheardTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(allowed));
        assertEquals(allowedTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(remade));
        when(asylumCase.read(FTPA_APPELLANT_DECISION_REMADE_RULE_32, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(dismissed));
        assertEquals(dismissedTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        assertEquals(applicantRefusedTemplateId,
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT",
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_of_all_information_given_decision_partially_granted_ada() {
        initializePrefixes(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation);
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforLegalRep());
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(dueDateService.calculateWorkingDaysDueDate(any(ZonedDateTime.class), any(Integer.class))).thenReturn(ZonedDateTime.now());
        Map<String, String> personalisation =
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals("Accelerated detained appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));

        verify(dueDateService, times(1)).calculateWorkingDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    @Test
    public void should_return_personalisation_of_all_information_given_decision_refused_in_country() {
        initializePrefixes(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforLegalRep());
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(refused));
        when(dueDateService.calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class))).thenReturn(ZonedDateTime.now());
        Map<String, String> personalisation =
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals("Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));

        verify(dueDateService, times(1)).calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    @Test
    public void should_return_personalisation_of_all_information_given_decision_not_admitted_out_of_country() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        initializePrefixes(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation);
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforLegalRep());
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(notAdmitted));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(dueDateService.calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class))).thenReturn(ZonedDateTime.now());
        Map<String, String> personalisation =
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals("Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));

        verify(dueDateService, times(1)).calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_of_all_information_given_others(YesOrNo isAda) {
        initializePrefixes(legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase)).thenReturn(getPersonalisationforLegalRep());
        when(asylumCase.read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        Map<String, String> personalisation =
            legalRepresentativeFtpaApplicationDecisionAppellantPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(legalRepReferenceNumber, personalisation.get("legalRepReferenceNumber"));

        verify(dueDateService, times(0)).calculateWorkingDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
        verify(dueDateService, times(0)).calculateCalendarDaysDueDate(any(ZonedDateTime.class), any(Integer.class));
    }

    private Map<String, String> getPersonalisationforLegalRep() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("legalRepReferenceNumber", legalRepReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", ariaListingReference)
            .build();
    }

}
