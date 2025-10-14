package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_REHEARD_APPEAL_ENABLED;
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
public class CaseOfficerFtpaDecisionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    private FeatureToggler featureToggler;

    private String caseOfficerEmailAddress = "caseOfficer@example.com";
    private Long caseId = 12345L;
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String applicantReheardTemplateId = "applicantReheardTemplateId";
    private String applicantReheardEnabledTemplateId = "applicantReheardEnabledTemplateId";

    private CaseOfficerFtpaDecisionPersonalisation caseOfficerFtpaDecisionPersonalisation;

    @BeforeEach
    public void setup() {
        caseOfficerFtpaDecisionPersonalisation = new CaseOfficerFtpaDecisionPersonalisation(
            applicantReheardTemplateId,
            applicantReheardEnabledTemplateId,
            personalisationProvider,
            emailAddressFinder,
                featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertEquals(applicantReheardTemplateId, caseOfficerFtpaDecisionPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertEquals(applicantReheardEnabledTemplateId, caseOfficerFtpaDecisionPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_CASE_OFFICER",
            caseOfficerFtpaDecisionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_lookup_map_when_feature_flag_is_Off() {
        assertTrue(
                caseOfficerFtpaDecisionPersonalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    public void should_return_given_email_address_from_lookup_map_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", false)).thenReturn(true);
        when(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase)).thenReturn(caseOfficerEmailAddress);
        assertTrue(
                caseOfficerFtpaDecisionPersonalisation.getRecipientsList(asylumCase).contains(caseOfficerEmailAddress));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_of_all_information_given(YesOrNo isAda) {
        initializePrefixes(caseOfficerFtpaDecisionPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation = caseOfficerFtpaDecisionPersonalisation.getPersonalisation(asylumCase);

        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .put("ariaListingReference", ariaListingReference)
            .build();
    }

}
