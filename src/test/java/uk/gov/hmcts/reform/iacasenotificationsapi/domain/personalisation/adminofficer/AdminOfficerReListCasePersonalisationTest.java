package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@RunWith(MockitoJUnitRunner.class)
public class AdminOfficerReListCasePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;
    private String templateId = "someTemplateId";
    private AdminOfficerReListCasePersonalisation adminOfficerReListCasePersonalisation;


    @Before
    public void setup() {

        String changeToHearingRequirementsAdminOfficerEmailAddress =
            "adminofficer-change-to-hearing-requirements@example.com";
        adminOfficerReListCasePersonalisation = new AdminOfficerReListCasePersonalisation(
            templateId,
            changeToHearingRequirementsAdminOfficerEmailAddress,
            adminOfficerPersonalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerReListCasePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {

        Long caseId = 12345L;
        assertEquals(caseId + "_RE_LIST_CASE_ADMIN_OFFICER",
            adminOfficerReListCasePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerReListCasePersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = adminOfficerReListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> personalisation = adminOfficerReListCasePersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}