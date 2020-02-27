package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;



@RunWith(MockitoJUnitRunner.class)
public class AdminOfficerPersonalisationProviderTest {

    @Mock AsylumCase asylumCase;
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    @Before
    public void setUp() {

        adminOfficerPersonalisationProvider = new AdminOfficerPersonalisationProvider();
    }

    @Test
    public void should_return_default_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getDefaultPersonlisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_reviewed_hearing_requirements_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_change_to_hearing_requirements_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}
