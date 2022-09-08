package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.MakeAnApplicationService;

@Slf4j
@Service
public class HomeOfficeDecideAnApplicationPersonalisation implements EmailNotificationPersonalisation {

    private static final String HOME_OFFICE_RESPONDENT_OFFICER = "caseworker-ia-respondentofficer";
    private static final String ROLE_LEGAL_REP = "caseworker-ia-legalrep-solicitor";
    private static final String HOME_OFFICE_LART = "caseworker-ia-homeofficelart";
    private static final String HOME_OFFICE_APC = "caseworker-ia-homeofficeapc";
    private static final String HOME_OFFICE_POU = "caseworker-ia-homeofficepou";
    private static final String CITIZEN = "citizen";


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
            String applicantRole = makeAnApplication.getApplicantRole();

            boolean isApplicationListed = makeAnApplicationService.isApplicationListed(State.get(makeAnApplication.getState()));

            boolean isHomeOfficeUser = Arrays.asList(HOME_OFFICE_APC,
                    HOME_OFFICE_LART,
                    HOME_OFFICE_POU,
                    HOME_OFFICE_RESPONDENT_OFFICER).contains(applicantRole);

            if (isApplicationListed) {
                if ("Granted".equals(decision)) {
                    return isHomeOfficeUser ?  homeOfficeDecideAnApplicationGrantedAfterListingTemplateId : homeOfficeDecideAnApplicationGrantedOtherPartyAfterListingTemplateId;
                } else {
                    return isHomeOfficeUser ?  homeOfficeDecideAnApplicationRefusedAfterListingTemplateId : homeOfficeDecideAnApplicationRefusedOtherPartyAfterListingTemplateId;
                }
            } else {
                if ("Granted".equals(decision)) {
                    return isHomeOfficeUser ?  homeOfficeDecideAnApplicationGrantedBeforeListingTemplateId : homeOfficeDecideAnApplicationGrantedOtherPartyBeforeListingTemplateId;
                } else {
                    return isHomeOfficeUser ?  homeOfficeDecideAnApplicationRefusedBeforeListingTemplateId : homeOfficeDecideAnApplicationRefusedOtherPartyBeforeListingTemplateId;
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
            State applicationState =  State.get(makeAnApplication.getState());
            boolean isAppealListed = makeAnApplicationService.isApplicationListed(applicationState);

            String applicantRole = makeAnApplication.getApplicantRole();

            if (applicantRole.equals(HOME_OFFICE_APC)
                    || (Arrays.asList(ROLE_LEGAL_REP, CITIZEN, HOME_OFFICE_RESPONDENT_OFFICER).contains(applicantRole)
                    && Arrays.asList(
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
                    || (Arrays.asList(ROLE_LEGAL_REP, CITIZEN, HOME_OFFICE_RESPONDENT_OFFICER).contains(applicantRole)
                    && Arrays.asList(
                    State.RESPONDENT_REVIEW,
                    State.LISTING,
                    State.SUBMIT_HEARING_REQUIREMENTS).contains(applicationState))) {
                return Collections.singleton(lartHomeOfficeEmailAddress);
            } else if (HOME_OFFICE_POU.equals(applicantRole)
                    || (Arrays.asList(ROLE_LEGAL_REP, CITIZEN, HOME_OFFICE_RESPONDENT_OFFICER).contains(applicantRole)
                    &&  isAppealListed)) {
                final Optional<HearingCentre> maybeCaseIsListed = asylumCase
                        .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

                if (maybeCaseIsListed.isPresent()) {
                    return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
                } else {
                    return  Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
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
