package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;


import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepEmailInternalOrLegalRepJourney;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeRemoveRepresentationPersonalisation implements EmailNotificationPersonalisation {

    private final String removeRepresentationLegalRepresentativeBeforeListingTemplateId;
    private final String removeRepresentationLegalRepresentativeAfterListingTemplateId;
    private final AppealService appealService;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeRemoveRepresentationPersonalisation(
        @NotNull(message = "removeRepresentationLegalRepresentativeBeforeListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.legalRep.beforeListing.email}") String removeRepresentationLegalRepresentativeBeforeListingTemplateId,
        @NotNull(message = "removeRepresentationLegalRepresentativeAfterListingTemplateId cannot be null") @Value("${govnotify.template.removeRepresentation.legalRep.afterListing.email}") String removeRepresentationLegalRepresentativeAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        AppealService appealService,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.removeRepresentationLegalRepresentativeBeforeListingTemplateId = removeRepresentationLegalRepresentativeBeforeListingTemplateId;
        this.removeRepresentationLegalRepresentativeAfterListingTemplateId = removeRepresentationLegalRepresentativeAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appealService = appealService;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase)
            ? removeRepresentationLegalRepresentativeAfterListingTemplateId
            : removeRepresentationLegalRepresentativeBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(getLegalRepEmailInternalOrLegalRepJourney(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_REPRESENTATION_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("ccdCaseId", String.valueOf(callback.getCaseDetails().getId()))
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }
}
