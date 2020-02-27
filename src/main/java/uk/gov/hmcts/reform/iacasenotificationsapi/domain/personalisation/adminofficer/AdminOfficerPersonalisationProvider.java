package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;


@Service
public class AdminOfficerPersonalisationProvider {

    public ImmutableMap<String, String> getDefaultPersonlisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    public ImmutableMap<String, String> getReviewedHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return getDefaultPersonlisation(asylumCase);
    }

    public ImmutableMap<String, String> getChangeToHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap.<String, String>builder()
            .putAll(getDefaultPersonlisation(asylumCase))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .build();
    }
}
