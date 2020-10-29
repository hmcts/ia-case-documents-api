package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

@ExtendWith(MockitoExtension.class)
public class AdminOfficerPersonalisationProviderTest {

    @Mock
    AsylumCase asylumCase;

    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    @BeforeEach
    public void setUp() {

        adminOfficerPersonalisationProvider = new AdminOfficerPersonalisationProvider(
            iaExUiFrontendUrl
        );
    }

    @Test
    public void should_return_default_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getDefaultPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_reviewed_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            adminOfficerPersonalisationProvider.getReviewedHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }

    @Test
    public void should_return_change_to_hearing_requirements_personalisation() {

        Map<String, String> personalisation =
            adminOfficerPersonalisationProvider.getChangeToHearingRequirementsPersonalisation(asylumCase);

        assertThat(asylumCase).isEqualToComparingOnlyGivenFields(personalisation);
    }
}
