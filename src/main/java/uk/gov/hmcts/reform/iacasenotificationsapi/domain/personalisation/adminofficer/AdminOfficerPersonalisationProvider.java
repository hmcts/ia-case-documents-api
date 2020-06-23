package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

@Service
public class AdminOfficerPersonalisationProvider {

    private final String iaExUiFrontendUrl;

    public AdminOfficerPersonalisationProvider(
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl
    ) {
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
    }

    public ImmutableMap<String, String> getDefaultPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    public ImmutableMap<String, String> getReviewedHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return getDefaultPersonalisation(asylumCase);
    }

    public ImmutableMap<String, String> getChangeToHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap.<String, String>builder()
            .putAll(getDefaultPersonalisation(asylumCase))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .build();
    }
}
