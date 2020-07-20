package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;

import com.google.common.collect.ImmutableMap;
import com.microsoft.applicationinsights.core.dependencies.apachecommons.lang3.StringUtils;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;


@Service
public class HomeOfficeEndAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String endAppealHomeOfficeBeforeListingTemplateId;
    private final String endAppealHomeOfficeAfterListingTemplateId;
    private final String endAppealEmailAddresses;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private EmailAddressFinder emailAddressFinder;

    public HomeOfficeEndAppealPersonalisation(
            @NotNull(message = "endAppealHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.endAppealBeforeListing.homeOffice.email}") String endAppealHomeOfficeBeforeListingTemplateId,
            @NotNull(message = "endAppealHomeOfficeTemplateId cannot be null") @Value("${govnotify.template.endAppealAfterListing.homeOffice.email}") String endAppealHomeOfficeAfterListingTemplateId,
            @Value("${endAppealHomeOfficeEmailAddress}") String endAppealEmailAddresses,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            EmailAddressFinder emailAddressFinder
    ) {
        this.endAppealHomeOfficeBeforeListingTemplateId = endAppealHomeOfficeBeforeListingTemplateId;
        this.endAppealHomeOfficeAfterListingTemplateId = endAppealHomeOfficeAfterListingTemplateId;
        this.endAppealEmailAddresses = endAppealEmailAddresses;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? endAppealHomeOfficeAfterListingTemplateId : endAppealHomeOfficeBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return (isAppealListed(asylumCase))
                ? Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase)) :
                Collections.singleton(endAppealEmailAddresses);

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_END_APPEAL_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("outcomeOfAppeal", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME, String.class).orElse(""))
                .put("reasonsOfOutcome", asylumCase.read(AsylumCaseDefinition.END_APPEAL_OUTCOME_REASON, String.class)
                        .filter(StringUtils::isNotBlank)
                        .orElse("No reason")
                )
                .put("endAppealApprover", asylumCase.read(AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE, String.class).orElse(""))
                .put("endAppealDate", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                        .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                        .orElse("")
                )
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
