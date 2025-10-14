package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeCaseLinkAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepCaseLinkAppealBeforeListingTemplateId;
    private final String legalRepCaseLinkAppealAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;

    public LegalRepresentativeCaseLinkAppealPersonalisation(
        @Value("${govnotify.template.createCaseLink.legalRep.beforeListing.email}") String legalRepCaseLinkAppealBeforeListingTemplateId,
        @Value("${govnotify.template.createCaseLink.legalRep.afterListing.email}") String legalRepCaseLinkAppealAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService) {
        this.legalRepCaseLinkAppealBeforeListingTemplateId = legalRepCaseLinkAppealBeforeListingTemplateId;
        this.legalRepCaseLinkAppealAfterListingTemplateId = legalRepCaseLinkAppealAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase) ? legalRepCaseLinkAppealAfterListingTemplateId
            : legalRepCaseLinkAppealBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_CASE_LINK_APPEAL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("reason", getReason(asylumCase))
                .build();
    }

    private String getReason(AsylumCase asylumCase) {
        Optional<String> reasonOptional = asylumCase.read(
            APPEAL_REFERENCE_NUMBER, String.class);
        if (reasonOptional.isPresent()) {
            return reasonOptional.get();
        }
        return StringUtils.EMPTY;
    }
}
