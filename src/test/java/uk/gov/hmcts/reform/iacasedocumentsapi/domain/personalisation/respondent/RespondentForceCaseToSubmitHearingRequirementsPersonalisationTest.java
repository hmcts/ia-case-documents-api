package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.SubjectPrefixesInitializer.initializePrefixes;

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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RespondentForceCaseToSubmitHearingRequirementsPersonalisationTest {

    @Mock
    AsylumCase asylumCase;

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String detentionTemplateId = "detentionTemplateId";
    private final String homeOfficeLartEmailAddress = "homeOfficeLART@example.com";

    private final String hmctsReference = "hmctsReference";
    private final String homeOfficeReference = "homeOfficeReference";
    private final String appellantGivenNames = "someAppellantGivenNames";
    private final String appellantFamilyName = "someAppellantFamilyName";

    private RespondentForceCaseToSubmitHearingRequirementsPersonalisation
         respondentForceCaseToSubmitHearingRequirementsPersonalisation;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(hmctsReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        respondentForceCaseToSubmitHearingRequirementsPersonalisation =
            new RespondentForceCaseToSubmitHearingRequirementsPersonalisation(
                templateId,
                detentionTemplateId,
                homeOfficeLartEmailAddress
            );
    }

    @Test
    public void should_return_the_given_template_id_for_non_detention() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        assertEquals(templateId, respondentForceCaseToSubmitHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    public void should_return_the_given_template_id_for_missing_detention() {
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.empty());

        assertEquals(templateId, respondentForceCaseToSubmitHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_given_template_id_for_detention() {
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertEquals(detentionTemplateId, respondentForceCaseToSubmitHearingRequirementsPersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_the_ho_lart_email_address_at_respondent_review() {
        assertEquals(Collections.singleton(homeOfficeLartEmailAddress),
            respondentForceCaseToSubmitHearingRequirementsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS_RESPONDENT",
            respondentForceCaseToSubmitHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "YES", "NO" })
    public void should_return_personalisation_when_all_information_given(YesOrNo isAda) {

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        initializePrefixes(respondentForceCaseToSubmitHearingRequirementsPersonalisation);
        Map<String, String> personalisation =
            respondentForceCaseToSubmitHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals(hmctsReference, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReference, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(isAda.equals(YesOrNo.YES)
            ? "Accelerated detained appeal"
            : "Immigration and Asylum appeal", personalisation.get("subjectPrefix"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
            () -> respondentForceCaseToSubmitHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
