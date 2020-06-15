package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@RunWith(MockitoJUnitRunner.class)
public class RespondentForceCaseToSubmitHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String homeOfficeLartEmailAddress = "homeOfficeLART@example.com";

    private String hmctsReference = "hmctsReference";
    private String homeOfficeReference = "homeOfficeReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private RespondentForceCaseToSubmitHearingRequirementsPersonalisation respondentForceCaseToSubmitHearingRequirementsPersonalisation;

    @Before
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(hmctsReference));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));

        respondentForceCaseToSubmitHearingRequirementsPersonalisation = new RespondentForceCaseToSubmitHearingRequirementsPersonalisation(
            templateId,
            homeOfficeLartEmailAddress
        );
    }

    @Test
    public void should_return_the_given_template_id() {

        assertEquals(templateId, respondentForceCaseToSubmitHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_the_ho_lart_email_address_at_respondent_review() {
        assertEquals(Collections.singleton(homeOfficeLartEmailAddress), respondentForceCaseToSubmitHearingRequirementsPersonalisation.getRecipientsList(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS_RESPONDENT", respondentForceCaseToSubmitHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = respondentForceCaseToSubmitHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertEquals(hmctsReference, personalisation.get("appealReferenceNumber"));
        assertEquals(homeOfficeReference, personalisation.get("homeOfficeReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(() -> respondentForceCaseToSubmitHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }
}
