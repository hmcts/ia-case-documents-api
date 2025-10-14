package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import static java.util.Objects.requireNonNull;

@Service
public class HomeOfficeReinstateAppealPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeReinstateAppealBeforeListingTemplateId;
    private final String homeOfficeReinstateAppealAfterListingTemplateId;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final String endAppealEmailAddresses;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;
    private final String iaExUiFrontendUrl;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;


    public HomeOfficeReinstateAppealPersonalisation(
            @NotNull(message = "homeOfficeReinstateAppealBeforeListingTemplateId cannot be null")
            @Value("${govnotify.template.reinstateAppeal.homeOffice.beforeListing.email}") String homeOfficeReinstateAppealBeforeListingTemplateId,
            @NotNull(message = "homeOfficeReinstateAppealAfterListingTemplateId cannot be null")
            @Value("${govnotify.template.reinstateAppeal.homeOffice.afterListing.email}") String homeOfficeReinstateAppealAfterListingTemplateId,
            @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
            @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
            @Value("${endAppealHomeOfficeEmailAddress}") String endAppealEmailAddresses,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            AppealService appealService,
            EmailAddressFinder emailAddressFinder
    ) {
        this.homeOfficeReinstateAppealBeforeListingTemplateId = homeOfficeReinstateAppealBeforeListingTemplateId;
        this.homeOfficeReinstateAppealAfterListingTemplateId = homeOfficeReinstateAppealAfterListingTemplateId;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.endAppealEmailAddresses = endAppealEmailAddresses;
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

        if (isAppealListed(asylumCase)) {
            return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
        } else if (Arrays.asList(
                State.APPEAL_SUBMITTED,
                State.PENDING_PAYMENT,
                State.REASONS_FOR_APPEAL_SUBMITTED,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
                State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED).contains(stateBeforeEndAppeal)) {
            return Collections.singleton(apcHomeOfficeEmailAddress);
        } else if (Arrays.asList(
                    State.CASE_BUILDING,
                    State.CASE_UNDER_REVIEW,
                    State.RESPONDENT_REVIEW,
                    State.SUBMIT_HEARING_REQUIREMENTS,
                    State.AWAITING_REASONS_FOR_APPEAL,
                    State.LISTING).contains(stateBeforeEndAppeal)) {
            return Collections.singleton(lartHomeOfficeEmailAddress);
        } else {
            return Collections.singleton(endAppealEmailAddresses);
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
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
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

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
                .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
