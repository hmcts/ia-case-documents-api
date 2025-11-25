package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.CITIZEN;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.HOME_OFFICE_APC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.HOME_OFFICE_GENERIC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.HOME_OFFICE_LART;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.HOME_OFFICE_POU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole.getAdminOfficerRoles;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.UserRole;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.MakeAnApplicationService;


@Slf4j
@Service
public class HomeOfficeMakeAnApplicationPersonalisation implements EmailNotificationPersonalisation {
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

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

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
        boolean isHomeOfficeUser = hasRoles(List.of(HOME_OFFICE_APC,
            HOME_OFFICE_LART,
            HOME_OFFICE_POU,
            HOME_OFFICE_GENERIC));

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

        ArrayList<UserRole> validRoles = new ArrayList<>(List.of(
            LEGAL_REPRESENTATIVE,
            CITIZEN,
            HOME_OFFICE_GENERIC));
        validRoles.addAll(getAdminOfficerRoles());
        if (hasRole(HOME_OFFICE_APC)
            || (hasRoles(validRoles) && isValidStateForHomeOfficeApc(currentState))) {
            return Collections.singleton(apcHomeOfficeEmailAddress);
        } else if (hasRole(HOME_OFFICE_LART)
            || (hasRoles(validRoles) && isValidStateForHomeOfficeLart(currentState))) {
            return Collections.singleton(lartHomeOfficeEmailAddress);
        } else if (hasRole(HOME_OFFICE_POU)
            || (hasRoles(validRoles) && (isValidStateForHomeOfficePou(currentState)))) {
            if (appealService.isAppealListed(asylumCase)) {
                return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
            } else {
                return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
            }
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
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("applicationType", makeAnApplicationService.getMakeAnApplication(asylumCase, false).map(MakeAnApplication::getType).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    private boolean hasRoles(List<UserRole> roles) {
        List<String> rawRoles = roles.stream()
            .map(UserRole::toString)
            .toList();
        return userDetailsProvider
            .getUserDetails()
            .getRoles()
            .stream()
            .anyMatch(rawRoles::contains);
    }

    private boolean hasRole(UserRole role) {
        return userDetailsProvider
            .getUserDetails()
            .getRoles()
            .contains(role.toString());
    }

    private boolean isValidStateForHomeOfficeApc(State currentState) {
        return List.of(State.APPEAL_SUBMITTED,
            State.PENDING_PAYMENT,
            State.AWAITING_RESPONDENT_EVIDENCE,
            State.CASE_BUILDING,
            State.REASONS_FOR_APPEAL_SUBMITTED,
            State.AWAITING_REASONS_FOR_APPEAL,
            State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
            State.CASE_UNDER_REVIEW,
            State.ENDED
        ).contains(currentState);
    }

    private boolean isValidStateForHomeOfficeLart(State currentState) {
        return List.of(State.RESPONDENT_REVIEW,
            State.LISTING,
            State.SUBMIT_HEARING_REQUIREMENTS).contains(currentState);
    }

    private boolean isValidStateForHomeOfficePou(State currentState) {
        return List.of(State.ADJOURNED,
            State.PREPARE_FOR_HEARING,
            State.FINAL_BUNDLING,
            State.PRE_HEARING,
            State.DECISION,
            State.DECIDED,
            State.FTPA_SUBMITTED,
            State.FTPA_DECIDED,
            State.REMITTED
        ).contains(currentState);
    }
}
