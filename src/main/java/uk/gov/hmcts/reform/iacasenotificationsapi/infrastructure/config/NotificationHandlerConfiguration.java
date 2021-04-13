package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.DC;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.RP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.BUILD_CASE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                         && Event.UNLINK_APPEAL.equals(callback.getEvent()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> linkAppealNotificationHandler(
        @Qualifier("linkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                         && Event.LINK_APPEAL.equals(callback.getEvent()),
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
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.END_APPEAL,
            notificationGenerators

        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appealOutcomeNotificationHandler(
        @Qualifier("appealOutcomeNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3361 - sendDecisionAndReasons
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.SEND_DECISION_AND_REASONS,
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
                       && !isOneOfHomeOfficeApiNotifications(callback);
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
                       && isOneOfHomeOfficeApiNotifications(callback);
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
                       && isLegalRepresentative;
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
                       && isRespondent;
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
                && callback.getEvent() == Event.LIST_CASE,
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
                && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
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

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

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

                boolean isRepJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == REP).orElse(true);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE
                       && isRepJourney;
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
                && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW,
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

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

                boolean isAppealOnTime = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == NO).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isAipJourney
                       && isAppealOnTime
                       && !isPaymentPendingForEaOrHuAppeal(callback);
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
                    .map(paymentStatus -> paymentStatus == FAILED).orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean paymentFailedChangedToPayLater = paymentFailed && payLater;

                boolean isRemissionApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                boolean isEaAndHuAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == EA || type == HU).orElse(false);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        &&
                        (callback.getEvent() == Event.SUBMIT_APPEAL || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL)
                        && callback.getCaseDetails().getCaseData()
                            .read(JOURNEY_TYPE, JourneyType.class)
                            .map(type -> type == REP).orElse(true)
                        && (!paymentFailed || paymentFailedChangedToPayLater)
                        && !isPaymentPendingForEaOrHuAppeal(callback))
                       || (callback.getEvent() == Event.RECORD_REMISSION_DECISION
                           && isRemissionApproved
                           && isEaAndHuAppealType);
            }, notificationGenerators,
            (callback, e) -> {
                callback
                    .getCaseDetails()
                    .getCaseData()
                    .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
            }
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

                boolean paymentFailed = asylumCase
                    .read(AsylumCaseDefinition.PAYMENT_STATUS, PaymentStatus.class)
                    .map(paymentStatus -> paymentStatus == FAILED).orElse(false);

                boolean payLater = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .map(paymentOption -> paymentOption.equals("payOffline") || paymentOption.equals("payLater"))
                    .orElse(false);

                boolean paymentFailedChangedToPayLater = paymentFailed && payLater;

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       &&
                       (callback.getEvent() == Event.SUBMIT_APPEAL || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL)
                       && (!paymentFailed || paymentFailedChangedToPayLater)
                       && !isPaymentPendingForEaOrHuAppeal(callback);
            }, notificationGenerators,
            (callback, e) -> {
                callback
                    .getCaseDetails()
                    .getCaseData()
                    .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
            }
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealOutOfTimeAipNotificationHandler(
        @Qualifier("submitAppealOutOfTimeAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

                boolean isOutOfTimeAppeal = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == YES).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_APPEAL
                       && isAipJourney
                       && isOutOfTimeAppeal
                       && !isPaymentPendingForEaOrHuAppeal(callback);
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
                && callback.getEvent() == Event.REQUEST_RESPONSE_AMEND,
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
                       && callback.getCaseDetails().getState() == State.AWAITING_RESPONDENT_EVIDENCE;
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
    public PreSubmitCallbackHandler<AsylumCase> editCaseListingNotificationHandler(
        @Qualifier("editCaseListingNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - editCaseListing
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_CASE_LISTING,
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
                && callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> hearingBundleReadyNotificationHandler(
        @Qualifier("hearingBundleReadyNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3316 - generateHearingBundle, customiseHearingBundle
        return new NotificationHandler(
            (callbackStage, callback) -> {

                final String stitchStatus = getStitchStatus(callback);

                return
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                    && stitchStatus.equalsIgnoreCase("DONE");
            },
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
                && callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS,
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
                && callback.getEvent() == Event.UPLOAD_ADDITIONAL_EVIDENCE,
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
                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceLegalRepHandler(
        @Qualifier("uploadAddendumEvidenceLegalRep") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAdditionalEvidenceAdminOfficerHandler(
        @Qualifier("uploadAddendumEvidenceAdminOfficer") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceHomeOfficeHandler(
        @Qualifier("uploadAddendumEvidenceHomeOffice") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
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
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REMOVE_APPEAL_FROM_ONLINE,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> changeHearingCentreNotificationHandler(
        @Qualifier("changeHearingCentreNotificationGenerator") List<NotificationGenerator> notificationGenerator) {
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.CHANGE_HEARING_CENTRE,
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
    public PreSubmitCallbackHandler<AsylumCase> submitTimeExtensionAipNotificationHandler(
        @Qualifier("submitTimeExtensionAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.SUBMIT_TIME_EXTENSION
                       && isAipJourney;
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

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

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

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

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

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

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

                boolean isAipJourney = asylumCase
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == AIP).orElse(false);

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
                && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT,
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
                    .map(type -> type == PA || type == HU || type == EA).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL
                       && isCorrectAppealType
                       && paymentStatus == PaymentStatus.PAID;
            }, notificationGenerators,
            (callback, e) -> {
                callback
                    .getCaseDetails()
                    .getCaseData()
                    .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
            }
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
            (callback, e) -> {
                callback
                    .getCaseDetails()
                    .getCaseData()
                    .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
            }
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
            (callback, e) -> {
                callback
                    .getCaseDetails()
                    .getCaseData()
                    .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
            }
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
    public PreSubmitCallbackHandler<AsylumCase> paymentPaidLegalRepNotificationHandler(
        @Qualifier("paymentPaidNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
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

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && callback.getEvent() == Event.PAYMENT_APPEAL
                       && isCorrectAppealTypeAndState
                       && !paymentStatus.equals(Optional.empty())
                       && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
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
            (callback, e) -> {
                callback
                    .getCaseDetails()
                    .getCaseData()
                    .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
            }
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

                boolean isCorrectAppealTypeEaHu = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == EA || type == HU).orElse(false);

                boolean isCorrectAppealTypeAndStateHUorEA =
                    isCorrectAppealTypeEaHu
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

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == EA || type == HU).orElse(false);

                boolean isCorrectAppealTypeAndStateHUorEA =
                    isCorrectAppealType
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
                    .map(type -> type == EA || type == HU || type == PA).orElse(false);

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

        boolean isEaAndHuAppealType = asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU).orElse(false);

        String eaHuAppealTypePaymentOption = asylumCase
            .read(AsylumCaseDefinition.EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

        State asylumCaseState = callback.getCaseDetails().getState();
        RemissionType remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);
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

        boolean isEaAndHuAppealType = asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU).orElse(false);

        YesOrNo remissionEnabledOption = asylumCase
            .read(IS_REMISSIONS_ENABLED, YesOrNo.class).orElse(NO);

        RemissionType remissionType = asylumCase
            .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);
        boolean isRemissionTypeValid = Arrays.asList(
            HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType);

        State asylumCaseState = callback.getCaseDetails().getState();
        return asylumCaseState == State.PENDING_PAYMENT
               && isEaAndHuAppealType
               && remissionEnabledOption.equals(YES)
               && isRemissionTypeValid;
    }

    private boolean isRemissionOptedForEaOrHuOrPaAppeal(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean isEaHuPaAppealType = asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU || type == PA).orElse(false);

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
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                                         && callback.getEvent() == Event.REINSTATE_APPEAL,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> makeAnApplicationNotificationHandler(
        @Qualifier("makeAnApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.MAKE_AN_APPLICATION,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideAnApplicationHomeOfficeNotificationHandler(
        @Qualifier("decideAnApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.DECIDE_AN_APPLICATION,
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
    public PostSubmitCallbackHandler<AsylumCase> nocRequestDecisionNotificationHandler(
        @Qualifier("nocRequestDecisionNotificationGenerator")
            List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> {
                return callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                       && callback.getEvent() == Event.NOC_REQUEST;

            },
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

                boolean isEaAndHuAppealType = isEaAndHuAppeal(asylumCase);

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

    protected boolean isEaAndHuAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == EA || type == HU).orElse(false);
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
    public PreSubmitCallbackHandler<AsylumCase> submitAppealAppellantSmsNotificationHandler(
        @Qualifier("submitAppealAppellantSmsNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean smsPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_SMS == contactPreference)
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.SUBMIT_APPEAL
                           || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL)
                       && asylumCase.read(MOBILE_NUMBER, String.class).isPresent()
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
    public PreSubmitCallbackHandler<AsylumCase> submitAppealAppellantEmailNotificationHandler(
        @Qualifier("submitAppealAppellantEmailNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean emailPreferred = asylumCase.read(CONTACT_PREFERENCE, ContactPreference.class)
                    .map(contactPreference -> ContactPreference.WANTS_EMAIL == contactPreference)
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                       && (callback.getEvent() == Event.SUBMIT_APPEAL
                           || callback.getEvent() == Event.PAY_AND_SUBMIT_APPEAL)
                       && asylumCase.read(EMAIL, String.class).isPresent()
                       && emailPreferred;
            },
            notificationGenerators,
            (callback, e) -> log.error(
                "cannot send email notification to the appellant on submitAppeal, caseId: {}",
                callback.getCaseDetails().getId(),
                e
            )
        );
    }

}


