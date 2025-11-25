package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeTransferOutOfAdaPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String transferOutOfAdaBeforeListingLegalRepresentativeTemplateId;
    private final String transferOutOfAdaAfterListingLegalRepresentativeTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;


    public LegalRepresentativeTransferOutOfAdaPersonalisation(
            @NotNull(message = "transferOutOfAdaBeforeListingLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.transferOutOfAda.legalRep.beforeListing.email}") String transferOutOfAdaBeforeListingLegalRepresentativeTemplateId,
            @NotNull(message = "transferOutOfAdaAfterListingLegalRepresentativeTemplateId cannot be null")
            @Value("${govnotify.template.transferOutOfAda.legalRep.afterListing.email}") String transferOutOfAdaAfterListingLegalRepresentativeTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider
    ) {
        this.transferOutOfAdaBeforeListingLegalRepresentativeTemplateId = transferOutOfAdaBeforeListingLegalRepresentativeTemplateId;
        this.transferOutOfAdaAfterListingLegalRepresentativeTemplateId = transferOutOfAdaAfterListingLegalRepresentativeTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? transferOutOfAdaAfterListingLegalRepresentativeTemplateId : transferOutOfAdaBeforeListingLegalRepresentativeTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_TRANSFER_OUT_OF_ADA_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("transferOutOfAdaReason", asylumCase.read(AsylumCaseDefinition.TRANSFER_OUT_OF_ADA_REASON, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
