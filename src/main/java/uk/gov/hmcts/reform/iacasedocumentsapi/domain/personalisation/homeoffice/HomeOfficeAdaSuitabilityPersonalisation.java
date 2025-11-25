package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AdaSuitabilityReviewDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeAdaSuitabilityPersonalisation implements EmailNotificationPersonalisation {

    private final String adaSuitabilityUnsuitableHomeOfficeTemplateId;
    private final String adaSuitabilitySuitableHomeOfficeTemplateId;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;

    public HomeOfficeAdaSuitabilityPersonalisation(
            @NotNull(message = "adaSuitabilityUnsuitableHomeOfficeTemplateId cannot be null")
            @Value("${govnotify.template.adaSuitabilityReview.homeOffice.unsuitable.email}") String adaSuitabilityUnsuitableHomeOfficeTemplateId,
            @Value("${govnotify.template.adaSuitabilityReview.homeOffice.suitable.email}") String adaSuitabilitySuitableHomeOfficeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            EmailAddressFinder emailAddressFinder,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.adaSuitabilityUnsuitableHomeOfficeTemplateId = adaSuitabilityUnsuitableHomeOfficeTemplateId;
        this.adaSuitabilitySuitableHomeOfficeTemplateId = adaSuitabilitySuitableHomeOfficeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        AdaSuitabilityReviewDecision decision =
                asylumCase.read(SUITABILITY_REVIEW_DECISION, AdaSuitabilityReviewDecision.class)
                        .orElseThrow(() -> new IllegalStateException("suitabilityReviewDecision is not present"));

        if (decision.toString().equals(AdaSuitabilityReviewDecision.UNSUITABLE.toString())) {
            return adaSuitabilityUnsuitableHomeOfficeTemplateId;
        } else {
            return adaSuitabilitySuitableHomeOfficeTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_ADA_SUITABILITY_DETERMINED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

}
