package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice.linkunlinkappeal;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REASON_FOR_LINK_APPEAL;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class HomeOfficeLinkAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String linkAppealHomeOfficeBeforeListingTemplateId;
    private final String linkAppealHomeOfficeAfterListingTemplateId;
    private final String linkAppealEmailAddress;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;

    public HomeOfficeLinkAppealPersonalisation(
        @NotNull(message = "linkAppealHomeOfficeBeforeListingTemplateId cannot be null")
        @Value("${govnotify.template.linkAppealBeforeListing.homeOffice.email}")
            String linkAppealHomeOfficeBeforeListingTemplateId,
        @NotNull(message = "linkAppealHomeOfficeAfterTemplateId cannot be null")
        @Value("${govnotify.template.linkAppealAfterListing.homeOffice.email}")
            String linkAppealHomeOfficeAfterListingTemplateId,
        @Value("${endAppealHomeOfficeEmailAddress}") String linkAppealEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService) {
        this.linkAppealHomeOfficeBeforeListingTemplateId = linkAppealHomeOfficeBeforeListingTemplateId;
        this.linkAppealHomeOfficeAfterListingTemplateId = linkAppealHomeOfficeAfterListingTemplateId;
        this.linkAppealEmailAddress = linkAppealEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appealService.isAppealListed(asylumCase)
            ? linkAppealHomeOfficeAfterListingTemplateId : linkAppealHomeOfficeBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(linkAppealEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LINK_APPEAL_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class)
                .orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class)
                .orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class)
                .orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("homeOfficeReferenceNumber",
                asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("reason", asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class)
                .map(ReasonForLinkAppealOptions::getId)
                .orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .build();
    }

}
