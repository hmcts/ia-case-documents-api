package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
public class AdminOfficerChangeToHearingRequirementsPersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock
    AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String changeToHearingRequirementsAdminOfficerEmailAddress =
        "adminofficer-change-to-hearing-requirements@example.com";
    private AdminOfficerChangeToHearingRequirementsPersonalisation
        adminOfficerChangeToHearingRequirementsPersonalisation;

    @BeforeEach
    public void setup() {

        adminOfficerChangeToHearingRequirementsPersonalisation =
            new AdminOfficerChangeToHearingRequirementsPersonalisation(
                templateId,
                changeToHearingRequirementsAdminOfficerEmailAddress,
                adminOfficerPersonalisationProvider
            );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerChangeToHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {

        assertEquals(caseId + "_CHANGE_TO_HEARING_REQUIREMENTS_ADMIN_OFFICER",
            adminOfficerChangeToHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(
            () -> adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation =
            adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> personalisation =
            adminOfficerChangeToHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}
