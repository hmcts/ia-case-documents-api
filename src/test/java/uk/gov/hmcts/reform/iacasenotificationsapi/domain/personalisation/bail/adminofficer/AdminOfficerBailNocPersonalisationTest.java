package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.bail.adminofficer.email.AdminOfficerBailNocChangedLrPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminOfficerBailNocPersonalisationTest {

    @Mock
    BailCase bailCase;

    private final Long caseId = 12345L;
    private final String templateId = "someTemplateId";
    private final String bailReferenceNumber = "someReferenceNumber";
    private final String legalRepReference = "someLegalRepReference";
    private final String homeOfficeReferenceNumber = "someHomeOfficeReferenceNumber";
    private final String applicantGivenNames = "someApplicantGivenNames";
    private final String applicantFamilyName = "someApplicantFamilyName";

    @Mock
    private EmailAddressFinder emailAddressFinder;
    private AdminOfficerBailNocChangedLrPersonalisation adminOfficerBailNocPersonalisation;

    @BeforeEach
    public void setup() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(bailReferenceNumber));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(applicantGivenNames));
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(applicantFamilyName));
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReference));
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));

        adminOfficerBailNocPersonalisation = new AdminOfficerBailNocChangedLrPersonalisation(
                templateId,
                emailAddressFinder
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerBailNocPersonalisation.getTemplateId(bailCase));
    }

    @Test
    public void should_return_given_reference_id() {
        assertEquals(caseId + "_BAIL_NOC_CHANGED_LR_ADMIN",
            adminOfficerBailNocPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_return_given_email_address_for_hearing_centre() {
        String hearingCentreEmail = "someHearingCentre@example.com";
        when(emailAddressFinder.getBailHearingCentreEmailAddress(bailCase)).thenReturn(hearingCentreEmail);
        assertTrue(adminOfficerBailNocPersonalisation.getRecipientsList(bailCase)
                .contains(hearingCentreEmail));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {
        assertThatThrownBy(
                () -> adminOfficerBailNocPersonalisation.getPersonalisation((BailCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("bailCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            adminOfficerBailNocPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        when(bailCase.read(BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_GIVEN_NAMES, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.APPLICANT_FAMILY_NAME, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.empty());
        when(bailCase.read(BailCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        Map<String, String> personalisation =
            adminOfficerBailNocPersonalisation.getPersonalisation(bailCase);

        assertThat(personalisation).isEqualToComparingOnlyGivenFields(bailCase);
    }

}
