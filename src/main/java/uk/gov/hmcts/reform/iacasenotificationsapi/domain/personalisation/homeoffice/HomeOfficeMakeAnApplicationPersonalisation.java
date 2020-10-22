package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;


@Slf4j
@Service
public class HomeOfficeMakeAnApplicationPersonalisation implements EmailNotificationPersonalisation {

    private static final String HOME_OFFICE_RESPONDENT_OFFICER = "caseworker-ia-respondentofficer";
    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";
    private static final String HOME_OFFICE_LART = "caseworker-ia-homeofficelart";
    private static final String HOME_OFFICE_APC = "caseworker-ia-homeofficeapc";
    private static final String HOME_OFFICE_POU = "caseworker-ia-homeofficepou";


    private final String homeOfficeMakeAnApplicationBeforeListingTemplateId;
    private final String homeOfficeMakeAnApplicationAfterListingTemplateId;
    private final String homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId;
    private final String homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;
    private final AppealService appealService;
    private final MakeAnApplicationService makeAnApplicationService;
    private final UserDetailsProvider userDetailsProvider;
    private final EmailAddressFinder emailAddressFinder;

    public HomeOfficeMakeAnApplicationPersonalisation(
            @Value("${govnotify.template.makeAnApplication.beforeListing.homeOffice.email}") String homeOfficeMakeAnApplicationBeforeListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.homeOffice.email}") String homeOfficeMakeAnApplicationAfterListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.beforeListing.otherParty.homeOffice.email}") String homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId,
            @Value("${govnotify.template.makeAnApplication.afterListing.otherParty.homeOffice.email}") String homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId,
            @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
            @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            AppealService appealService,
            UserDetailsProvider userDetailsProvider,
            EmailAddressFinder emailAddressFinder,
            MakeAnApplicationService makeAnApplicationService
    ) {
        this.homeOfficeMakeAnApplicationBeforeListingTemplateId = homeOfficeMakeAnApplicationBeforeListingTemplateId;
        this.homeOfficeMakeAnApplicationAfterListingTemplateId = homeOfficeMakeAnApplicationAfterListingTemplateId;
        this.homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId = homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId;
        this.homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId = homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
        this.userDetailsProvider = userDetailsProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.makeAnApplicationService = makeAnApplicationService;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        boolean isAppealListed = appealService.isAppealListed(asylumCase);
        boolean isHomeOfficeUser = hasRoles(Arrays.asList(HOME_OFFICE_APC,
                                 HOME_OFFICE_LART,
                                HOME_OFFICE_POU,
                                HOME_OFFICE_RESPONDENT_OFFICER));

        if (isAppealListed) {
            return isHomeOfficeUser ? homeOfficeMakeAnApplicationAfterListingTemplateId : homeOfficeMakeAnApplicationOtherPartyAfterListingTemplateId;
        } else {
            return isHomeOfficeUser ? homeOfficeMakeAnApplicationBeforeListingTemplateId : homeOfficeMakeAnApplicationOtherPartyBeforeListingTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        State currentState = asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
                .orElse(null);

        if (currentState == null) {
            return Collections.emptySet();
        }

        if (hasRoles(Arrays.asList(HOME_OFFICE_APC))
                || (hasRoles(Arrays.asList(ROLE_LEGAL_REP, HOME_OFFICE_RESPONDENT_OFFICER))
                && Arrays.asList(State.APPEAL_SUBMITTED,
                            State.PENDING_PAYMENT,
                            State.AWAITING_RESPONDENT_EVIDENCE,
                            State.CASE_BUILDING,
                            State.CASE_UNDER_REVIEW,
                            State.ENDED
                ).contains(currentState))) {
            return Collections.singleton(apcHomeOfficeEmailAddress);
        } else if (hasRoles(Arrays.asList(HOME_OFFICE_LART))
                || (hasRoles(Arrays.asList(ROLE_LEGAL_REP, HOME_OFFICE_RESPONDENT_OFFICER))
                && Arrays.asList(State.RESPONDENT_REVIEW,
                        State.LISTING,
                        State.SUBMIT_HEARING_REQUIREMENTS).contains(currentState))) {
            return Collections.singleton(lartHomeOfficeEmailAddress);
        } else if (hasRoles(Arrays.asList(HOME_OFFICE_POU))
                || (hasRoles(Arrays.asList(ROLE_LEGAL_REP, HOME_OFFICE_RESPONDENT_OFFICER))
                && (Arrays.asList(State.ADJOURNED,
                    State.PREPARE_FOR_HEARING,
                    State.FINAL_BUNDLING,
                    State.PRE_HEARING,
                    State.DECISION,
                    State.DECIDED,
                    State.FTPA_SUBMITTED,
                    State.FTPA_DECIDED).contains(currentState)))) {
            return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
        } else {
            throw new IllegalStateException("homeOffice email Address cannot be found");
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MAKE_AN_APPLICATION_HOME_OFFICE";
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
                .put("applicationType", makeAnApplicationService.getMakeAnApplicationTypeName(asylumCase))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }

    private boolean hasRoles(List<String> roles) {
        return userDetailsProvider
                .getUserDetails()
                .getRoles()
                .stream()
                .anyMatch(r -> roles.contains(r));
    }
}
