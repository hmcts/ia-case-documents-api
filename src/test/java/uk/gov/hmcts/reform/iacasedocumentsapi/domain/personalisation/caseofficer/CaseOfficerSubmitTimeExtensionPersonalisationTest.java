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
public class CaseOfficerSubmitTimeExtensionPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    private FeatureToggler featureToggler;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenName = "Pablo";
    private String appellantFamilyName = "Jimenez";
    private String iaExUiFrontendUrl = "http://localhost";


    private CaseOfficerSubmitTimeExtensionPersonalisation caseOfficerSubmitTimeExtensionPersonalisation;

    @BeforeEach
    public void setUp() {
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        when(asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class))
            .thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenName));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        caseOfficerSubmitTimeExtensionPersonalisation =
            new CaseOfficerSubmitTimeExtensionPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                emailAddressFinder,
                    featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerSubmitTimeExtensionPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_SUBMIT_TIME_EXTENSION_CASE_OFFICER",
            caseOfficerSubmitTimeExtensionPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_Off() {
        assertTrue(caseOfficerSubmitTimeExtensionPersonalisation.getRecipientsList(asylumCase)
                .isEmpty());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-application-notifications-feature", true)).thenReturn(true);
        assertTrue(caseOfficerSubmitTimeExtensionPersonalisation.getRecipientsList(asylumCase)
                .contains(hearingCentreEmailAddress), hearingCentreEmailAddress);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerSubmitTimeExtensionPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase cannot be null");
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerSubmitTimeExtensionPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", appealReferenceNumber)
                .put("Appellant Given names", appellantGivenName)
                .put("Appellant Family name", appellantFamilyName)
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .build();

        Map<String, String> actualPersonalisation =
            caseOfficerSubmitTimeExtensionPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_mandatory_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerSubmitTimeExtensionPersonalisation);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));

        final Map<String, String> expectedPersonalisation =
            ImmutableMap
                .<String, String>builder()
                .put("subjectPrefix", isAda.equals(YesOrNo.YES)
                    ? "Accelerated detained appeal"
                    : "Immigration and Asylum appeal")
                .put("Appeal Ref Number", "")
                .put("Appellant Given names", "")
                .put("Appellant Family name", "")
                .put("Hyperlink to service", iaExUiFrontendUrl)
                .build();

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> actualPersonalisation =
            caseOfficerSubmitTimeExtensionPersonalisation.getPersonalisation(asylumCase);

        assertThat(actualPersonalisation).isEqualTo(expectedPersonalisation);
    }
}
