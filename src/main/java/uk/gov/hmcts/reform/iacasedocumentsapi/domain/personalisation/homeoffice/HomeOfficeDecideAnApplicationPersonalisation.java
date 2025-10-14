package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.CITIZEN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.HOME_OFFICE_APC;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.HOME_OFFICE_GENERIC;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.HOME_OFFICE_LART;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.HOME_OFFICE_POU;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.LEGAL_REPRESENTATIVE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.getAdminOfficerRoles;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Slf4j
@Service
public class HomeOfficeDecideAnApplicationPersonalisation implements EmailNotificationPersonalisation {
    private final String homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId;
    private final String homeOfficeDecideAnApplicationGrantedAfterListingTemplateId;
    private final String homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
    private final String homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;
    private final String homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId;
    private final String homeOfficeDecideAnApplicationRefusedAfterListingTemplateId;
    private final String homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
    private final String homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;

    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final String iaExUiFrontendUrl;
    private final MakeAnApplicationService makeAnApplicationService;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public HomeOfficeDecideAnApplicationPersonalisation(
        @Value("${govnotify.template.decideAnApplication.granted.applicant.homeOffice.beforeListing.email}") String homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.granted.applicant.homeOffice.afterListing.email}") String homeOfficeDecideAnApplicationGrantedAfterListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.granted.otherParty.homeOffice.beforeListing.email}") String homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.granted.otherParty.homeOffice.afterListing.email}") String homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.refused.applicant.homeOffice.beforeListing.email}") String homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.refused.applicant.homeOffice.afterListing.email}") String homeOfficeDecideAnApplicationRefusedAfterListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.refused.otherParty.homeOffice.beforeListing.email}") String homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId,
        @Value("${govnotify.template.decideAnApplication.refused.otherParty.homeOffice.afterListing.email}") String homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId,
        @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
        @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        CustomerServicesProvider customerServicesProvider,
        MakeAnApplicationService makeAnApplicationService,
        EmailAddressFinder emailAddressFinder
    ) {
        this.homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId = homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId;
        this.homeOfficeDecideAnApplicationGrantedAfterListingTemplateId = homeOfficeDecideAnApplicationGrantedAfterListingTemplateId;
        this.homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId = homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
        this.homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId = homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;

        this.homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId = homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId;
        this.homeOfficeDecideAnApplicationRefusedAfterListingTemplateId = homeOfficeDecideAnApplicationRefusedAfterListingTemplateId;
        this.homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId = homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
        this.homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId = homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;

        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.makeAnApplicationService = makeAnApplicationService;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<MakeAnApplication> maybeMakeAnApplication = getMakeAnApplication(asylumCase);

        if (maybeMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = maybeMakeAnApplication.get();
            String decision = makeAnApplication.getDecision();
            UserRole applicantRole = UserRole.getById(makeAnApplication.getApplicantRole());

            String listingRef = asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(null);

            boolean isHomeOfficeUser = List.of(HOME_OFFICE_APC,
                HOME_OFFICE_LART,
                HOME_OFFICE_POU,
                HOME_OFFICE_GENERIC).contains(applicantRole);

            if (listingRef != null) {
                if ("Granted".equals(decision)) {
                    return isHomeOfficeUser ? homeOfficeDecideAnApplicationGrantedAfterListingTemplateId : homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;
                } else {
                    return isHomeOfficeUser ? homeOfficeDecideAnApplicationRefusedAfterListingTemplateId : homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;
                }
            } else {
                if ("Granted".equals(decision)) {
                    return isHomeOfficeUser ? homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId : homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
                } else {
                    return isHomeOfficeUser ? homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId : homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
                }
            }
        } else {
            return "";
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Optional<MakeAnApplication> maybeMakeAnApplication = getMakeAnApplication(asylumCase);

        if (maybeMakeAnApplication.isPresent()) {
            MakeAnApplication makeAnApplication = maybeMakeAnApplication.get();
            State applicationState = State.get(makeAnApplication.getState());
            boolean isAppealListed = makeAnApplicationService.isApplicationListed(applicationState);

            UserRole applicantRole = UserRole.getById(makeAnApplication.getApplicantRole());

            ArrayList<UserRole> validRoles = new ArrayList<>(List.of(
                LEGAL_REPRESENTATIVE,
                CITIZEN,
                HOME_OFFICE_GENERIC));
            validRoles.addAll(getAdminOfficerRoles());
            boolean hasValidRole = validRoles.contains(applicantRole);
            if (applicantRole.equals(HOME_OFFICE_APC)
                || (hasValidRole
                && List.of(
                State.APPEAL_SUBMITTED,
                State.PENDING_PAYMENT,
                State.AWAITING_RESPONDENT_EVIDENCE,
                State.CASE_BUILDING,
                State.CASE_UNDER_REVIEW,
                State.AWAITING_REASONS_FOR_APPEAL,
                State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
                State.REASONS_FOR_APPEAL_SUBMITTED,
                State.ENDED).contains(applicationState))) {
                return Collections.singleton(apcHomeOfficeEmailAddress);
            } else if (HOME_OFFICE_LART.equals(applicantRole)
                || (hasValidRole
                && List.of(
                State.RESPONDENT_REVIEW,
                State.LISTING,
                State.SUBMIT_HEARING_REQUIREMENTS).contains(applicationState))) {
                return Collections.singleton(lartHomeOfficeEmailAddress);
            } else if (HOME_OFFICE_POU.equals(applicantRole)
                || (hasValidRole
                && isAppealListed)) {
                final Optional<HearingCentre> maybeCaseIsListed = asylumCase
                    .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

                if (maybeCaseIsListed.isPresent()) {
                    return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
                } else {
                    return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
                }
            } else {
                throw new IllegalStateException("homeOffice email Address cannot be found");
            }
        } else {
            throw new IllegalStateException("homeOffice email Address cannot be found");
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_DECIDE_AN_APPLICATION_HOME_OFFICE";
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
            .put("applicationType", getMakeAnApplication(asylumCase).map(MakeAnApplication::getType).orElse(""))
            .put("applicationDecisionReason", getMakeAnApplication(asylumCase).map(MakeAnApplication::getDecisionReason).orElse("No reason given"))
            .put("decisionMaker", getMakeAnApplication(asylumCase).map(MakeAnApplication::getDecisionMaker).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .build();
    }

    private Optional<MakeAnApplication> getMakeAnApplication(AsylumCase asylumCase) {
        return makeAnApplicationService.getMakeAnApplication(asylumCase, true);
    }

}
