package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeRemoveDetentionStatusPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String removeDetentionStatusLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;


    public LegalRepresentativeRemoveDetentionStatusPersonalisation(
            @NotNull(message = "removeDetentionStatusLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.removeDetentionStatus.legalRep.email}") String removeDetentionStatusLegalRepresentativeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.removeDetentionStatusLegalRepresentativeTemplateId = removeDetentionStatusLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return removeDetentionStatusLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_DETENTION_STATUS_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        String ariaListingReferenceIfPresent = asylumCase.read(ARIA_LISTING_REFERENCE, String.class)
                .map(ariaListingReference -> "Listing reference: " + ariaListingReference)
                .orElse("");
        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("ariaListingReferenceIfPresent", ariaListingReferenceIfPresent)
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
