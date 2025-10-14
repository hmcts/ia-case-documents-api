package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative.linkunlinkappeal;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeLinkAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepLinkAppealBeforeListingTemplateId;
    private final String legalRepLinkAppealAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeLinkAppealPersonalisation(
        @Value("${govnotify.template.linkAppealBeforeListing.legalRep.email}") String legalRepLinkAppealBeforeListingTemplateId,
        @Value("${govnotify.template.linkAppealAfterListing.legalRep.email}") String legalRepLinkAppealAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService) {
        this.legalRepLinkAppealBeforeListingTemplateId = legalRepLinkAppealBeforeListingTemplateId;
        this.legalRepLinkAppealAfterListingTemplateId = legalRepLinkAppealAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase) ? legalRepLinkAppealAfterListingTemplateId
            : legalRepLinkAppealBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_LINK_APPEAL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
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
        Optional<ReasonForLinkAppealOptions> reasonOptional = asylumCase.read(
            REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class);
        if (reasonOptional.isPresent()) {
            return reasonOptional.get().getId();
        }
        return StringUtils.EMPTY;
    }
}
