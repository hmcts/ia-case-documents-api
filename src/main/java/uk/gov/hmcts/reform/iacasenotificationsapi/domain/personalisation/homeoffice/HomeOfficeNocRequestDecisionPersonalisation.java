package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;



@Slf4j
@Service
public class HomeOfficeNocRequestDecisionPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeNocRequestDecisionBeforeListingTemplateId;
    private final String homeOfficeNocRequestDecisionAfterListingTemplateId;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;
    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;

    public HomeOfficeNocRequestDecisionPersonalisation(
            @Value("${govnotify.template.nocRequestDecision.homeOffice.beforeListing.email}") String homeOfficeNocRequestDecisionBeforeListingTemplateId,
            @Value("${govnotify.template.nocRequestDecision.homeOffice.afterListing.email}") String homeOfficeNocRequestDecisionAfterListingTemplateId,
            @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
            @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            AppealService appealService,
            EmailAddressFinder emailAddressFinder
    ) {
        this.homeOfficeNocRequestDecisionBeforeListingTemplateId = homeOfficeNocRequestDecisionBeforeListingTemplateId;
        this.homeOfficeNocRequestDecisionAfterListingTemplateId = homeOfficeNocRequestDecisionAfterListingTemplateId;
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
        return isAppealListed ? homeOfficeNocRequestDecisionAfterListingTemplateId : homeOfficeNocRequestDecisionBeforeListingTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        State currentState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
                .orElse(null);

        if (currentState == null) {
            return Collections.emptySet();
        }

        if (Arrays.asList(State.APPEAL_STARTED,
                            State.APPEAL_SUBMITTED,
                            State.PENDING_PAYMENT,
                            State.AWAITING_RESPONDENT_EVIDENCE,
                            State.CASE_BUILDING,
                            State.CASE_UNDER_REVIEW,
                            State.ENDED
                ).contains(currentState)) {
            return Collections.singleton(apcHomeOfficeEmailAddress);
        } else if (Arrays.asList(State.RESPONDENT_REVIEW,
                        State.LISTING,
                        State.SUBMIT_HEARING_REQUIREMENTS).contains(currentState)) {
            return Collections.singleton(lartHomeOfficeEmailAddress);
        } else if (Arrays.asList(State.ADJOURNED,
                    State.PREPARE_FOR_HEARING,
                    State.FINAL_BUNDLING,
                    State.PRE_HEARING,
                    State.DECISION,
                    State.DECIDED,
                    State.FTPA_SUBMITTED,
                    State.FTPA_DECIDED).contains(currentState)) {
            if (appealService.isAppealListed(asylumCase)) {
                return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
            } else {
                return  Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
            }
        } else {
            throw new IllegalStateException("homeOffice email Address cannot be found");
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_NOC_REQUEST_DECISION_HOME_OFFICE";
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
                .build();
    }
}
