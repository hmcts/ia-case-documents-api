package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
public class AdminOfficerFtpaDecisionRespondentPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    PersonalisationProvider personalisationProvider;

    private Long caseId = 12345L;
    private String adminOfficeEmailAddress = "some-email@example.com";
    private String upperTribunalPermissionApplicationsEmailAddress = "upperTribunalPermissionApplicationsEmailAddress";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "ariaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String grantedTemplateId = "grantedTemplateId";
    private String grantedWithoutListingTemplateId = "grantedithoutListingTemplateId";
    private String partiallyGrantedTemplateId = "partiallyGrantedTemplateId";
    private String partiallyGrantedWithoutListingTemplateId = "partiallyGrantedWithoutListingTemplateId";

    private FtpaDecisionOutcomeType granted = FtpaDecisionOutcomeType.FTPA_GRANTED;
    private FtpaDecisionOutcomeType partiallyGranted = FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;

    private AdminOfficerFtpaDecisionRespondentPersonalisation adminOfficerFtpaDecisionRespondentPersonalisation;

    @BeforeEach
    public void setup() {
        adminOfficerFtpaDecisionRespondentPersonalisation = new AdminOfficerFtpaDecisionRespondentPersonalisation(
            grantedTemplateId,
            grantedWithoutListingTemplateId,
            partiallyGrantedTemplateId,
            partiallyGrantedWithoutListingTemplateId,
            adminOfficeEmailAddress,
            upperTribunalPermissionApplicationsEmailAddress,
            personalisationProvider
        );

        initializePrefixes(adminOfficerFtpaDecisionRespondentPersonalisation);
    }

    @ParameterizedTest
    @EnumSource(value = FtpaDecisionOutcomeType.class, names = {
        "FTPA_GRANTED",
        "FTPA_PARTIALLY_GRANTED",
        "FTPA_REFUSED",
        "FTPA_NOT_ADMITTED",
        "FTPA_REHEARD35",
        "FTPA_REHEARD32",
        "FTPA_REMADE32",
        "FTPA_ALLOWED",
        "FTPA_DISMISSED"
    })
    void should_return_correct_recipient_email_address(FtpaDecisionOutcomeType decision) {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(decision));

        String expectedEmailAddress = Set.of(FTPA_GRANTED, FTPA_PARTIALLY_GRANTED).contains(decision)
            ? upperTribunalPermissionApplicationsEmailAddress
            : adminOfficeEmailAddress;

        assertTrue(adminOfficerFtpaDecisionRespondentPersonalisation.getRecipientsList(asylumCase)
            .contains(expectedEmailAddress));
    }

    @Test
    public void should_return_given_template_id_when_outcome_is_empty() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminOfficerFtpaDecisionRespondentPersonalisation.getTemplateId(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("ftpaRespondentDecisionOutcomeType is not present");
    }

    @Test
    public void should_return_given_template_id() {
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(grantedTemplateId, adminOfficerFtpaDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));
        assertEquals(partiallyGrantedTemplateId,
            adminOfficerFtpaDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_template_id_for_internal_case() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(granted));
        assertEquals(grantedWithoutListingTemplateId, adminOfficerFtpaDecisionRespondentPersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class))
            .thenReturn(Optional.of(partiallyGranted));

        assertEquals(partiallyGrantedWithoutListingTemplateId,
            adminOfficerFtpaDecisionRespondentPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FTPA_APPLICATION_DECISION_ADMIN_OFFICER_RESPONDENT",
            adminOfficerFtpaDecisionRespondentPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_of_all_information_given(YesOrNo isAda) {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .thenReturn(getPersonalisationMapWithGivenValues());
        Map<String, String> personalisation =
            adminOfficerFtpaDecisionRespondentPersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    private Map<String, String> getPersonalisationMapWithGivenValues() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }

}

