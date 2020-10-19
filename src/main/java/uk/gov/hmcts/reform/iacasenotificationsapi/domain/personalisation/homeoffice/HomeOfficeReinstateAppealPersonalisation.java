package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeReinstateAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeReinstateAppealBeforeListingTemplateId;
    private final String homeOfficeReinstateAppealAfterListingTemplateId;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;


    public HomeOfficeReinstateAppealPersonalisation(
            @NotNull(message = "homeOfficeReinstateAppealBeforeListingTemplateId cannot be null")
            @Value("${govnotify.template.reinstateAppeal.homeOffice.beforeListing.email}") String homeOfficeReinstateAppealBeforeListingTemplateId,
            @NotNull(message = "homeOfficeReinstateAppealAfterListingTemplateId cannot be null")
            @Value("${govnotify.template.reinstateAppeal.homeOffice.afterListing.email}") String homeOfficeReinstateAppealAfterListingTemplateId,
            @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
            @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            AppealService appealService,
            EmailAddressFinder emailAddressFinder
    ) {
        this.homeOfficeReinstateAppealBeforeListingTemplateId = homeOfficeReinstateAppealBeforeListingTemplateId;
        this.homeOfficeReinstateAppealAfterListingTemplateId = homeOfficeReinstateAppealAfterListingTemplateId;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        boolean isAppealListed = appealService.isAppealListed(asylumCase);

        if (isAppealListed) {
            return homeOfficeReinstateAppealAfterListingTemplateId;
        } else {
            return homeOfficeReinstateAppealBeforeListingTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        State stateBeforeEndAppeal = asylumCase.read(AsylumCaseDefinition.STATE_BEFORE_END_APPEAL, State.class)
                .orElse(null);

        if (stateBeforeEndAppeal == null) {
            return Collections.emptySet();
        }

        if (Arrays.asList(
                State.APPEAL_SUBMITTED,
                State.PENDING_PAYMENT,
                State.AWAITING_RESPONDENT_EVIDENCE).contains(stateBeforeEndAppeal)) {
            return Collections.singleton(apcHomeOfficeEmailAddress);
        } else if (Arrays.asList(
                    State.CASE_BUILDING,
                    State.CASE_UNDER_REVIEW,
                    State.RESPONDENT_REVIEW,
                    State.SUBMIT_HEARING_REQUIREMENTS,
                    State.LISTING).contains(stateBeforeEndAppeal)) {
            return Collections.singleton(lartHomeOfficeEmailAddress);
        } else if (
                Arrays.asList(State.ADJOURNED,
                        State.PREPARE_FOR_HEARING,
                        State.FINAL_BUNDLING,
                        State.PRE_HEARING,
                        State.DECISION,
                        State.DECIDED,
                        State.FTPA_SUBMITTED,
                        State.FTPA_DECIDED).contains(stateBeforeEndAppeal)) {
            return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
        } else {
            throw new IllegalStateException("homeOffice email Address cannot be found");
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REINSTATE_APPEAL_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(AsylumCaseDefinition.ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("reinstateAppealDate", asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_DATE, String.class)
                        .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                        .orElse("")
                )
                .put("reinstateAppealReason", asylumCase.read(AsylumCaseDefinition.REINSTATE_APPEAL_REASON, String.class).orElse("No reason given"))
                .put("reinstatedDecisionMaker", asylumCase.read(AsylumCaseDefinition.REINSTATED_DECISION_MAKER, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
