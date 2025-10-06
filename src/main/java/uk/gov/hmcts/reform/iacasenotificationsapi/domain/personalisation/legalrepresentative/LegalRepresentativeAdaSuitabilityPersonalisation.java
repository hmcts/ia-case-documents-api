package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.SUITABILITY_REVIEW_DECISION;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeAdaSuitabilityPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String adaSuitabilityUnsuitableLegalRepresentativeTemplateId;
    private final String adaSuitabilitySuitableLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;


    public LegalRepresentativeAdaSuitabilityPersonalisation(
            @NotNull(message = "adaSuitabilityUnsuitableLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.adaSuitabilityReview.legalRep.unsuitable.email}") String adaSuitabilityUnsuitableLegalRepresentativeTemplateId,
            @Value("${govnotify.template.adaSuitabilityReview.legalRep.suitable.email}") String adaSuitabilitySuitableLegalRepresentativeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.adaSuitabilityUnsuitableLegalRepresentativeTemplateId = adaSuitabilityUnsuitableLegalRepresentativeTemplateId;
        this.adaSuitabilitySuitableLegalRepresentativeTemplateId = adaSuitabilitySuitableLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        AdaSuitabilityReviewDecision decision =
                asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                        .orElseThrow(() -> new IllegalStateException("suitabilityReviewDecision is not present"));

        if (decision.toString().equals(AdaSuitabilityReviewDecision.UNSUITABLE.toString())) {
            return adaSuitabilityUnsuitableLegalRepresentativeTemplateId;
        } else {
            return adaSuitabilitySuitableLegalRepresentativeTemplateId;
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_ADA_SUITABILITY_DETERMINED_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
