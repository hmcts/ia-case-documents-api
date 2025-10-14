package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CaseOfficerRespondentEvidenceSubmittedPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    private FeatureToggler featureToggler;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerRespondentEvidenceSubmittedPersonalisation caseOfficerRespondentEvidenceSubmittedPersonalisation;

    @BeforeEach
    public void setUp() {

        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);
        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerRespondentEvidenceSubmittedPersonalisation =
            new CaseOfficerRespondentEvidenceSubmittedPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                emailAddressFinder,
                    featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerRespondentEvidenceSubmittedPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_RESPONDENT_EVIDENCE_SUBMITTED_CASE_OFFICER",
            caseOfficerRespondentEvidenceSubmittedPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_Off() {
        assertTrue(caseOfficerRespondentEvidenceSubmittedPersonalisation.getRecipientsList(asylumCase)
                .isEmpty());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        assertTrue(caseOfficerRespondentEvidenceSubmittedPersonalisation.getRecipientsList(asylumCase)
                .contains(hearingCentreEmailAddress), hearingCentreEmailAddress);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> caseOfficerRespondentEvidenceSubmittedPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerRespondentEvidenceSubmittedPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .put("appealReferenceNumber", appealReferenceNumber)
                .put("appellantGivenNames", appellantGivenNames)
                .put("appellantFamilyName", appellantFamilyName)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();

        Map<String, String> actualPersonalisation =
            caseOfficerRespondentEvidenceSubmittedPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerRespondentEvidenceSubmittedPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .put("appealReferenceNumber", "")
                .put("appellantGivenNames", "")
                .put("appellantFamilyName", "")
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation =
            caseOfficerRespondentEvidenceSubmittedPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }
}
