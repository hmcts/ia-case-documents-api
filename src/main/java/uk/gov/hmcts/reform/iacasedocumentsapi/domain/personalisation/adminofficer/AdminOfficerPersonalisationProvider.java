package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import java.util.List;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;

//test

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

    public ImmutableMap<String, String> getAdminPersonalisation(AsylumCase asylumCase) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.<String, String>builder()
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("linkedCase", asylumCase.read(AsylumCaseDefinition.CASE_LINKS, List.class).map(value -> value.isEmpty() ? "No" : "Yes").orElse("No"));

        asylumCase.read(AsylumCaseDefinition.HEARING_CENTRE, HearingCentre.class)
            .ifPresent(hearingCentre -> builder.put("hearingCentre", String.valueOf(hearingCentre).toUpperCase()));
        asylumCase.read(AsylumCaseDefinition.IS_DECISION_ALLOWED, AppealDecision.class)
            .ifPresent(appealOutcomeDecision -> builder.put("applicationDecision", String.valueOf(appealOutcomeDecision).toUpperCase()));

        return builder.build();
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
