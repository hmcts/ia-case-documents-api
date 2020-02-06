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
public class AdminOfficerReviewHearingRequirementsPersonalisationTest {

    @Mock AsylumCase asylumCase;
    @Mock PersonalisationProvider personalisationProvider;
    private Long caseId = 12345L;
    private String templateId = "someTemplateId";
    private String reviewHearingRequirementsAdminOfficerEmailAddress = "adminofficer-review-hearing-requirements@example.com";
    private AdminOfficerReviewHearingRequirementsPersonalisation adminOfficerReviewHearingRequirementsPersonalisation;

    @Before
    public void setup() {

        adminOfficerReviewHearingRequirementsPersonalisation = new AdminOfficerReviewHearingRequirementsPersonalisation(
            templateId,
            reviewHearingRequirementsAdminOfficerEmailAddress,
            personalisationProvider
        );
    }

    @Test
    public void should_return_given_template_id() {
        assertEquals(templateId, adminOfficerReviewHearingRequirementsPersonalisation.getTemplateId());
    }

    @Test
    public void should_return_given_reference_id() {

        assertEquals(caseId + "_REVIEW_HEARING_REQUIREMENTS_ADMIN_OFFICER", adminOfficerReviewHearingRequirementsPersonalisation.getReferenceId(caseId));
    }

    @Test
    public void should_throw_exception_on_personalisation_when_case_is_null() {

        assertThatThrownBy(() -> adminOfficerReviewHearingRequirementsPersonalisation.getPersonalisation((AsylumCase) null))
            .isExactlyInstanceOf(NullPointerException.class)
            .hasMessage("asylumCase must not be null");
    }

    @Test
    public void should_return_personalisation_when_all_information_given() {

        Map<String, String> personalisation = adminOfficerReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);

    }

    @Test
    public void should_return_personalisation_when_all_mandatory_information_given() {

        Map<String, String> personalisation = adminOfficerReviewHearingRequirementsPersonalisation.getPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}