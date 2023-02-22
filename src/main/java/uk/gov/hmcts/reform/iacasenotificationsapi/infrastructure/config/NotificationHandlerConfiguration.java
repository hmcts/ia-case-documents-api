package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.OutOfTimeDecisionType.IN_TIME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.OutOfTimeDecisionType.UNKNOWN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.BUILD_CASE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PostSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.em.Bundle;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.ErrorHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.postsubmit.PostSubmitNotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecordApplicationRespondentFinder;

@Slf4j
@Configuration
public class NotificationHandlerConfiguration {

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> forceCaseProgressionNotificationHandler(
        @Qualifier("forceCaseProgressionNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> function = (callbackStage, callback) ->
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.FORCE_REQUEST_CASE_BUILDING;
        return new NotificationHandler(function, notificationGenerators);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editDocumentsNotificationHandler(
        @Qualifier("editDocumentsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> function = (callbackStage, callback) ->
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && callback.getEvent() == Event.EDIT_DOCUMENTS;
        return new NotificationHandler(function, notificationGenerators);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> unlinkAppealNotificationHandler(
        @Qualifier("unlinkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && Event.UNLINK_APPEAL.equals(callback.getEvent())
                        && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> unlinkAppealAppellantNotificationHandler(
        @Qualifier("unlinkAppealAppellantNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && Event.UNLINK_APPEAL.equals(callback.getEvent())
                        && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> linkAppealNotificationHandler(
        @Qualifier("linkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && Event.LINK_APPEAL.equals(callback.getEvent())
                        && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> linkAppealAppellantNotificationHandler(
        @Qualifier("linkAppealAppellantNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && Event.LINK_APPEAL.equals(callback.getEvent())
                        && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> reListCaseNotificationHandler(
        @Qualifier("reListCaseNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.RESTORE_STATE_FROM_ADJOURN,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestCaseEditNotificationHandler(
        @Qualifier("requestCaseEditNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_CASE_EDIT,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> endAppealNotificationHandler(
        @Qualifier("endAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.END_APPEAL
                       && isRepJourney(asylumCase);
            },
            notificationGenerators

        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> endAppealAipSmsAppellantNotificationHandler(
        @Qualifier("endAppealAipSmsAppellantNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);


                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.END_APPEAL
                        && isAipJourney(asylumCase)
                        && isSmsPreferred(asylumCase));
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> endAppealAipEmailAppellantNotificationHandler(
        @Qualifier("endAppealAipEmailAppellantNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = isAipJourney(asylumCase);

                final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

                Set<IdValue<Subscriber>> emailPreferred = maybeSubscribers
                        .orElse(Collections.emptyList()).stream()
                        .filter(subscriber -> YES.equals(subscriber.getValue().getWantsEmail()))
                        .collect(Collectors.toSet());

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.END_APPEAL
                        && isAipJourney
                        && emailPreferred.size() > 0
                        && emailPreferred.stream().findFirst().map(subscriberIdValue ->
                            subscriberIdValue.getValue().getEmail()).isPresent());
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> endAppealAipEmailRespondentNotificationHandler(
        @Qualifier("endAppealAipEmailRespondentNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = isAipJourney(asylumCase);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.END_APPEAL
                        && isAipJourney);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealOutcomeNotificationHandler(
        @Qualifier("appealOutcomeNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SEND_DECISION_AND_REASONS,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealOutcomeRepNotificationHandler(
        @Qualifier("appealOutcomeRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SEND_DECISION_AND_REASONS
                       && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealOutcomeAipNotificationHandler(
        @Qualifier("appealOutcomeAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = isAipJourney(asylumCase);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.SEND_DECISION_AND_REASONS
                        && isAipJourney);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealOutcomeHomeOfficeNotificationFailedNotificationHandler(
        @Qualifier("appealOutcomeHomeOfficeNotificationFailedNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                final String instructStatus = asylumCase.read(HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS, String.class)
                    .orElse("");

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DECISION_AND_REASONS
                    && instructStatus.equals("FAIL");
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentChangeDirectionDueDateNotificationHandler(
        @Qualifier("respondentChangeDirectionDueDateNotificationGenerator")
            List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.RESPONDENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                       && isRespondent
                       && !isOneOfHomeOfficeApiNotifications(callback)
                       && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentChangeDirectionDueDateAipNotificationHandler(
            @Qualifier("respondentChangeDirectionDueDateAipNotificationGenerator")
                    List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.RESPONDENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                       && isRespondent
                       && !isOneOfHomeOfficeApiNotifications(callback)
                       && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    private boolean isOneOfHomeOfficeApiNotifications(Callback<AsylumCase> callback) {

        return Arrays.asList(
            State.RESPONDENT_REVIEW,
            State.AWAITING_RESPONDENT_EVIDENCE
        ).contains(
            callback.getCaseDetails().getState()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationHandler(
        @Qualifier("respondentChangeDirectionDueDateForHomeOfficeApiEventsNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.RESPONDENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                       && isRespondent
                       && isOneOfHomeOfficeApiNotifications(callback)
                       && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentChangeDirectionDueDateForHomeOfficeApiEventsAipNotificationHandler(
        @Qualifier("respondentChangeDirectionDueDateForHomeOfficeApiEventsAipNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.RESPONDENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                       && isRespondent
                       && isOneOfHomeOfficeApiNotifications(callback)
                       && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> legalRepChangeDirectionDueDateNotificationHandler(
        @Qualifier("legalRepChangeDirectionDueDateNotificationGenerator")
            List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isLegalRepresentative = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.LEGAL_REPRESENTATIVE))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                       && isLegalRepresentative
                       && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> bothPartiesChangeDirectionDueDateNotificationHandler(
        @Qualifier("bothPartiesChangeDirectionDueDateNotificationGenerator")
            List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.BOTH))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                       && isRespondent
                       && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> listCaseNotificationHandler(
        @Qualifier("listCaseNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - listCase
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.LIST_CASE
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> listCaseAipNotificationHandler(
        @Qualifier("listCaseAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - listCase
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.LIST_CASE
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> addAppealNotificationHandler(
        @Qualifier("addAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.ADD_APPEAL_RESPONSE,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> hearingRequirementsNotificationHandler(
        @Qualifier("hearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestHearingRequirementsNotificationHandler(
        @Qualifier("requestHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE
                    && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestHearingRequirementsAipNotificationHandler(
        @Qualifier("requestHearingRequirementsAipNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestNewHearingRequirementsNotificationHandler(
        @Qualifier("requestNewHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isReheardAppealEnabled = asylumCase
                    .read(IS_REHEARD_APPEAL_ENABLED, YesOrNo.class)
                    .equals(Optional.of(YES));

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REQUEST_NEW_HEARING_REQUIREMENTS
                       && isReheardAppealEnabled;
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentEvidenceAipNotificationHandler(
        @Qualifier("respondentEvidenceAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = isAipJourney(asylumCase);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE
                       && isAipJourney;
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentEvidenceRepNotificationHandler(
        @Qualifier("respondentEvidenceRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - requestRespondentEvidence
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE
                       && isRepJourney(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentReviewNotificationHandler(
        @Qualifier("respondentReviewNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - requestRespondentReview
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW
                        && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentReviewAipNotificationHandler(
        @Qualifier("respondentReviewAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - requestRespondentReview
        return new NotificationHandler(
            (callbackStage, callback) ->
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW
                            && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }


    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealAipNotificationHandler(
        @Qualifier("submitAppealAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAppealOnTime = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == NO).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isAipJourney(asylumCase)
                       && isAppealOnTime
                       && !isEaHuEuAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> paymentPaidPostSubmitAipNotificationHandler(
        @Qualifier("paymentAppealAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        // This applies only to EA/HU/EUSS appeals

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean paymentPaid = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == PAID).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.PAYMENT_APPEAL
                    && paymentPaid
                    && isAipJourney(asylumCase)
                    && isEaHuEuAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealRepNotificationHandler(
        @Qualifier("submitAppealRepNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean paymentFailed = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == FAILED || paymentStatus == TIMEOUT).orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean paymentFailedChangedToPayLater = paymentFailed && payLater;

                boolean isRemissionApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.SUBMIT_APPEAL
                        && callback.getCaseDetails().getCaseData()
                            .read(JOURNEY_TYPE, JourneyType.class)
                            .map(type -> type == REP).orElse(true)
                        && (!paymentFailed || paymentFailedChangedToPayLater)
                        && !isPaymentPendingForEaOrHuAppeal(callback))
                       || (callback.getEvent() == Event.RECORD_REMISSION_DECISION
                           && isRemissionApproved
                           && isEaHuEuAppeal(asylumCase));
            }, notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitCaseRepSubmitToRepNotificationHandler(
        @Qualifier("submitCaseRepSubmitToRepNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                List<Event> validEvent = Arrays.asList(Event.SUBMIT_CASE, BUILD_CASE);
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && validEvent.contains(callback.getEvent())
                       && callback.getCaseDetails().getCaseData()
                           .read(JOURNEY_TYPE, JourneyType.class)
                           .map(type -> type == REP).orElse(true);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealHoNotificationHandler(
        @Qualifier("submitAppealHoNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        // RIA-3631 - submitAppeal
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean paymentPaid = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == PAID).orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isRepJourney(asylumCase)
                       && (paymentPaid || payLater);

            }, notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealOutOfTimeAipNotificationHandler(
        @Qualifier("submitAppealOutOfTimeAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isOutOfTimeAppeal = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == YES).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isAipJourney(asylumCase)
                       && isOutOfTimeAppeal
                       && !isEaHuEuAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitCaseNotificationHandler(
        @Qualifier("submitCaseNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                List<Event> validEvent = Arrays.asList(Event.SUBMIT_CASE, BUILD_CASE);
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && validEvent.contains(callback.getEvent());
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadRespondentNotificationHandler(
        @Qualifier("uploadRespondentNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_RESPONDENT_EVIDENCE,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestReasonForAppealUploadAipNotificationHandler(
        @Qualifier("requestReasonsForAppealAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_REASONS_FOR_APPEAL
                && callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitReasonsForAppealAipNotificationHandler(
        @Qualifier("submitReasonsForAppealAipNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_REASONS_FOR_APPEAL
                && callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentEvidenceSubmittedHandler(
        @Qualifier("respondentEvidenceSubmitted") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_HOME_OFFICE_BUNDLE,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestResponseAmendDirectionhandler(
        @Qualifier("requestResponseAmendDirectionGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        // RIA-3631 - requestResponseAmend
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_RESPONSE_AMEND
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestResponseAmendAipDirectionhandler(
        @Qualifier("requestResponseAmendAipDirectionGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        // RIA-3631 - requestResponseAmend
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_RESPONSE_AMEND
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestCaseBuildingNotificationHandler(
        @Qualifier("requestCaseBuildingNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_CASE_BUILDING,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentDirectionNotificationHandler(
        @Qualifier("respondentDirectionNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        // RIA-3631 sendDirection (awaitingRespondentEvidence only)
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SEND_DIRECTION
                       && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.RESPONDENT)
                       && callback.getCaseDetails().getState() != State.AWAITING_RESPONDENT_EVIDENCE;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> awaitingRespondentDirectionNotificationHandler(
        @Qualifier("awaitingRespondentDirectionNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        // RIA-3631 sendDirection (awaitingRespondentEvidence only)
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SEND_DIRECTION
                       && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.RESPONDENT)
                       && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE
                       && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> awaitingRespondentDirectionAipNotificationHandler(
            @Qualifier("awaitingRespondentDirectionAipNotificationGenerator") List<NotificationGenerator> notificationGenerators,
            DirectionFinder directionFinder) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase =
                            callback
                                    .getCaseDetails()
                                    .getCaseData();

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.SEND_DIRECTION
                            && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.RESPONDENT)
                            && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE
                            && isAipJourney(asylumCase)
                            && !notificationGenerators.isEmpty();
                },
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> legalRepDirectionNotificationHandler(
        @Qualifier("legalRepDirectionNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SEND_DIRECTION
                       &&
                       isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.LEGAL_REPRESENTATIVE);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> bothPartiesNonStandardDirectionHandler(
        @Qualifier("bothPartiesNonStandardDirectionGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SEND_DIRECTION
                       && directionFinder
                           .findFirst(asylumCase, DirectionTag.NONE)
                           .map(direction -> direction.getParties().equals(Parties.BOTH))
                           .orElse(false);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordApplicationNotificationHandler(
        @Qualifier("recordApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        RecordApplicationRespondentFinder recordApplicationRespondentFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isApplicationRefused = asylumCase
                    .read(AsylumCaseDefinition.APPLICATION_DECISION, String.class)
                    .map(decision -> decision.equals(ApplicationDecision.REFUSED.toString()))
                    .orElse(false);

                boolean requiresEmail = recordApplicationRespondentFinder.requiresEmail(asylumCase);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RECORD_APPLICATION
                       && isApplicationRefused
                       && requiresEmail;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editCaseListingRepNotificationHandler(
        @Qualifier("editCaseListingRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - editCaseListing
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_CASE_LISTING
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editCaseListingAipNotificationHandler(
        @Qualifier("editCaseListingAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_CASE_LISTING
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadHomeOfficeAppealResponseNotificationHandler(
        @Qualifier("uploadHomeOfficeAppealResponseNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestResponseReviewNotificationHandler(
        @Qualifier("requestResponseReviewNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestResponseReviewAipNotificationHandler(
        @Qualifier("requestResponseReviewAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> hearingBundleReadyRepNotificationHandler(
        @Qualifier("hearingBundleReadyRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                    && callback.getCaseDetails().getState() != State.FTPA_DECIDED
                    && "DONE".equalsIgnoreCase(getStitchStatus(callback))
                    && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> hearingBundleReadyAipNotificationHandler(
        @Qualifier("hearingBundleReadyAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                        && callback.getCaseDetails().getState() != State.FTPA_DECIDED
                        && "DONE".equalsIgnoreCase(getStitchStatus(callback))
                        && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> hearingBundleFailedNotificationHandler(
        @Qualifier("hearingBundleFailedNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                final String stitchStatus = getStitchStatus(callback);

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                    && callback.getCaseDetails().getState() != State.FTPA_DECIDED
                    && stitchStatus.equalsIgnoreCase("FAILED");
            },
            notificationGenerators
        );
    }

    private String getStitchStatus(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<Bundle>>> maybeCaseBundles = asylumCase.read(AsylumCaseDefinition.CASE_BUNDLES);

        final List<Bundle> caseBundles = maybeCaseBundles.isPresent() ? maybeCaseBundles.get()
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList()) : Collections.emptyList();

        return caseBundles.isEmpty() ? "" : caseBundles.get(0).getStitchStatus().orElse("");
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> stitchingCompleteHomeOfficeNotificationFailedNotificationHandler(
        @Qualifier("asyncStitchingCompleteHomeOfficeNotificationFailedNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                final String instructStatus = asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS, String.class)
                    .orElse("");

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                    && instructStatus.equals("FAIL");
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submittedHearingRequirementsNotificationHandler(
        @Qualifier("submittedHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS
                    && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submittedHearingRequirementsAipNotificationHandler(
        @Qualifier("submittedHearingRequirementsAipNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adjustedHearingRequirementsNotificationHandler(
        @Qualifier("adjustedHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(

            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> withoutHearingRequirementsNotificationHandler(
        @Qualifier("withoutHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAdditionalEvidenceHandler(
        @Qualifier("uploadAdditionalEvidence") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDITIONAL_EVIDENCE
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAdditionalEvidenceHomeOfficeHandler(
        @Qualifier("uploadAdditionalEvidenceHomeOffice") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceCaseOfficerHandler(
        @Qualifier("uploadAddendumEvidenceCaseOfficer") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE
                        && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceCaseOfficerAipHandler(
            @Qualifier("uploadAddendumEvidenceCaseOfficerAip") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
                (callbackStage, callback) ->
                        callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE
                                && isAipJourney(callback.getCaseDetails().getCaseData()),
                notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceLegalRepHandler(
        @Qualifier("uploadAddendumEvidenceLegalRep") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceLegalRepForAipHandler(
        @Qualifier("uploadAddendumEvidenceLegalRepForAip") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAdditionalEvidenceAdminOfficerHandler(
        @Qualifier("uploadAddendumEvidenceAdminOfficer") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
                        && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceAdminOfficerAipHandler(
            @Qualifier("uploadAddendumEvidenceAdminOfficerAip") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
                (callbackStage, callback) ->
                        callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
                                && isAipJourney(callback.getCaseDetails().getCaseData()),
                notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceHomeOfficeHandler(
        @Qualifier("uploadAddendumEvidenceHomeOffice") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE
                        && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceHomeOfficeAipHandler(
            @Qualifier("uploadAddendumEvidenceHomeOfficeAip") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
                (callbackStage, callback) ->
                        callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE
                                && isAipJourney(callback.getCaseDetails().getCaseData()),
                notificationGenerator
        );
    }

    private boolean isValidUserDirection(
        DirectionFinder directionFinder, AsylumCase asylumCase,
        DirectionTag directionTag, Parties parties
    ) {
        return directionFinder
            .findFirst(asylumCase, directionTag)
            .map(direction -> direction.getParties().equals(parties))
            .orElse(false);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> changeToHearingRequirementsNotificationHandler(
        @Qualifier("changeToHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPDATE_HEARING_ADJUSTMENTS,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealExitedOnlineNotificationHandler(
        @Qualifier("appealExitedOnlineNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE
                        && isRepJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealExitedOnlineAppellantNotificationHandler(
        @Qualifier("appealExitedOnlineAppellantNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE
                        && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> changeHearingCentreNotificationHandler(
        @Qualifier("changeHearingCentreNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.CHANGE_HEARING_CENTRE
                        && isRepJourney(asylumCase);
            },
                        notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> changeHearingCentreAppellantNotificationHandler(
        @Qualifier("changeHearingCentreAppellantNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.CHANGE_HEARING_CENTRE
                        && isAipJourney(asylumCase);
            },
                        notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaSubmittedLegalRepNotificationHandler(
        @Qualifier("ftpaSubmittedLegalRepNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        // RIA-3316 - applyForFTPAAppellant
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaAppellantSubmittedHomeOfficeNotificationFailedNotificationHandler(
        @Qualifier("ftpaSubmittedHomeOfficeNotificationFailedCaseOfficerNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                final String instructStatus = asylumCase.read(HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS, String.class)
                    .orElse("");

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT
                    && instructStatus.equals("FAIL")
                    ;
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaRespondentSubmittedHomeOfficeNotificationFailedNotificationHandler(
        @Qualifier("ftpaSubmittedHomeOfficeNotificationFailedCaseOfficerNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                final String instructStatus = asylumCase.read(HOME_OFFICE_FTPA_RESPONDENT_INSTRUCT_STATUS, String.class)
                    .orElse("");

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_RESPONDENT
                    && instructStatus.equals("FAIL");
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaSubmittedLegNotificationHandler(
        @Qualifier("ftpaSubmittedRespondentNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        // RIA-3316 - applyForFTPARespondent
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.APPLY_FOR_FTPA_RESPONDENT,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> makeAnApplicationAipNotificationHandler(
            @Qualifier("makeAnApplicationAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.MAKE_AN_APPLICATION
                            && isAipJourney(asylumCase);
                }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> reviewTimeExtensionGrantedHandler(
        @Qualifier("reviewTimeExtensionGrantedGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                State currentState = callback.getCaseDetails().getState();

                boolean isAipJourney = isAipJourney(asylumCase);

                final Optional<List<IdValue<TimeExtension>>> maybeTimeExtensions =
                    asylumCase.read(AsylumCaseDefinition.TIME_EXTENSIONS);

                final Optional<IdValue<TimeExtension>> maybeTargetTimeExtension = maybeTimeExtensions
                    .orElse(Collections.emptyList()).stream()
                    .filter(timeExtensionIdValue ->
                        currentState == timeExtensionIdValue.getValue().getState()
                        && String.valueOf(maybeTimeExtensions.get().size()).equals(timeExtensionIdValue.getId())
                        && TimeExtensionStatus.GRANTED == timeExtensionIdValue.getValue().getStatus())
                    .findFirst();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REVIEW_TIME_EXTENSION
                       && isAipJourney
                       && maybeTargetTimeExtension.isPresent();
            }, notificationGenerators
        );
    }


    @Bean
    public PreSubmitCallbackHandler<AsylumCase> reviewTimeExtensionRefusedHandler(
        @Qualifier("reviewTimeExtensionRefusedGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                State currentState = callback.getCaseDetails().getState();

                boolean isAipJourney = isAipJourney(asylumCase);

                final Optional<List<IdValue<TimeExtension>>> maybeTimeExtensions =
                    asylumCase.read(AsylumCaseDefinition.TIME_EXTENSIONS);

                final Optional<IdValue<TimeExtension>> maybeTargetTimeExtension = maybeTimeExtensions
                    .orElse(Collections.emptyList()).stream()
                    .filter(timeExtensionIdValue ->
                        currentState == timeExtensionIdValue.getValue().getState()
                        && String.valueOf(maybeTimeExtensions.get().size()).equals(timeExtensionIdValue.getId())
                        && TimeExtensionStatus.REFUSED == timeExtensionIdValue.getValue().getStatus())
                    .findFirst();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REVIEW_TIME_EXTENSION
                       && isAipJourney
                       && maybeTargetTimeExtension.isPresent();
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestClarifyingQuestionsAipNotificationHandler(
        @Qualifier("requestClarifyingQuestionsAipNotificationGenerator")
            List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = isAipJourney(asylumCase);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SEND_DIRECTION_WITH_QUESTIONS
                       && isAipJourney;
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitClarifyingQuestionAnswersNotificationHandler(
        @Qualifier("submitClarifyingQuestionAnswersNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = isAipJourney(asylumCase);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS
                       && isAipJourney;
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> forceCaseProgressionToCaseUnderReviewHandler(
        @Qualifier("forceCaseProgressionToCaseUnderReviewNotificationGenerator")
            List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> forceCaseToSubmitHearingRequirementsNotificationHandler(
        @Qualifier("forceCaseToSubmitHearingRequirementsNotificationGenerator")
            List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adjournHearingWithoutDateHandler(
        @Qualifier("adjournHearingWithoutDateNotificationGenerator")
            List<NotificationGenerator> notificationGenerator) {

        // RIA-3631 adjournHearingWithoutDate
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.ADJOURN_HEARING_WITHOUT_DATE,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestCmaRequirementsAipNotificationHandler(
        @Qualifier("requestCmaRequirementsAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_CMA_REQUIREMENTS
                && callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitCmaRequirementsAipNotificationHandler(
        @Qualifier("submitCmaRequirementsAipNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_CMA_REQUIREMENTS
                && callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> listCmaAipNotificationHandler(
        @Qualifier("listCmaAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.LIST_CMA
                && callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editAppealAfterSubmitNotificationHandler(
        @Qualifier("editAppealAfterSubmitNotificationGenerator") List<NotificationGenerator> notificationGenerator) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionRefusedOrNotAdmittedAppellantNotificationHandler(
        @Qualifier("ftpaApplicationDecisionRefusedOrNotAdmittedAppellantNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        //RIA-3631 leadership/resident judge decision
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString()))
                    .orElse(false);
                if (!isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome) {
                    isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(
                            decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.toString())
                                        || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())
                                        || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                           || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome
                       && !hasThisNotificationSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionGrantedOrPartiallyGrantedAppellantNotificationHandler(
        @Qualifier("ftpaApplicationDecisionGrantedOrPartiallyGrantedAppellantNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        // RIA-3631
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isGrantedOrPartiallyGrantedOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString()))
                    .orElse(false);

                if (!isGrantedOrPartiallyGrantedOutcome) {
                    isGrantedOrPartiallyGrantedOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())
                                         || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                           || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && isGrantedOrPartiallyGrantedOutcome
                       && !hasThisNotificationSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealPaidLegalRepNotificationHandler(
        @Qualifier("submitAppealPaidNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                PaymentStatus paymentStatus = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class).orElse(PAYMENT_PENDING);

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA || type == HU || type == EA || type == EU).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                       && isCorrectAppealType
                       && paymentStatus == PaymentStatus.PAID;
            }, notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionReheardAppellantNotificationHandler(
        @Qualifier("ftpaApplicationDecisionReheardAppellantNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 reheard FTPA application (resident Judge)
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isReheardDecisionOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))
                    .orElse(false);

                if (!isReheardDecisionOutcome) {
                    isReheardDecisionOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                                         || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                       && isReheardDecisionOutcome
                       && !hasThisNotificationSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealLegalRepPayLaterNotificationHandler(
        @Qualifier("submitAppealLegalRepPayLaterNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                String paAppealTypePaymentOption = asylumCase
                    .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && (isPaAppealType
                           && paAppealTypePaymentOption.equals("payLater"));
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealLegalRepNotificationHandler(
        @Qualifier("submitAppealLegalRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRpAndDcAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == RP || type == DC).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isRpAndDcAppealType;
            },
            notificationGenerators,
            getErrorHandler()
        );
    }


    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationHandler(
        @Qualifier("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        // RIA-3631
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString()))
                    .orElse(false);

                if (!isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome) {
                    isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(
                            decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.toString())
                                        || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())
                                        || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REMADE32.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                           || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome
                       && !hasThisNotificationSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentNotificationHandler(
        @Qualifier("ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        // RIA-3631
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isGrantedOrPartiallyGrantedOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString()))
                    .orElse(false);

                if (!isGrantedOrPartiallyGrantedOutcome) {
                    isGrantedOrPartiallyGrantedOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())
                                         || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                           || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && isGrantedOrPartiallyGrantedOutcome
                       && !hasThisNotificationSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaDecisionHomeOfficeNotificationFailedNotificationHandler(
        @Qualifier("ftpaDecisionHomeOfficeNotificationFailedNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                String ftpaApplicantType = asylumCase
                    .read(FTPA_APPLICANT_TYPE, String.class)
                    .orElse("");

                final String hoRequestEvidenceInstructStatus =
                    ftpaApplicantType.equals("appellant")
                        ? asylumCase.read(HOME_OFFICE_FTPA_APPELLANT_DECIDED_INSTRUCT_STATUS,
                        String.class).orElse("")
                        : asylumCase.read(HOME_OFFICE_FTPA_RESPONDENT_DECIDED_INSTRUCT_STATUS,
                        String.class).orElse("");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                           || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                       && hoRequestEvidenceInstructStatus.equals("FAIL");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionReheardRespondentNotificationHandler(
        @Qualifier("ftpaApplicationDecisionReheardRespondentNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        //RIA-3631 - ftpsResidentJudgeDecision
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isReheardDecisionOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                                     || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))
                    .orElse(false);
                if (!isReheardDecisionOutcome) {
                    isReheardDecisionOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString())
                                         || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                       && isReheardDecisionOutcome
                       && !hasThisNotificationSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT");
            },
            notificationGenerators
        );
    }

    private boolean hasThisNotificationSentBefore(AsylumCase asylumCase, Callback callback,
                                                  String notificationReference) {
        Optional<List<IdValue<String>>> maybeNotificationSent =
            asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent =
            maybeNotificationSent
                .orElseGet(ArrayList::new);

        return notificationsSent
                   .stream()
                   .filter(
                       notification -> notification.getId().equals(callback.getCaseDetails().getId() + notificationReference))
                   .count() > 0 ? true : false;
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealPayOfflineNotificationHandler(
        @Qualifier("submitAppealPayOfflineNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                String paymentOption = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isCorrectAppealType
                       && (paymentOption.equals("payOffline") || isRemissionOptedForEaOrHuOrPaAppeal(callback));
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealPendingPaymentNotificationHandler(
        @Qualifier("submitAppealPendingPaymentNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - submitAppeal This needs to be changed as per ACs
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SUBMIT_APPEAL
                && (isPaymentPendingForEaOrHuAppeal(callback)
                    || isPaymentPendingForEaOrHuAppealWithRemission(callback)),
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> paymentPendingPaidLegalRepNotificationHandler(
        @Qualifier("paymentPendingPaidLegalRepNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                State currentState = callback.getCaseDetails().getState();

                boolean isCorrectAppealTypePA = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                boolean isCorrectAppealTypeAndStateHUorEA =
                    isEaHuEuAppeal(asylumCase)
                    && (currentState == State.APPEAL_SUBMITTED);

                Optional<PaymentStatus> paymentStatus = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.MARK_APPEAL_PAID
                       && (isCorrectAppealTypePA || isCorrectAppealTypeAndStateHUorEA)
                       && !paymentStatus.equals(Optional.empty())
                       && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> paymentPendingPaidCaseOfficerNotificationHandler(
        @Qualifier("paymentPendingPaidCaseOfficerNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                State currentState = callback.getCaseDetails().getState();

                boolean isCorrectAppealTypeAndStateHUorEA =
                    isEaHuEuAppeal(asylumCase)
                    && (currentState == State.APPEAL_SUBMITTED);

                Optional<PaymentStatus> paymentStatus = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.MARK_APPEAL_PAID
                       && isCorrectAppealTypeAndStateHUorEA
                       && !paymentStatus.equals(Optional.empty())
                       && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> remissionDecisionApprovedNotificationHandler(
        @Qualifier("remissionDecisionApprovedNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == EA || type == HU || type == EU || type == PA).orElse(false);

                boolean isApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                       && isCorrectAppealType
                       && isApproved;

            }, notificationGenerators
        );
    }


    private boolean isPaymentPendingForEaOrHuAppeal(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String eaHuAppealTypePaymentOption = asylumCase
            .read(AsylumCaseDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        State asylumCaseState = callback.getCaseDetails().getState();
        RemissionType remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);
        boolean isEaAndHuAppealType = isEaHuEuAppeal(asylumCase);
        if (Arrays.asList(
            HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType)) {
            return asylumCaseState == State.PENDING_PAYMENT
                   && isEaAndHuAppealType;
        }
        return asylumCaseState == State.PENDING_PAYMENT
               && isEaAndHuAppealType
               && eaHuAppealTypePaymentOption.equals("payOffline");
    }

    private boolean isPaymentPendingForEaOrHuAppealWithRemission(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo remissionEnabledOption = asylumCase
            .read(IS_REMISSIONS_ENABLED, YesOrNo.class).orElse(NO);

        RemissionType remissionType = asylumCase
            .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);
        boolean isRemissionTypeValid = Arrays.asList(
            HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType);

        State asylumCaseState = callback.getCaseDetails().getState();
        return asylumCaseState == State.PENDING_PAYMENT
               && isEaHuEuAppeal(asylumCase)
               && remissionEnabledOption.equals(YES)
               && isRemissionTypeValid;
    }

    private boolean isRemissionOptedForEaOrHuOrPaAppeal(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean isEaHuPaAppealType = asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU || type == EU || type == PA).orElse(false);

        YesOrNo remissionEnabledOption = asylumCase
            .read(IS_REMISSIONS_ENABLED, YesOrNo.class).orElse(NO);

        RemissionType remissionType = asylumCase
            .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);
        boolean isRemissionTypeValid = Arrays.asList(
            HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType);

        return isEaHuPaAppealType
               && remissionEnabledOption.equals(YES)
               && isRemissionTypeValid;
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> reinstateAppealNotificationHandler(
        @Qualifier("reinstateAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                 callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                         && callback.getEvent() == Event.REINSTATE_APPEAL
                         && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> reinstateAppealAipNotificationHandler(
            @Qualifier("reinstateAppealAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) ->
                        callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                && callback.getEvent() == Event.REINSTATE_APPEAL
                                && isAipJourney(callback.getCaseDetails().getCaseData()),
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> makeAnApplicationNotificationHandler(
            @Qualifier("makeAnApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) -> {

                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.MAKE_AN_APPLICATION
                            && isRepJourney(asylumCase);
                }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideAnApplicationHomeOfficeNotificationHandler(
        @Qualifier("decideAnApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.DECIDE_AN_APPLICATION
                && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideAnApplicationAipNotificationHandler(
        @Qualifier("decideAnApplicationAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.DECIDE_AN_APPLICATION
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> remissionDecisionPartiallyApprovedNotificationHandler(
        @Qualifier("remissionDecisionPartiallyApprovedNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isPartiallyApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> PARTIALLY_APPROVED == decision)
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                       && isPartiallyApproved;
            },
            notificationGenerators
        );
    }


    @Bean
    public PreSubmitCallbackHandler<AsylumCase> remissionDecisionRejectedNotificationHandler(
        @Qualifier("remissionDecisionRejectedNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isRejected = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> REJECTED == decision)
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                       && isRejected;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> manageFeeUpdateRefundInstructedNotificationHandler(
        @Qualifier("manageFeeUpdateRefundInstructedNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                Optional<List<String>> completedStages = asylumCase.read(FEE_UPDATE_COMPLETED_STAGES);
                boolean isRefundInstructed = completedStages.isPresent()
                                             && completedStages.get().get(completedStages.get().size() - 1)
                                                 .equals("feeUpdateRefundInstructed");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.MANAGE_FEE_UPDATE
                       && isRefundInstructed;
            },
            notificationGenerators
        );

    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> nocRequestDecisionLrNotificationHandler(
        @Qualifier("nocRequestDecisionLrNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && callback.getEvent() == Event.NOC_REQUEST
                && hasRepEmail(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> nocRequestDecisionHomeOfficeNotificationHandler(
        @Qualifier("nocRequestDecisionHomeOfficeNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && callback.getEvent() == Event.NOC_REQUEST,
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentationNotificationHandler(
        @Qualifier("removeRepresentationNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && (callback.getEvent() == Event.REMOVE_REPRESENTATION
                           || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE);
            },
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentationAppellantEmailNotificationHandler(
        @Qualifier("removeRepresentationAppellantEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.REMOVE_REPRESENTATION
                       && callback.getCaseDetails().getCaseData().read(EMAIL, String.class).isPresent();
            },
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentationAppellantSmsNotificationHandler(
        @Qualifier("removeRepresentationAppellantSmsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.REMOVE_REPRESENTATION
                       && callback.getCaseDetails().getCaseData().read(MOBILE_NUMBER, String.class).isPresent();
            },
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentativeAppellantEmailNotificationHandler(
        @Qualifier("removeRepresentativeAppellantEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE
                       && callback.getCaseDetails().getCaseData().read(EMAIL, String.class).isPresent();
            },
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentativeAppellantSmsNotificationHandler(
        @Qualifier("removeRepresentativeAppellantSmsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE
                       && callback.getCaseDetails().getCaseData().read(MOBILE_NUMBER, String.class).isPresent();
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestFeeRemissionNotificationHandler(
        @Qualifier("requestFeeRemissionNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REQUEST_FEE_REMISSION;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> manageFeeUpdateCaseOfficerNotificationHandler(
        @Qualifier("caseOfficerManageFeeUpdateGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isEaAndHuAppealType = isEaHuEuAppeal(asylumCase);

                boolean isPaAppealType = isPaAppeal(asylumCase);

                String eaHuAppealTypePaymentOption = asylumCase
                    .read(AsylumCaseDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");
                String paAppealTypePaymentOption = asylumCase
                    .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

                Optional<PaymentStatus> paymentStatus = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class);

                boolean maybeFeeUpdateRecorded = asylumCase
                    .read(FEE_UPDATE_RECORDED, CheckValues.class)
                    .map(value -> value.getValues().contains("feeUpdateRecorded")).orElse(false);

                boolean isPaidByCard = ((isEaAndHuAppealType && eaHuAppealTypePaymentOption.equals("payOffline"))
                                        || (isPaAppealType && paAppealTypePaymentOption.equals("payOffline")));

                boolean isPaidByAccount = ((isEaAndHuAppealType && eaHuAppealTypePaymentOption.equals("payNow"))
                                           || (isPaAppealType && Arrays.asList("payNow", "payLater").contains(paAppealTypePaymentOption)));

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.MANAGE_FEE_UPDATE
                       && (isPaidByCard || isPaidByAccount)
                       && maybeFeeUpdateRecorded
                       && !paymentStatus.equals(Optional.empty())
                       && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
    }

    protected boolean isPaAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == PA).orElse(false);
    }

    protected boolean isEaHuEuAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU || type == EU).orElse(false);
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> nocRequestDecisionAppellantSmsNotificationHandler(
        @Qualifier("nocRequestDecisionAppellantSmsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean smsPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_SMS == contactPreference)
                    .orElse(false);
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.NOC_REQUEST
                       && smsPreferred
                       && asylumCase.read(MOBILE_NUMBER, String.class).isPresent();
            },
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> nocRequestDecisionAppellantEmailNotificationHandler(
        @Qualifier("nocRequestDecisionAppellantEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean emailPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_EMAIL == contactPreference)
                    .orElse(false);
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.NOC_REQUEST
                       && emailPreferred
                       && asylumCase.read(EMAIL, String.class).isPresent();
            },
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> aipNocRequestDecisionAppellantNotificationHandler(
        @Qualifier("aipNocRequestDecisionAppellantNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                    && callback.getEvent() == Event.NOC_REQUEST
                    && (isSmsPreferred(asylumCase) || isEmailPreferred(asylumCase));
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealAppellantSmsNotificationHandler(
        @Qualifier("submitAppealAppellantSmsNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean smsPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_SMS == contactPreference)
                    .orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean payNow = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payNow"))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && asylumCase.read(MOBILE_NUMBER, String.class).isPresent()
                       && (payLater || payNow)
                       && smsPreferred;
            },
            notificationGenerators,
            (callback, e) -> log.error(
                "cannot send sms notification to the appellant on submitAppeal, caseId: {}",
                callback.getCaseDetails().getId(),
                e
            )
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updatePaymentStatusAppellantSmsNotificationHandler(
        @Qualifier("submitAppealAppellantSmsNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean smsPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_SMS == contactPreference)
                    .orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean payNow = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payNow"))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.UPDATE_PAYMENT_STATUS
                       && asylumCase.read(MOBILE_NUMBER, String.class).isPresent()
                       && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                       && !payLater
                       && !payNow
                       && smsPreferred;
            },
            notificationGenerators,
            getSmsErrorHandling()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealAppellantEmailNotificationHandler(
        @Qualifier("submitAppealAppellantEmailNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean emailPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_EMAIL == contactPreference)
                    .orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean payNow = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payNow"))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && asylumCase.read(EMAIL, String.class).isPresent()
                       && emailPreferred
                       && (payLater || payNow);
            },
            notificationGenerators,
            (callback, e) -> log.error(
                "cannot send email notification to the appellant on submitAppeal, caseId: {}",
                callback.getCaseDetails().getId(),
                e
            )
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updatePaymentStatusAppellantEmailNotificationHandler(
        @Qualifier("submitAppealAppellantEmailNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean emailPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_EMAIL == contactPreference)
                    .orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean payNow = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payNow"))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.UPDATE_PAYMENT_STATUS
                       && asylumCase.read(EMAIL, String.class).isPresent()
                       && emailPreferred
                       && !payLater
                       && !payNow
                       && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent();
            },
            notificationGenerators,
            (callback, e) -> log.error(
                "cannot send email notification to the appellant on submitAppeal, caseId: {}",
                callback.getCaseDetails().getId(),
                e
            )
        );
    }


    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCanProceedEmailNotificationHandler(
        @Qualifier("recordOfTimeDecisionCanProceedEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                OutOfTimeDecisionType outOfTimeDecisionType =
                    asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                        .orElse(UNKNOWN);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                       && Arrays.asList(IN_TIME, OutOfTimeDecisionType.APPROVED)
                           .contains(outOfTimeDecisionType)
                       && isRepJourney(asylumCase);

            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCannotProceedEmailNotificationHandler(
        @Qualifier("recordOfTimeDecisionCannotProceedEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                OutOfTimeDecisionType outOfTimeDecisionType =
                    asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                        .orElse(UNKNOWN);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                       && outOfTimeDecisionType == OutOfTimeDecisionType.REJECTED
                        && isRepJourney(asylumCase);

            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payAndSubmitAppealEmailNotificationHandler(
        @Qualifier("payAndSubmitAppealEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean paymentFailed = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == FAILED || paymentStatus == TIMEOUT).orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean paymentFailedChangedToPayLater = paymentFailed && payLater;

                boolean isRemissionApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                return (callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                        && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                        && callback.getCaseDetails().getCaseData()
                            .read(JOURNEY_TYPE, JourneyType.class)
                            .map(type -> type == REP).orElse(true)
                        && (!paymentFailed || paymentFailedChangedToPayLater)
                        && !isPaymentPendingForEaOrHuAppeal(callback))
                       || (callback.getEvent() == Event.RECORD_REMISSION_DECISION
                           && isRemissionApproved
                           && isEaHuEuAppeal(asylumCase));
            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payAndSubmitAppealFailedEmailNotificationHandler(
        @Qualifier("payAndSubmitAppealFailedEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators
    ) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean paymentFailed = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == FAILED || paymentStatus == TIMEOUT).orElse(false);

                boolean isPaAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                       && paymentFailed
                       && isPaAppealType;
            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payAndSubmitAppealAppellantEmailNotificationHandler(
        @Qualifier("payAndSubmitAppealAppellantEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators
    ) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean emailPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_EMAIL == contactPreference)
                    .orElse(false);

                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                       && asylumCase.read(EMAIL, String.class).isPresent()
                       && emailPreferred;
            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payAndSubmitAppealAppellantSmsNotificationHandler(
        @Qualifier("payAndSubmitAppealAppellantSmsNotificationGenerator")
            List<NotificationGenerator> notificationGenerators
    ) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean smsPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_SMS == contactPreference)
                    .orElse(false);

                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                       && asylumCase.read(MOBILE_NUMBER, String.class).isPresent()
                       && smsPreferred;
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editPaymentMethodNotificationHandler(
            @Qualifier("editPaymentMethodNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {

                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    final State state = callback.getCaseDetails().getState();

                    return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.EDIT_PAYMENT_METHOD
                            && state != State.APPEAL_STARTED
                            && isEaHuEuAppeal(asylumCase)
                            && !isRemissionRejectedAndPaymentChangedToCard(asylumCase)
                        );
                }, notificationGenerators
        );
    }

    private boolean isRemissionRejectedAndPaymentChangedToCard(AsylumCase asylumCase) {

        Optional<RemissionDecision> optionalRemissionDecision =
                asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        return optionalRemissionDecision.isPresent() && optionalRemissionDecision.get() == REJECTED;
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editPaymentMethodAoNotificationHandler(
            @Qualifier("editPaymentMethodAoNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
                (callbackStage, callback) -> {

                    final State state = callback.getCaseDetails().getState();

                    return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.EDIT_PAYMENT_METHOD
                            && state != State.APPEAL_STARTED);
                }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payForAppealAppealEmailNotificationHandler(
        @Qualifier("payForAppealEmailNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean paymentFailed = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == FAILED || paymentStatus == TIMEOUT).orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean paymentFailedChangedToPayLater = paymentFailed && payLater;

                boolean isRemissionApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                return (callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                        && callback.getEvent() == Event.PAY_FOR_APPEAL
                        && callback.getCaseDetails().getCaseData()
                            .read(JOURNEY_TYPE, JourneyType.class)
                            .map(type -> type == REP).orElse(true)
                        && (!paymentFailed || paymentFailedChangedToPayLater)
                        && !isPaymentPendingForEaOrHuAppeal(callback))
                       || (callback.getEvent() == Event.RECORD_REMISSION_DECISION
                           && isRemissionApproved
                           && isEaHuEuAppeal(asylumCase));
            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> paymentPaidPostSubmitLegalRepNotificationHandler(
        @Qualifier("paymentPaidPostSubmitNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                State currentState = callback.getCaseDetails().getState();

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                boolean isCorrectAppealTypeAndState =
                    isCorrectAppealType
                    && (currentState != State.APPEAL_STARTED
                        || currentState != State.APPEAL_SUBMITTED
                    );

                Optional<PaymentStatus> paymentStatus = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class);

                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.PAYMENT_APPEAL
                       && isCorrectAppealTypeAndState
                       && !paymentStatus.equals(Optional.empty())
                       && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCannotProceedAppellantEmailNotificationHandler(
            @Qualifier("recordOfTimeDecisionCannotProceedAppellantEmailNotificationGenerator")
                    List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    OutOfTimeDecisionType outOfTimeDecisionType =
                            asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                                    .orElse(UNKNOWN);

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                            && outOfTimeDecisionType == OutOfTimeDecisionType.REJECTED
                            && isAipJourney(asylumCase)
                            && isEmailPreferred(asylumCase);

                }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCannotProceedAppellantSmsNotificationHandler(
            @Qualifier("recordOfTimeDecisionCannotProceedAppellantSmsNotificationGenerator")
                    List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    OutOfTimeDecisionType outOfTimeDecisionType =
                            asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                                    .orElse(UNKNOWN);

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                            && outOfTimeDecisionType == OutOfTimeDecisionType.REJECTED
                            && isAipJourney(asylumCase)
                            && isSmsPreferred(asylumCase);

                }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCanProceedAppellantEmailNotificationHandler(
            @Qualifier("recordOfTimeDecisionCanProceedAppellantEmailNotificationGenerator")
                    List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    OutOfTimeDecisionType outOfTimeDecisionType =
                            asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                                    .orElse(UNKNOWN);

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                            && Arrays.asList(IN_TIME, OutOfTimeDecisionType.APPROVED)
                                .contains(outOfTimeDecisionType)
                            && isAipJourney(asylumCase)
                            && isEmailPreferred(asylumCase);

                }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCanProceedAppellantSmsNotificationHandler(
            @Qualifier("recordOfTimeDecisionCanProceedAppellantSmsNotificationGenerator")
                    List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
                (callbackStage, callback) -> {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    OutOfTimeDecisionType outOfTimeDecisionType =
                            asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                                    .orElse(UNKNOWN);

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                            && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                            && Arrays.asList(IN_TIME, OutOfTimeDecisionType.APPROVED)
                                .contains(outOfTimeDecisionType)
                            && isAipJourney(asylumCase)
                            && isSmsPreferred(asylumCase);

                }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> upperTribinalBundleFailedNotificationHandler(
        @Qualifier("upperTribunalBundleFailedNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                final String stitchStatus = getStitchStatus(callback);

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                    && callback.getCaseDetails().getState() == State.FTPA_DECIDED
                    && stitchStatus.equalsIgnoreCase("FAILED");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealDecidedOrEndedPendingPaymentNotificationHandler(
            @Qualifier("appealDecidedOrEndedPendingPaymentGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-4827 - Ctsc notification of Pending payment on appeal decided or ended.
        return new NotificationHandler(
                (callbackStage, callback) -> {

                    final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                    boolean isAipCorrectAppealType = asylumCase
                        .read(APPEAL_TYPE, AppealType.class)
                        .map(type -> type == PA).orElse(false);

                    boolean isAipPaymentUnpaid =
                        asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
                            .map(status -> status != PAID).orElse(false)
                        || asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).isEmpty();

                    boolean isAipAppealUnpaid =
                        (isAipJourney(asylumCase) && isAipCorrectAppealType && isAipPaymentUnpaid);

                    boolean isPaymentPending =
                            asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
                                    .map(status -> status == PAYMENT_PENDING).orElse(false);

                    return
                            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                    && Arrays.asList(
                                            Event.SEND_DECISION_AND_REASONS,
                                            Event.END_APPEAL).contains(callback.getEvent())
                                    && (isPaymentPending || isAipAppealUnpaid);
                },
                notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealEndedAutomaticallyNotificationHandler(
        @Qualifier("appealEndedAutomaticallyNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                 && Objects.equals(Event.END_APPEAL_AUTOMATICALLY, callback.getEvent())
                                 && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppealEndedAutomaticallyNotificationHandler(
        @Qualifier("aipAppealEndedAutomaticallyNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Objects.equals(Event.END_APPEAL_AUTOMATICALLY, callback.getEvent())
                && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    private boolean isRepJourney(AsylumCase asylumCase) {

        return asylumCase
                .read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> type == REP).orElse(true);
    }

    private boolean isAipJourney(AsylumCase asylumCase) {

        return asylumCase
                .read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> type == AIP).orElse(false);
    }

    private boolean hasRepEmail(AsylumCase asylumCase) {
        return asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).isPresent();
    }

    private boolean isSmsPreferred(AsylumCase asylumCase) {

        final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

        Set<IdValue<Subscriber>> smsPreferred = maybeSubscribers
                .orElse(Collections.emptyList()).stream()
                .filter(subscriber -> YES.equals(subscriber.getValue().getWantsSms()))
                .collect(Collectors.toSet());

        return smsPreferred.size() > 0 && smsPreferred.stream().findFirst().map(subscriberIdValue ->
                subscriberIdValue.getValue().getMobileNumber()).isPresent();
    }

    private boolean isEmailPreferred(AsylumCase asylumCase) {

        final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

        Set<IdValue<Subscriber>> smsPreferred = maybeSubscribers
                .orElse(Collections.emptyList()).stream()
                .filter(subscriber -> YES.equals(subscriber.getValue().getWantsEmail()))
                .collect(Collectors.toSet());

        return smsPreferred.size() > 0 && smsPreferred.stream().findFirst().map(subscriberIdValue ->
                subscriberIdValue.getValue().getEmail()).isPresent();
    }

    private ErrorHandler<AsylumCase> getErrorHandler() {
        ErrorHandler<AsylumCase> errorHandler = (callback, e) -> {
            callback
                .getCaseDetails()
                .getCaseData()
                .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
        };
        return errorHandler;
    }

    private ErrorHandler<AsylumCase> getSmsErrorHandling() {
        ErrorHandler<AsylumCase> errorHandler =
            (callback, e) -> log.error(
                "cannot send sms notification to the appellant on submitAppeal, caseId: {}",
                callback.getCaseDetails().getId(),
                e
            );
        return errorHandler;
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updatePaymentStatusSuccessLrHoNotificationHandler(
        @Qualifier("updatePaymentStatusPaidAppealSubmittedLrHoGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                State currentState = callback.getCaseDetails().getState();

                boolean isCorrectAppealTypeAndStateHUorEAorPA =
                    isEaHuEuAppeal(asylumCase)
                    && (currentState == State.APPEAL_SUBMITTED);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                Optional<PaymentStatus> paymentStatus = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.UPDATE_PAYMENT_STATUS
                       && isCorrectAppealTypeAndStateHUorEAorPA
                       && !paymentStatus.equals(Optional.empty())
                       && !payLater
                       && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                       && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealLrHoWaysToPayPaPayNowNotificationHandler(
        @Qualifier("submitAppealLrHoWaysToPayPaPayNowNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                String paAppealTypePaymentOption = asylumCase
                    .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                       && (isPaAppealType
                           && paAppealTypePaymentOption.equals("payNow"));
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

}


