package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AppealService;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class RespondentNonStandardDirectionPersonalisation implements EmailNotificationPersonalisation {

    public static final String CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL_FLAG_IS_NOT_PRESENT = "currentCaseStateVisibleToHomeOfficeAll flag is not present";
    private final String respondentNonStandardDirectionBeforeListingTemplateId;
    private final String respondentNonStandardDirectionAfterListingTemplateId;
    private final String respondentNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
    private final String respondentNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final String apcHomeOfficeEmailAddress;
    private final String lartHomeOfficeEmailAddress;
    private final DirectionFinder directionFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final AppealService appealService;
    private final EmailAddressFinder emailAddressFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public RespondentNonStandardDirectionPersonalisation(
        @Value("${govnotify.template.nonStandardDirectionBeforeListing.respondent.email}") String respondentNonStandardDirectionBeforeListingTemplateId,
        @Value("${govnotify.template.nonStandardDirectionAfterListing.respondent.email}") String respondentNonStandardDirectionAfterListingTemplateId,
        @Value("${govnotify.template.nonStandardDirectionToAppellantAndRespondentBeforeListing.respondent.email}") String respondentNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId,
        @Value("${govnotify.template.nonStandardDirectionToAppellantAndRespondentAfterListing.respondent.email}") String respondentNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        @Value("${apcHomeOfficeEmailAddress}") String apcHomeOfficeEmailAddress,
        @Value("${lartHomeOfficeEmailAddress}") String lartHomeOfficeEmailAddress,
        DirectionFinder directionFinder,
        CustomerServicesProvider customerServicesProvider,
        AppealService appealService,
        EmailAddressFinder emailAddressFinder
    ) {
        this.respondentNonStandardDirectionBeforeListingTemplateId = respondentNonStandardDirectionBeforeListingTemplateId;
        this.respondentNonStandardDirectionAfterListingTemplateId = respondentNonStandardDirectionAfterListingTemplateId;
        this.respondentNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId = respondentNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
        this.respondentNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId = respondentNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.apcHomeOfficeEmailAddress = apcHomeOfficeEmailAddress;
        this.lartHomeOfficeEmailAddress = lartHomeOfficeEmailAddress;
        this.directionFinder = directionFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.appealService = appealService;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (directionFinder
                .findFirst(asylumCase, DirectionTag.NONE)
                .map(direction -> direction.getParties().equals(Parties.APPELLANT_AND_RESPONDENT))
                .orElse(false)) {
            return appealService.isAppealListed(asylumCase)
                    ? respondentNonStandardDirectionToAppellantAndRespondentAfterListingTemplateId : respondentNonStandardDirectionToAppellantAndRespondentBeforeListingTemplateId;
        } else {
            return appealService.isAppealListed(asylumCase)
                    ? respondentNonStandardDirectionAfterListingTemplateId : respondentNonStandardDirectionBeforeListingTemplateId;
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
                        State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS,
                        State.CLARIFYING_QUESTIONS_ANSWERS_SUBMITTED
                ).contains(currentState)) {
                    return Collections.singleton(apcHomeOfficeEmailAddress);
                } else if (Arrays.asList(
                        State.CASE_BUILDING,
                        State.CASE_UNDER_REVIEW,
                        State.RESPONDENT_REVIEW,
                        State.AWAITING_REASONS_FOR_APPEAL,
                        State.REASONS_FOR_APPEAL_SUBMITTED
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
                        State.APPEAL_TAKEN_OFFLINE
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
        return caseId + "_RESPONDENT_NON_STANDARD_DIRECTION";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
                directionFinder
                        .findFirst(asylumCase, DirectionTag.NONE)
                        .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        final String directionDueDate =
                LocalDate
                        .parse(direction.getDateDue())
                        .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .put("explanation", direction.getExplanation())
                .put("dueDate", directionDueDate)
                .build();
    }
}
