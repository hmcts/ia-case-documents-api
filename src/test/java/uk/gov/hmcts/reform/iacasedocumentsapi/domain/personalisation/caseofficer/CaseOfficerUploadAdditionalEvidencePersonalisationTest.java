package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
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
public class CaseOfficerUploadAdditionalEvidencePersonalisationTest {

    @Mock
    Callback<AsylumCase> callback;
    @Mock
    CaseDetails<AsylumCase> caseDetails;
    @Mock
    AsylumCase asylumCase;
    @Mock
    EmailAddressFinder emailAddressFinder;
    @Mock
    PersonalisationProvider personalisationProvider;
    @Mock
    private FeatureToggler featureToggler;

    private Long caseId = 12345L;
    private String beforeListingTemplateId = "beforeListingTemplateId";
    private String afterListingTemplateId = "afterListingTemplateId";
    private String iaExUiFrontendUrl = "http://localhost";
    private HearingCentre hearingCentre = HearingCentre.TAYLOR_HOUSE;
    private String hearingCentreEmailAddress = "hearingCentre@example.com";
    private String appealReferenceNumber = "hmctsReference";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private CaseOfficerUploadAdditionalEvidencePersonalisation caseOfficerUploadAdditionalEvidencePersonalisation;

    @BeforeEach
    public void setUp() {
        when(emailAddressFinder.getHearingCentreEmailAddress(asylumCase)).thenReturn(hearingCentreEmailAddress);

        caseOfficerUploadAdditionalEvidencePersonalisation = new CaseOfficerUploadAdditionalEvidencePersonalisation(
            beforeListingTemplateId,
            afterListingTemplateId,
            iaExUiFrontendUrl,
            personalisationProvider,
            emailAddressFinder,
                featureToggler);
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(beforeListingTemplateId,
            caseOfficerUploadAdditionalEvidencePersonalisation.getTemplateId(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertEquals(afterListingTemplateId,
            caseOfficerUploadAdditionalEvidencePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_Off() {
        assertTrue(caseOfficerUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase).isEmpty());
    }

    @Test
    public void should_return_given_email_address_from_asylum_case_when_feature_flag_is_On() {
        when(featureToggler.getValue("tcw-notifications-feature", false)).thenReturn(true);
        assertEquals(Collections.singleton(hearingCentreEmailAddress),
                caseOfficerUploadAdditionalEvidencePersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_CASE_OFFICER",
            caseOfficerUploadAdditionalEvidencePersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {
        initializePrefixes(caseOfficerUploadAdditionalEvidencePersonalisation);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(personalisationProvider.getPersonalisation(callback)).thenReturn(getPersonalisationForCaseOfficer());

        Map<String, String> personalisation =
            caseOfficerUploadAdditionalEvidencePersonalisation.getPersonalisation(callback);

        assertThat(personalisation).isNotEmpty();
        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> caseOfficerUploadAdditionalEvidencePersonalisation.getPersonalisation((Callback<AsylumCase>) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    private Map<String, String> getPersonalisationForCaseOfficer() {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", appealReferenceNumber)
            .put("ariaListingReference", ariaListingReference)
            .put("appellantGivenNames", appellantGivenNames)
            .put("appellantFamilyName", appellantFamilyName)
            .build();
    }

    @Test
    public void should_return_false_if_appeal_not_yet_listed() {
        assertFalse(caseOfficerUploadAdditionalEvidencePersonalisation.isAppealListed(asylumCase));

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        assertTrue(caseOfficerUploadAdditionalEvidencePersonalisation.isAppealListed(asylumCase));
    }
}
