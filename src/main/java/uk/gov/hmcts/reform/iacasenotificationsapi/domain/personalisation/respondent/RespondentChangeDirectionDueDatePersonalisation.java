package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class RespondentChangeDirectionDueDatePersonalisation implements EmailNotificationPersonalisation {

    public static final String CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT = "currentCaseStateVisibleToHomeOfficeAll flag is not present";
    private final String respondentChangeDirectionDueDateBeforeListingTemplateId;
    private final String respondentChangeDirectionDueDateAfterListingTemplateId;
    private final String respondentChangeAppellantDirectionDueDateTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public RespondentChangeDirectionDueDatePersonalisation(
        @Value("${govnotify.template.changeDirectionDueDate.respondent.afterListing.email}") String respondentChangeDirectionDueDateAfterListingTemplateId,
        @Value("${govnotify.template.changeDirectionDueDate.respondent.beforeListing.email}") String respondentChangeDirectionDueDateBeforeListingTemplateId,
        @Value("${govnotify.template.changeDirectionDueDateOfAppellant.homeOffice.email}") String respondentChangeAppellantDirectionDueDateTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
        @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService,
        EmailAddressFinder emailAddressFinder
    ) {
        requireNonNull(iaExUiFrontendUrl, "iaExUiFrontendUrl must not be null");
        this.respondentChangeDirectionDueDateAfterListingTemplateId = respondentChangeDirectionDueDateAfterListingTemplateId;
        this.respondentChangeDirectionDueDateBeforeListingTemplateId = respondentChangeDirectionDueDateBeforeListingTemplateId;
        this.respondentChangeAppellantDirectionDueDateTemplateId = respondentChangeAppellantDirectionDueDateTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (isAipJourney(asylumCase) && !isDirectionToRespondent(asylumCase)) {
            return respondentChangeAppellantDirectionDueDateTemplateId;
        } else {
            return appealService.isAppealListed(asylumCase)
                ? respondentChangeDirectionDueDateAfterListingTemplateId
                : respondentChangeDirectionDueDateBeforeListingTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {

        return asylumCase.read(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL, State.class)
            .map(currentState -> {
                if (Arrays.asList(
                    State.APPEAL_SUBMITTED,
                    State.PENDING_PAYMENT,
                    State.AWAITING_RESPONDENT_EVIDENCE,
                    State.AWAITING_REASONS_FOR_APPEAL,
                    State.REASONS_FOR_APPEAL_SUBMITTED,
                    State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
                    State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED,
                    State.AWAITING_CMA_REQUIREMENTS,
                    State.CMA_REQUIREMENTS_SUBMITTED,
                    State.CMA_ADJUSTMENTS_AGREED
                ).contains(currentState)) {
                    return Collections.singleton(apcHomeOfficeEmailAddress);
                } else if (Arrays.asList(
                    State.CASE_BUILDING,
                    State.CASE_UNDER_REVIEW,
                    State.RESPONDENT_REVIEW
                ).contains(currentState)) {
                    return Collections.singleton(lartHomeOfficeEmailAddress);
                } else if (Arrays.asList(
                    State.FTPA_SUBMITTED,
                    State.FTPA_DECIDED).contains(currentState)) {
                    return Collections.singleton(emailAddressFinder.getListCaseFtpaHomeOfficeEmailAddress(asylumCase));
                } else if (Arrays.asList(
                    State.LISTING,
                    State.SUBMIT_HEARING_REQUIREMENTS,
                    State.ENDED,
                    State.APPEAL_TAKEN_OFFLINE).contains(currentState)
                           && !appealService.isAppealListed(asylumCase)) {
                    return  Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
                } else if (Arrays.asList(
                    State.PREPARE_FOR_HEARING,
                    State.FINAL_BUNDLING,
                    State.PRE_HEARING,
                    State.DECISION,
                    State.ADJOURNED,
                    State.DECIDED,
                    State.ENDED,
                    State.APPEAL_TAKEN_OFFLINE,
                    State.CMA_LISTED
                ).contains(currentState) && appealService.isAppealListed(asylumCase)) {
                    final Optional<HearingCentre> maybeCaseIsListed = asylumCase
                        .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

                    if (maybeCaseIsListed.isPresent()) {
                        return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
                    } else {
                        return  Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
                    }
                }
                throw new IllegalStateException(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT);
            })
            .orElseThrow(() -> new IllegalStateException(CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_CHANGE_DIRECTION_DUE_DATE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        String listingReferenceLine = callback.getCaseDetails().getCaseData()
            .read(ARIA_LISTING_REFERENCE, String.class)
            .map(ref -> "\nListing reference: " + ref)
            .orElse("");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData())
                ? adaPrefix
                : nonAdaPrefix)
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("listingReferenceLine", listingReferenceLine)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    private boolean isAipJourney(AsylumCase asylumCase) {

        return asylumCase
            .read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);
    }

    private boolean isDirectionToRespondent(AsylumCase asylumCase) {
        return asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
            .map(Parties -> Parties.equals(Parties.RESPONDENT))
            .orElse(false);
    }
}