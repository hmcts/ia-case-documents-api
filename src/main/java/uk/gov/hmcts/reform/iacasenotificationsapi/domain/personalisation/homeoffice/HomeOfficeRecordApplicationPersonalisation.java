package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

@Service
public class HomeOfficeRecordApplicationPersonalisation implements EmailNotificationPersonalisation {

    private final String recordRefusedApplicationHomeOfficeBeforeListingTemplateId;
    private final String recordRefusedApplicationHomeOfficeAfterListingTemplateId;
    private final String recordApplicationHomeOfficeEmailAddress;
    private final Map<HearingCentre, String> homeOfficeEmailAddresses;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;

    public HomeOfficeRecordApplicationPersonalisation(
        @Value("${govnotify.template.recordRefusedApplicationBeforeListing.homeOffice.email}") String recordRefusedApplicationHomeOfficeBeforeListingTemplateId,
        @Value("${govnotify.template.recordRefusedApplicationAfterListing.homeOffice.email}") String recordRefusedApplicationHomeOfficeAfterListingTemplateId,
        @Value("${endAppealHomeOfficeEmailAddress}") String recordApplicationHomeOfficeEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        Map<HearingCentre, String> homeOfficeEmailAddresses,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.recordRefusedApplicationHomeOfficeBeforeListingTemplateId = recordRefusedApplicationHomeOfficeBeforeListingTemplateId;
        this.recordRefusedApplicationHomeOfficeAfterListingTemplateId = recordRefusedApplicationHomeOfficeAfterListingTemplateId;
        this.recordApplicationHomeOfficeEmailAddress = recordApplicationHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? recordRefusedApplicationHomeOfficeAfterListingTemplateId : recordRefusedApplicationHomeOfficeBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(homeOfficeEmailAddresses::get)
            .orElse(recordApplicationHomeOfficeEmailAddress));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RECORD_APPLICATION_HOME_OFFICE";
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
            .put("applicationType", asylumCase.read(AsylumCaseDefinition.APPLICATION_TYPE, String.class).map(StringUtils::lowerCase).orElse(""))
            .put("applicationDecisionReason", asylumCase.read(AsylumCaseDefinition.APPLICATION_DECISION_REASON, String.class)
                .filter(StringUtils::isNotBlank)
                .orElse("No reason given")
            )
            .put("applicationSupplier", asylumCase.read(AsylumCaseDefinition.APPLICATION_SUPPLIER, String.class).map(StringUtils::lowerCase).orElse(""))
            .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
