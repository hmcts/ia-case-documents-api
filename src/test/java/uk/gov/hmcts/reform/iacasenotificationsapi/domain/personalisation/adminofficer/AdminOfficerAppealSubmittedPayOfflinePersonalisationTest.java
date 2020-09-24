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


@RunWith(MockitoJUnitRunner.class)
public class AdminOfficerAppealSubmittedPayOfflinePersonalisationTest {

    @Mock
    AsylumCase asylumCase;
    @Mock AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String changeToHearingRequirementsAdminOfficerEmailAddress = "fees-ao@example.com";
    private AdminOfficerAppealSubmittedPayOfflinePersonalisation adminOfficerAppealSubmittedPayOfflinePersonalisation;

    @Before
    public void setup() {

        adminOfficerAppealSubmittedPayOfflinePersonalisation = new AdminOfficerAppealSubmittedPayOfflinePersonalisation(
                templateId,
                changeToHearingRequirementsAdminOfficerEmailAddress,
                adminOfficerPersonalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerAppealSubmittedPayOfflinePersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {

        assertEquals(caseId + "_APPEAL_SUBMITTED_PAY_OFFLINE_ADMIN_OFFICER", adminOfficerAppealSubmittedPayOfflinePersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation((AsylumCase) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

    }

    @org.junit.Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> personalisation = adminOfficerAppealSubmittedPayOfflinePersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);


    }
}
