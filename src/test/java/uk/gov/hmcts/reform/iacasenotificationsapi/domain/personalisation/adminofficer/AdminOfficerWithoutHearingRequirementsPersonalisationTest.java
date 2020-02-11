package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;


@RunWith(MockitoJUnitRunner.class)
public class AdminOfficerWithoutHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;
    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String reviewHearingRequirementsAdminOfficerEmailAddress = "adminofficer-without-hearing-requirements@example.com";
    private AdminOfficerWithoutHearingRequirementsPersonalisation adminOfficerWithoutHearingRequirementsPersonalisation;

    @Before
    public void setup() {

        adminOfficerWithoutHearingRequirementsPersonalisation = new AdminOfficerWithoutHearingRequirementsPersonalisation(
            templateId,
            reviewHearingRequirementsAdminOfficerEmailAddress,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerWithoutHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {

        assertEquals(caseId + "_WITHOUT_HEARING_REQUIREMENTS_ADMIN_OFFICER", adminOfficerWithoutHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerWithoutHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = adminOfficerWithoutHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> personalisation = adminOfficerWithoutHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}