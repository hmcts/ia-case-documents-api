package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.legalrepresentative.linkunlinkappeal;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class LegalRepresentativeUnlinkAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepUnlinkAppealBeforeListingTemplateId;
    private final String legalRepUnlinkAppealAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeUnlinkAppealPersonalisation(
        @Value("${govnotify.template.unlinkAppealBeforeListing.legalRep.email}")
            String legalRepUnlinkAppealBeforeListingTemplateId,
        @Value("${govnotify.template.unlinkAppealAfterListing.legalRep.email}")
            String legalRepUnlinkAppealAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService) {
        this.legalRepUnlinkAppealBeforeListingTemplateId = legalRepUnlinkAppealBeforeListingTemplateId;
        this.legalRepUnlinkAppealAfterListingTemplateId = legalRepUnlinkAppealAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase) ? legalRepUnlinkAppealAfterListingTemplateId
            : legalRepUnlinkAppealBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REPRESENTATIVE_UNLINK_APPEAL";
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
                .build();
    }
}
