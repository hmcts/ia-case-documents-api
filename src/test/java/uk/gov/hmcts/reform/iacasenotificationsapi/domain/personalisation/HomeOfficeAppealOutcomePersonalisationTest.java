package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@RunWith(MockitoJUnitRunner.class)
public class HomeOfficeAppealOutcomePersonalisationTest {

    @Mock AsylumCase asylumCase;

    private Long caseId = 12345L;
    private String templateAllowedId = "someTemplateAllowedId";
    private String templateDismissedId = "someTemplateDismissedId";

    private String allowedEmailAddress = "homeoffice-allowed@example.com";
    private String dismissedEmailAddress = "homeoffice-dismissed@example.com";

    private String appealReferenceNumber = "someReferenceNumber";
    private String homeOfficeRefNumber = "someHomeOfficeRefNumber";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private HomeOfficeAppealOutcomePersonalisation homeOfficeAppealOutcomePersonalisation;

    @Before
    public void setup() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeRefNumber));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));

        homeOfficeAppealOutcomePersonalisation = new HomeOfficeAppealOutcomePersonalisation(
            allowedEmailAddress,
            dismissedEmailAddress,
            templateAllowedId,
            templateDismissedId
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateAllowedId, homeOfficeAppealOutcomePersonalisation.getTemplateId(asylumCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_APPEAL_OUTCOME_HOME_OFFICE", homeOfficeAppealOutcomePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_for_allowed_appeal_outcome() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));

        assertEquals(allowedEmailAddress, homeOfficeAppealOutcomePersonalisation.getEmailAddress(asylumCase));
    }

    @Test
    public void should_return_given_email_address_for_dismissed_appeal_outcome() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.DISMISSED));

        assertEquals(dismissedEmailAddress, homeOfficeAppealOutcomePersonalisation.getEmailAddress(asylumCase));
    }

    @Test
    public void should_return_dismissed_appeal_outcome_decision() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.DISMISSED));

        assertEquals("dismissed", homeOfficeAppealOutcomePersonalisation.getAppealDecision(asylumCase));
    }

    @Test
    public void should_return_allowed_appeal_outcome_decision() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.of(AppealDecision.ALLOWED));

        assertEquals("allowed", homeOfficeAppealOutcomePersonalisation.getAppealDecision(asylumCase));
    }

    @Test
    public void should_throw_exception_when_appeal_outcome_decision_is_missing() {
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> homeOfficeAppealOutcomePersonalisation.getAppealDecision(asylumCase))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("appealOutcomeDecision is not present");
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> homeOfficeAppealOutcomePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = homeOfficeAppealOutcomePersonalisation.getPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(homeOfficeRefNumber, personalisation.get("homeOfficeReferenceNumber"));
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation = homeOfficeAppealOutcomePersonalisation.getPersonalisation(asylumCase);

        assertEquals("", personalisation.get("appealReferenceNumber"));
        assertEquals("", personalisation.get("homeOfficeReferenceNumber"));
        assertEquals("", personalisation.get("appellantGivenNames"));
        assertEquals("", personalisation.get("appellantFamilyName"));
    }
}