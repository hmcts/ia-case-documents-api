package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
public class CaseOfficerSubmittedHearingRequirementsPersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    AsylumCase asylumCase;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    private FeatureToggler featureToggler;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "someReferenceNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerSubmittedHearingRequirementsPersonalisation
        caseOfficerSubmittedHearingRequirementsPersonalisation;

    @BeforeEach
    public void setUp() {

        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        caseOfficerSubmittedHearingRequirementsPersonalisation =
            new CaseOfficerSubmittedHearingRequirementsPersonalisation(
                templateId,
                iaExUiFrontendUrl,
                personalisationProvider,
                emailAddressFinder,
                    featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, caseOfficerSubmittedHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_Off() {
        assertTrue(caseOfficerSubmittedHearingRequirementsPersonalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", true)).thenReturn(true);
        assertEquals(Collections.singleton(hearingCentreEmailAddress),
                caseOfficerSubmittedHearingRequirementsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_CASE_OFFICER_OF_SUBMITTED_HEARING_REQUIREMENTS",
            caseOfficerSubmittedHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        initializePrefixes(caseOfficerSubmittedHearingRequirementsPersonalisation);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisation());

        Map<String, String> personalisation =
            caseOfficerSubmittedHearingRequirementsPersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(personalisation).isEqualToComparingOnlyGivenFields(asylumCase);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> caseOfficerSubmittedHearingRequirementsPersonalisation
            .getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisation() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }
}
