package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_DECISION_ALLOWED;

import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer.AdminOfficerPersonalisationProvider;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AdminOfficerPersonalisationProviderTest {

    @Mock
    AsylumCase asylumCase;

    private String iaExUiFrontendUrl = "http://somefrontendurl";
    private String appealReferenceNumber = "someReferenceNumber";
    private String ariaListingReference = "someAriaListingReference";
    private String appellantGivenNames = "someAppellantGivenNames";
    private String appellantFamilyName = "someAppellantFamilyName";

    private String hearingCentre = "GLASGOW";

    private String applicationDecision = "ALLOWED";
    private AdminOfficerPersonalisationProvider adminOfficerPersonalisationProvider;

    @BeforeEach
    public void setUp() {
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        when(asylumCase.read(ARIA_LISTING_REFERENCE, String.class)).thenReturn(Optional.of(ariaListingReference));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.GLASGOW));
        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(AppealDecision.ALLOWED));

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
    public void should_return_admin_personalisation() {

        Map<String, String> personalisation = adminOfficerPersonalisationProvider.getAdminPersonalisation(asylumCase);

        assertEquals(appealReferenceNumber, personalisation.get("appealReferenceNumber"));
        assertEquals(ariaListingReference, personalisation.get("ariaListingReference"));
        assertEquals(appellantGivenNames, personalisation.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, personalisation.get("appellantFamilyName"));
        assertEquals(hearingCentre, personalisation.get("hearingCentre"));
        assertEquals(applicationDecision, personalisation.get("applicationDecision"));
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
