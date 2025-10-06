package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.DC;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType.RP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType.APPELLANT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType.RESPONDENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FeeTribunalAction.ADDITIONAL_PAYMENT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_REFUSED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.OutOfTimeDecisionType.IN_TIME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.OutOfTimeDecisionType.UNKNOWN;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole.getAdminOfficerRoles;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.FAILED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.PAID;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.PAYMENT_PENDING;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.PaymentStatus.TIMEOUT;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ApplicationDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FeeTribunalAction;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.UserRole;
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
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils;

@Slf4j
@Configuration
public class NotificationHandlerConfiguration {
    private static final String RESPONDENT_APPLICANT = "Respondent";
    private static final String IS_APPELLANT = "The appellant";

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> forceCaseProgressionNotificationHandler(
        @Qualifier("forceCaseProgressionNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> function = (callbackStage, callback) ->
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.FORCE_REQUEST_CASE_BUILDING
                && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData());
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
    public PreSubmitCallbackHandler<AsylumCase> caseLinkAppealNotificationHandler(
        @Qualifier("caseLinkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Event.CREATE_CASE_LINK.equals(callback.getEvent())
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalMaintainCaseLinkAppealNotificationHandler(
        @Qualifier("internalMaintainCaseLinkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Event.CREATE_CASE_LINK.equals(callback.getEvent())
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> caseUnlinkAppealNotificationHandler(
        @Qualifier("caseUnlinkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Event.MAINTAIN_CASE_LINKS.equals(callback.getEvent())
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalMaintainCaseUnlinkAppealNotificationHandler(
        @Qualifier("internalMaintainCaseUnlinkAppealNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Event.MAINTAIN_CASE_LINKS.equals(callback.getEvent())
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
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
            (callbackStage, callback) -> {
                YesOrNo isIntegrated = callback
                    .getCaseDetails()
                    .getCaseData()
                    .read(IS_INTEGRATED, YesOrNo.class)
                    .orElse(YesOrNo.NO);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RESTORE_STATE_FROM_ADJOURN
                    && isIntegrated.equals(NO);
            },
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
            notificationGenerators

        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> endAppealInternalNotificationHandler(
        @Qualifier("endAppealInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.END_APPEAL
                    && isRepJourney(asylumCase)
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
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
                    && !emailPreferred.isEmpty()
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
    public PreSubmitCallbackHandler<AsylumCase> appealOutcomeAdminNotificationHandler(
        @Qualifier("appealOutcomeAdminNotificationGenerator") List<NotificationGenerator> notificationGenerators) {
        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
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

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantChangeDirectionDueDateAipNotificationHandler(
        @Qualifier("appellantChangeDirectionDueDateAipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isAppellant = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.APPELLANT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                    && isAppellant
                    && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantAndRespondentChangeDirectionDueDateAipNotificationHandler(
        @Qualifier("appellantAndRespondentChangeDirectionDueDateAipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isAppellantAndRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.APPELLANT_AND_RESPONDENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                    && isAppellantAndRespondent
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedAppellantOnlyChangeDirectionDueDateNotificationHandler(
        @Qualifier("internalDetainedAppellantOnlyChangeDirectionDueDateNotificationGenerator")
        List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isAppellant = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.APPELLANT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                    && isAppellant
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedAppellantRespondentChangeDirectionDueDateNotificationHandler(
        @Qualifier("internalDetainedAppellantRespondentChangeDirectionDueDateNotificationGenerator")
        List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean isAppellantAndRespondent = asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                    .map(Parties -> Parties.equals(Parties.APPELLANT_AND_RESPONDENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE
                    && isAppellantAndRespondent
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedAppellantHoChangeDirectionDueDateNotificationHandler(
        @Qualifier("internalDetainedAppellantHoChangeDirectionDueDateNotificationGenerator")
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
                    && isAppellantInDetention(asylumCase)
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> listCaseNotificationHandler(
        @Qualifier("listCaseNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - listCase
        return new NotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedAsylumCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == LIST_CASE);

                if (isAllowedAsylumCase) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == LIST_CASE
                        && isRepJourney(callback.getCaseDetails().getCaseData())
                        && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                        && !isAcceleratedDetainedAppeal(asylumCase));
                } else {
                    return false;
                }
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> listCaseAdaNotificationHandler(
        @Qualifier("listCaseAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // ADA listCase
        return new NotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedAsylumCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == LIST_CASE);

                if (isAllowedAsylumCase) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == LIST_CASE
                        && isRepJourney(callback.getCaseDetails().getCaseData())
                        && isAcceleratedDetainedAppeal(asylumCase))
                        && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
                } else {
                    return false;
                }
            },
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
                    && callback.getEvent() == LIST_CASE
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> listCaseInternalDetainedNotificationHandler(
        @Qualifier("listCaseInternalDetainedNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                boolean isAllowedAsylumCase = (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == LIST_CASE);

                if (isAllowedAsylumCase) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return (callback.getEvent() == LIST_CASE
                        && isInternalCase(asylumCase)
                        && !isAcceleratedDetainedAppeal(asylumCase));
                } else {
                    return false;
                }
            },
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
    public PreSubmitCallbackHandler<AsylumCase> requestHearingRequirementsInternalDetainedNotificationHandler(
        @Qualifier("requestHearingRequirementsInternalDetainedNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return (callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase));
            },
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData());
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentEvidenceInternalNotificationHandler(
        @Qualifier("respondentEvidenceInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-6702 - requestRespondentEvidence internal
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE
                    && isRepJourney(asylumCase)
                    && isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentEvidenceInternalNonAdaNotificationHandler(
        @Qualifier("respondentEvidenceInternalNonAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentReviewNotificationHandler(
        @Qualifier("respondentReviewNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - requestRespondentReview
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW
                    && isRepJourney(asylumCase)
                    && !isInternalCase(asylumCase);
            }, notificationGenerators
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
    public PreSubmitCallbackHandler<AsylumCase> respondentReviewInternalNotificationHandler(
        @Qualifier("respondentReviewInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_RESPONDENT_REVIEW
                    && isInternalCase(asylumCase);
            }, notificationGenerators
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

                RemissionOption remissionOption = asylumCase
                    .read(REMISSION_OPTION, RemissionOption.class).orElse(RemissionOption.NO_REMISSION);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isAipJourney(asylumCase)
                    && isAppealOnTime
                    && !isEaHuEuAppeal(asylumCase)
                    && !isDlrmFeeRemissionEnabled(asylumCase))
                    || (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isAipJourney(asylumCase)
                    && isAppealOnTime
                    && !isEaHuEuAppeal(asylumCase)
                    && isDlrmFeeRemissionEnabled(asylumCase)
                    && remissionOption == RemissionOption.NO_REMISSION);
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
                    && (callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == REP).orElse(true)
                    || isInternalCase(asylumCase))
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
                    && (isRepJourney(asylumCase) || isInternalCase(asylumCase))
                    && (paymentPaid || payLater);

            }, notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealInternalHoNotificationHandler(
        @Qualifier("submitAppealHoNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        // RIA-7948 - submitAppeal HO missing notification
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRpAndDcAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == RP || type == DC).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && (isAcceleratedDetainedAppeal(asylumCase) || (!isAcceleratedDetainedAppeal(asylumCase) && isRpAndDcAppealType));
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

                RemissionOption remissionOption = asylumCase
                    .read(REMISSION_OPTION, RemissionOption.class).orElse(RemissionOption.NO_REMISSION);

                return ((callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isAipJourney(asylumCase)
                    && isOutOfTimeAppeal
                    && !isEaHuEuAppeal(asylumCase)
                    && !isDlrmFeeRemissionEnabled(asylumCase))
                    ||
                    (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.SUBMIT_APPEAL
                        && isAipJourney(asylumCase)
                        && isOutOfTimeAppeal
                        && !isEaHuEuAppeal(asylumCase)
                        && isDlrmFeeRemissionEnabled(asylumCase)
                        && remissionOption == RemissionOption.NO_REMISSION));
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
                    && isAipJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestCaseBuildingNotificationHandler(
        @Qualifier("requestCaseBuildingNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_CASE_BUILDING
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
                    && !isAipJourney(asylumCase)
                    && callback.getCaseDetails().getState() != State.AWAITING_RESPONDENT_EVIDENCE
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipRespondentDirectionNotificationHandler(
        @Qualifier("aipRespondentDirectionNotificationGenerator") List<NotificationGenerator> notificationGenerators,
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
                    && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppellantAndRespondentDirectionNotificationHandler(
        @Qualifier("aipAppellantAndRespondentDirectionNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                    && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.APPELLANT_AND_RESPONDENT)
                    && isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppellantNonStandardDirectionNotificationHandler(
        @Qualifier("aipAppellantNonStandardDirectionNotificationGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                    && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.APPELLANT)
                    && isAipJourney(asylumCase);
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
                    && isRepJourney(asylumCase)
                    && !isInternalCase(asylumCase);
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
                    && !isAipJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                    && directionFinder
                    .findFirst(asylumCase, DirectionTag.NONE)
                    .map(direction -> direction.getParties().equals(Parties.BOTH))
                    .orElse(false);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantRespondentInternalNonStandardDirectionHandler(
        @Qualifier("appellantRespondentInternalNonStandardDirectionGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                    && isInternalNonStdDirectionWithParty(asylumCase, Parties.APPELLANT_AND_RESPONDENT, directionFinder);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> hoInternalNonStandardDirectionHandler(
        @Qualifier("hoInternalNonStandardDirectionGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                    && isInternalNonStdDirectionWithParty(asylumCase, Parties.RESPONDENT, directionFinder);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantInternalNonStandardDirectionHandler(
        @Qualifier("appellantInternalNonStandardDirectionGenerator") List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                    && isInternalNonStdDirectionWithParty(asylumCase, Parties.APPELLANT, directionFinder);
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData())
                    && !isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editCaseListingAdaRepNotificationHandler(
        @Qualifier("editCaseListingAdaRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.EDIT_CASE_LISTING
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData())
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()),
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
                    && !isInternalCase(callback.getCaseDetails().getCaseData())
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
    public PreSubmitCallbackHandler<AsylumCase> uploadHomeOfficeAppealResponseInternalAdaNotificationHandler(
        @Qualifier("uploadHomeOfficeAppealResponseInternalAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE
                    && AsylumCaseUtils.isInternalCase(asylumCase)
                    && AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestResponseReviewNotificationHandler(
        @Qualifier("requestResponseReviewNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
    public PreSubmitCallbackHandler<AsylumCase> hearingBundleReadyInternalDetNotificationHandler(
        @Qualifier("HearingBundleReadyInternalDetNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.ASYNC_STITCHING_COMPLETE
                && callback.getCaseDetails().getState() != State.FTPA_DECIDED
                && "DONE".equalsIgnoreCase(getStitchStatus(callback))
                && AsylumCaseUtils.isInternalCase(callback.getCaseDetails().getCaseData()),
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

        final List<Bundle> caseBundles = maybeCaseBundles.map(idValues -> idValues
            .stream()
            .map(IdValue::getValue)
            .collect(Collectors.toList())).orElse(Collections.emptyList());

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
                    && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS
                    && !isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> reviewedAdaHearingRequirementsNotificationHandler(
        @Qualifier("reviewedAdaHearingRequirementsNotificationGenerator")
        List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(

            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS
                    && isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()),
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
    public PreSubmitCallbackHandler<AsylumCase> uploadAdditionalEvidenceAipHandler(
        @Qualifier("uploadAdditionalEvidenceAip") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPLOAD_ADDITIONAL_EVIDENCE
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadAdditionalEvidenceHomeOfficeHandler(
        @Qualifier("uploadAdditionalEvidenceHomeOffice") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
                    && isAipJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(callback.getCaseDetails().getCaseData()),
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
    public PreSubmitCallbackHandler<AsylumCase> uploadAddendumEvidenceAdminOfficerInternalHandler(
        @Qualifier("uploadAddendumEvidenceAdminOfficerInternal") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
                    && isInternalCase(callback.getCaseDetails().getCaseData()),
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(callback.getCaseDetails().getCaseData()),
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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
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
                    && isAipJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(callback.getCaseDetails().getCaseData()),
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> changeHearingCentreInternalNotificationHandler(
        @Qualifier("changeHearingCentreInternalNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.CHANGE_HEARING_CENTRE
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);

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
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT
                    && !isAipJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaSubmittedAipNotificationHandler(
        @Qualifier("ftpaSubmittedAipNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        // RIA-6112
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaSubmittedDetentionEngagementTeamNotificationHandler(
        @Qualifier("ftpaSubmittedDetentionEngagementTeamNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT
                    && AsylumCaseUtils.isInternalCase(callback.getCaseDetails().getCaseData()),
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
                        && !isAipJourney(callback.getCaseDetails().getCaseData());
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
        @Qualifier("respondentFtpaSubmittedNotificationGeneratorLegalRep") List<NotificationGenerator> notificationGenerator) {

        // RIA-3316 - applyForFTPARespondent
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_RESPONDENT
                    && !isAipJourney(callback.getCaseDetails().getCaseData())
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaSubmittedRespondentAipJourneyNotificationHandler(
        @Qualifier("ftpaSubmittedRespondentAipJourneyNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        // RIA-6115 - applyForFTPARespondent - notification to appellant
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_RESPONDENT
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedApplyForFtpaRespondentNotificationHandler(
        @Qualifier("respondentFtpaSubmittedNotificationGeneratorDetentionEngagementTeam") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.APPLY_FOR_FTPA_RESPONDENT
                    && isInternalCase(asylumCase);
            }, notificationGenerators
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
    public PreSubmitCallbackHandler<AsylumCase> forceAppellantCaseToCaseUnderReviewEmailNotificationHandler(
        @Qualifier("forceAppellantCaseToCaseUnderReviewEmailNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> function = (callbackStage, callback) -> {
            AsylumCase caseData = callback.getCaseDetails().getCaseData();

            return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW
                && isAipJourney(caseData)
                && isEmailPreferred(caseData);
        };
        return new NotificationHandler(function, notificationGenerators);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> forceAppellantCaseToCaseUnderReviewSmsNotificationHandler(
        @Qualifier("forceAppellantCaseToCaseUnderReviewSmsNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        BiPredicate<PreSubmitCallbackStage, Callback<AsylumCase>> function = (callbackStage, callback) -> {
            AsylumCase caseData = callback.getCaseDetails().getCaseData();

            return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW
                && isAipJourney(caseData)
                && isSmsPreferred(caseData));
        };
        return new NotificationHandler(function, notificationGenerators);
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
    public PreSubmitCallbackHandler<AsylumCase> adjournHearingWithoutDateNonIntegratedHandler(
        @Qualifier("adjournHearingWithoutDateNonIntegratedNotificationGenerator")
        List<NotificationGenerator> notificationGenerator) {

        // RIA-3631 adjournHearingWithoutDate
        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.ADJOURN_HEARING_WITHOUT_DATE
                && !isListAssistIntegrated(callback.getCaseDetails().getCaseData())
                && !isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adjournHearingWithoutDateIntegratedHandler(
        @Qualifier("adjournHearingWithoutDateIntegratedNotificationGenerator")
        List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.ADJOURN_HEARING_WITHOUT_DATE
                && isListAssistIntegrated(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordAdjournmentDetailsNonIntegratedHandler(
        @Qualifier("recordAdjournmentDetailsNonIntegratedNotificationGenerator")
        List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean cannotRelistCaseImmediately =
                    asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
                        .map(relistCaseImmediately -> relistCaseImmediately == NO)
                        .orElse(false);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_ADJOURNMENT_DETAILS
                    && cannotRelistCaseImmediately
                    && !isListAssistIntegrated(callback.getCaseDetails().getCaseData()));
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordAdjournmentDetailsIntegratedHandler(
        @Qualifier("recordAdjournmentDetailsIntegratedNotificationGenerator")
        List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean cannotRelistCaseImmediately =
                    asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)
                        .map(relistCaseImmediately -> relistCaseImmediately == NO)
                        .orElse(false);

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_ADJOURNMENT_DETAILS
                    && cannotRelistCaseImmediately
                    && isListAssistIntegrated(callback.getCaseDetails().getCaseData()));
            },
            notificationGenerator
        );
    }

    public PreSubmitCallbackHandler<AsylumCase> internalAdjournHearingWithoutDateHandler(
        @Qualifier("internalAdjournHearingWithoutDateNotificationGenerator")
        List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ADJOURN_HEARING_WITHOUT_DATE
                    && isAppellantInDetention(callback.getCaseDetails().getCaseData())
                    && isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalAdjournHearingWithoutDateNonDetainedHandler(
        @Qualifier("internalAdjournHearingWithoutDateNonDetainedGenerator")
        List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ADJOURN_HEARING_WITHOUT_DATE
                    && !isAppellantInDetention(callback.getCaseDetails().getCaseData())
                    && isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decisionWithoutHearingHandler(
        @Qualifier("decisionWithoutHearingNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        // RIA-6980
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECISION_WITHOUT_HEARING
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decisionWithoutHearingInternalHandler(
        @Qualifier("decisionWithoutHearingInternalNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        // RIA-7929
        return new NotificationHandler(
            (callbackStage, callback) -> {

                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECISION_WITHOUT_HEARING
                    && isInternalCase(asylumCase);
            },
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
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
                    && isRepJourney(asylumCase)
                    && !isInternalCase(asylumCase);
            },
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editAppealAfterSubmitInternalCaseNotificationHandler(
        @Qualifier("editAppealAfterSubmitInternalCaseNotificationGenerator") List<NotificationGenerator> notificationGenerator) {
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
                    && isInternalCase(asylumCase);
            },
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

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isNotAdmittedOrRefusesOrRemade32Outcome(asylumCase,
                    FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)
                    && !AsylumCaseUtils.isInternalCase(asylumCase)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT")
                    && !isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionRefusedOrNotAdmittedAppellantAipJourneyNotificationHandler(
        @Qualifier("ftpaApplicationDecisionRefusedOrNotAdmittedAppellantAipJourneyNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        //RIA-3631 leadership/resident judge decision
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isNotAdmittedOrRefusesOrRemade32Outcome(asylumCase,
                    FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_TO_APPELLANT_EMAIL")
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS")
                    && isAipJourney(asylumCase);
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

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && !AsylumCaseUtils.isInternalCase(asylumCase)
                    && isGrantedOrPartiallyGrantedOutcome(asylumCase,
                    FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_LEGAL_REPRESENTATIVE_APPELLANT")
                    && !isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionGrantedOrPartiallyGrantedAppellantAipJourneyNotificationHandler(
        @Qualifier("ftpaApplicationDecisionGrantedOrPartiallyGrantedAppellantAipJourneyNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        // RIA-6113
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isGrantedOrPartiallyGrantedOutcome(asylumCase,
                    FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_TO_APPELLANT_EMAIL")
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_TO_APPELLANT_SMS")
                    && isAipJourney(asylumCase);
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
                    && (callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && !AsylumCaseUtils.isInternalCase(asylumCase)
                    && isReheardDecisionOutcome
                    && !isDlrmSetAsideEnabled(asylumCase)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
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
                    && isRpAndDcAppealType
                    && !isAcceleratedDetainedAppeal(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
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

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isNotAdmittedOrRefusesOrRemade32Outcome(asylumCase,
                    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)
                    && !AsylumCaseUtils.isInternalCase(asylumCase)
                    && !isAipJourney(asylumCase)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT");
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionRefusedOrNotAdmittedRespondentAipJourneyNotificationHandler(
        @Qualifier("ftpaApplicationDecisionRefusedOrNotAdmittedRespondentAipJourneyNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        // RIA-6135
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isNotAdmittedOrRefusesOrRemade32Outcome(asylumCase,
                    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)
                    && isAipJourney(asylumCase)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT");
            },
            notificationGenerators
        );
    }

    /**
     * To find if the decision outcome was not admitted or refused or remade-32. It checks the leadership judge outcome,
     * if not present, it checks the resident judge outcome. The parameters should be both of type appellant or both
     * of type respondent.
     * @param asylumCase The asylum case
     * @param ftpaByWhoLjDecisionOutcomeType leadership judge decision on ftpa application by appellant or respondent,
     *                                       FTPA_APPELLANT_DECISION_OUTCOME_TYPE or FTPA_RESPONDENT_DECISION_OUTCOME_TYPE
     * @param ftpaByWhoRjDecisionOutcomeType resident judge decision on ftpa application by appellant or respondent,
     *                                       FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE or FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE
     * @return true if either the first (or if missing then the second) parameter is FTPA_NOT_ADMITTED or FTPA_REFUSED or FTPA_REMADE32
     */
    private boolean isNotAdmittedOrRefusesOrRemade32Outcome(AsylumCase asylumCase,
                                                            AsylumCaseDefinition ftpaByWhoLjDecisionOutcomeType,
                                                            AsylumCaseDefinition ftpaByWhoRjDecisionOutcomeType) {
        List<String> ftpaDecisionOutcomeTypes = new ArrayList<>(List.of(
            FtpaDecisionOutcomeType.FTPA_NOT_ADMITTED.toString(),
            FtpaDecisionOutcomeType.FTPA_REFUSED.toString()
        ));

        if (!isDlrmSetAsideEnabled(asylumCase)) {
            ftpaDecisionOutcomeTypes.add(FtpaDecisionOutcomeType.FTPA_REMADE32.toString());
        }

        boolean isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome = asylumCase
            .read(ftpaByWhoLjDecisionOutcomeType, FtpaDecisionOutcomeType.class)
            .map(decision -> ftpaDecisionOutcomeTypes.contains(decision.toString()))
            .orElse(false);

        if (!isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome) {
            isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome = asylumCase
                .read(ftpaByWhoRjDecisionOutcomeType, FtpaDecisionOutcomeType.class)
                .map(decision -> ftpaDecisionOutcomeTypes.contains(decision.toString()))
                .orElse(false);
        }

        return isAllowedOrDismissedOrRefusedOrNotAdmittedDecisionOutcome;
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRespondentFtpaApplicationHoNotificationHandler(
        @Qualifier("internalRespondentFtpaApplicationHoNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isRespondentApplication = asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)
                    .map(applicantType -> RESPONDENT == applicantType).orElse(false);
                FtpaDecisionOutcomeType decisionOutcomeType = AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase)
                    .orElse(null);
                Set<FtpaDecisionOutcomeType> validDecisionOutcomeTypes = Set.of(
                    FTPA_REFUSED,
                    FTPA_GRANTED,
                    FTPA_NOT_ADMITTED,
                    FTPA_PARTIALLY_GRANTED
                );

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION)
                    && isRespondentApplication
                    && validDecisionOutcomeTypes.contains(decisionOutcomeType)
                    && AsylumCaseUtils.isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRespondentFtpaApplicationDetNotificationHandler(
        @Qualifier("internalRespondentFtpaApplicationDetNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                boolean isRespondentApplication = asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)
                    .map(applicantType -> RESPONDENT == applicantType).orElse(false);
                FtpaDecisionOutcomeType decisionOutcomeType = AsylumCaseUtils.getFtpaDecisionOutcomeType(asylumCase)
                    .orElse(null);
                Set<FtpaDecisionOutcomeType> validDecisionOutcomeTypes = Set.of(
                    FTPA_REFUSED,
                    FTPA_GRANTED,
                    FTPA_PARTIALLY_GRANTED
                );

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    && isRespondentApplication
                    && validDecisionOutcomeTypes.contains(decisionOutcomeType)
                    && AsylumCaseUtils.isAppellantInDetention(asylumCase)
                    && AsylumCaseUtils.isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    /**
     * To find if the decision outcome was granted or partially granted. It checks the leadership judge outcome,
     * if not present, it checks the resident judge outcome. The parameters should be both of type appellant or both
     * of type respondent.
     * @param asylumCase The asylum case
     * @param ftpaByWhoLjDecisionOutcomeType leadership judge decision on ftpa application by appellant or respondent,
     *                                       FTPA_APPELLANT_DECISION_OUTCOME_TYPE or FTPA_RESPONDENT_DECISION_OUTCOME_TYPE
     * @param ftpaByWhoRjDecisionOutcomeType resident judge decision on ftpa application by appellant or respondent,
     *                                       FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE or FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE
     * @return true if either the first (or if missing then the second) parameter is FTPA_GRANTED or FTPA_PARTIALLY_GRANTED
     */
    private boolean isGrantedOrPartiallyGrantedOutcome(AsylumCase asylumCase,
                                                       AsylumCaseDefinition ftpaByWhoLjDecisionOutcomeType,
                                                       AsylumCaseDefinition ftpaByWhoRjDecisionOutcomeType) {


        boolean isGrantedOrPartiallyGrantedOutcome = asylumCase
            .read(ftpaByWhoLjDecisionOutcomeType, FtpaDecisionOutcomeType.class)
            .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())
                || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString()))
            .orElse(false);

        if (!isGrantedOrPartiallyGrantedOutcome) {
            isGrantedOrPartiallyGrantedOutcome = asylumCase
                .read(ftpaByWhoRjDecisionOutcomeType,
                    FtpaDecisionOutcomeType.class)
                .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())
                    || decision.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString()))
                .orElse(false);
        }

        return isGrantedOrPartiallyGrantedOutcome;
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentNotificationHandler(
        @Qualifier("ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        // RIA-3631
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isGrantedOrPartiallyGrantedOutcome(asylumCase,
                    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)
                    && !AsylumCaseUtils.isInternalCase(asylumCase)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT")
                    && !isAipJourney(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationInternalDecisionGrantedOrPartiallyGrantedRespondentNotificationHandler(
        @Qualifier("ftpaApplicationInternalDecisionGrantedOrPartiallyGrantedRespondentNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                    && isGrantedOrPartiallyGrantedOutcome(asylumCase,
                    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)
                    && AsylumCaseUtils.isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentAipJourneyNotificationHandler(
        @Qualifier("ftpaApplicationDecisionGrantedOrPartiallyGrantedRespondentAipJourneyNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        // RIA-6116
        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && isGrantedOrPartiallyGrantedOutcome(asylumCase,
                    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE)
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT")
                    && isAipJourney(asylumCase);
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
                    .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
                    .map(ApplicantType::getValue)
                    .orElse("");

                final String hoRequestEvidenceInstructStatus =
                    ftpaApplicantType.equals("appellant")
                        ? asylumCase.read(HOME_OFFICE_FTPA_APPELLANT_DECIDED_INSTRUCT_STATUS,
                        String.class).orElse("")
                        : asylumCase.read(HOME_OFFICE_FTPA_RESPONDENT_DECIDED_INSTRUCT_STATUS,
                        String.class).orElse("");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.LEADERSHIP_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
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
                List<String> ftpaDecisionOutcomeTypes = new ArrayList<>(List.of(
                    FtpaDecisionOutcomeType.FTPA_REHEARD32.toString()
                ));

                if (!isDlrmSetAsideEnabled(asylumCase)) {
                    ftpaDecisionOutcomeTypes.add(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString());
                }

                boolean isReheardDecisionOutcome = asylumCase
                    .read(AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                    .map(decision -> ftpaDecisionOutcomeTypes.contains(decision.toString()))
                    .orElse(false);
                if (!isReheardDecisionOutcome) {
                    isReheardDecisionOutcome = asylumCase
                        .read(AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE,
                            FtpaDecisionOutcomeType.class)
                        .map(decision -> ftpaDecisionOutcomeTypes.contains(decision.toString()))
                        .orElse(false);
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && (callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                    || callback.getEvent() == Event.DECIDE_FTPA_APPLICATION)
                    && !AsylumCaseUtils.isInternalCase(asylumCase)
                    && isReheardDecisionOutcome
                    && hasThisNotificationNotSentBefore(asylumCase, callback,
                    "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT");
            },
            notificationGenerators
        );
    }

    private boolean hasThisNotificationNotSentBefore(AsylumCase asylumCase, Callback<AsylumCase> callback,
                                                     String notificationReference) {
        Optional<List<IdValue<String>>> maybeNotificationSent =
            asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent =
            maybeNotificationSent
                .orElseGet(ArrayList::new);

        return notificationsSent
            .stream()
            .noneMatch(notification ->
                notification.getId().equals(callback.getCaseDetails().getId() + notificationReference));
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealPayOfflineNotificationHandler(
        @Qualifier("submitAppealPayOfflineNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                String paPaymentOption = asylumCase
                    .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isCorrectAppealType
                    && !isInternalCase(asylumCase)
                    && (paPaymentOption.equals("payOffline") || isRemissionOptedForEaOrHuOrPaAppeal(callback));
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealPayOfflineInternalNotificationHandler(
        @Qualifier("submitAppealPayOfflineInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isCorrectAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == PA).orElse(false);

                String paPaymentOption = asylumCase
                    .read(AsylumCaseDefinition.PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && isCorrectAppealType
                    && (paPaymentOption.equals("payOffline") || isRemissionOptedForEaOrHuOrPaAppeal(callback));
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
                    && !isInternalCase(callback.getCaseDetails().getCaseData())
                    && (isPaymentPendingForEaOrHuAppeal(callback)
                    || isPaymentPendingForEaOrHuAppealWithRemission(callback)),
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealPendingPaymentInternalNotificationHandler(
        @Qualifier("submitAppealPendingPaymentInternalNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        // RIA-3631 - submitAppeal This needs to be changed as per ACs
        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(callback.getCaseDetails().getCaseData())
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
                    && paymentStatus.isPresent()
                    && !paymentStatus.equals(Optional.empty())
                    && paymentStatus.get().equals(PaymentStatus.PAID)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
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
                    && paymentStatus.isPresent()
                    && !paymentStatus.equals(Optional.empty())
                    && paymentStatus.get().equals(PaymentStatus.PAID);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAsPaidInternalDetandHoNotificationHandler(
        @Qualifier("markAsPaidInternalDetandHonotificationGenerator")
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
                    && paymentStatus.isPresent()
                    && !paymentStatus.equals(Optional.empty())
                    && paymentStatus.get().equals(PaymentStatus.PAID)
                    && isInternalCase(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase);
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
                    && isApproved
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);

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
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(callback.getCaseDetails().getCaseData()),
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
    public PreSubmitCallbackHandler<AsylumCase> reinstateAppealInternalNotificationHandler(
        @Qualifier("reinstateAppealInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REINSTATE_APPEAL
                    && isInternalCase(callback.getCaseDetails().getCaseData()),
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
                    && isRepJourney(asylumCase)
                    && !isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalMakeAnApplicationNotificationHandler(
        @Qualifier("internalMakeAnApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MAKE_AN_APPLICATION
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideAnApplicationLegalRepNotificationHandler(
        @Qualifier("decideAnApplicationLegalRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_AN_APPLICATION
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }


    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideAnApplicationInternalNotificationHandler(
        @Qualifier("decideAnApplicationInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_AN_APPLICATION
                    && isInternalCase(callback.getCaseDetails().getCaseData())
                    && isApplicationCreatedByAdmin(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideARespondentApplicationInternalNotificationHandler(
        @Qualifier("decideARespondentApplicationInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_AN_APPLICATION
                    && isInternalCase(callback.getCaseDetails().getCaseData())
                    && isApplicationCreatedByRespondent(callback.getCaseDetails().getCaseData()),
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
                       && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                       && isPartiallyApproved
                       && isEaHuEuAppeal(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> remissionDecisionPaPartiallyApprovedNotificationHandler(
        @Qualifier("remissionDecisionPaPartiallyApprovedNotificationGenerator")
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
                       && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                       && isPartiallyApproved
                       && isPaAppeal(asylumCase);
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
                       && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                       && isRejected
                       && isEaHuEuAppeal(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> remissionDecisionPaRejectedNotificationHandler(
        @Qualifier("remissionDecisionPaRejectedNotificationGenerator")
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
                       && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                       && isRejected
                       && isPaAppeal(asylumCase);
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
                boolean isRefundInstructed = false;
                if (completedStages.isPresent() && !completedStages.get().isEmpty()) {
                    isRefundInstructed = completedStages.get().get(completedStages.get().size() - 1)
                        .equals("feeUpdateRefundInstructed");
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MANAGE_FEE_UPDATE
                    && isRefundInstructed;
            },
            notificationGenerators
        );

    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> manageFeeUpdateAdditionalPaymentRequestedNotificationHandler(
        @Qualifier("manageFeeUpdateAdditionalPaymentRequestedNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                boolean additionalPaymentRequested = asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)
                    .map(action -> ADDITIONAL_PAYMENT == action)
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MANAGE_FEE_UPDATE
                    && additionalPaymentRequested
                    && !isAipJourney(asylumCase)
                    && isDlrmFeeRefundEnabled(asylumCase);
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
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && (callback.getEvent() == Event.REMOVE_REPRESENTATION
                || callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE),
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentationAppellantEmailNotificationHandler(
        @Qualifier("removeRepresentationAppellantEmailNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && callback.getEvent() == Event.REMOVE_REPRESENTATION
                && callback.getCaseDetails().getCaseData().read(EMAIL, String.class).isPresent(),
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentationAppellantSmsNotificationHandler(
        @Qualifier("removeRepresentationAppellantSmsNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && callback.getEvent() == Event.REMOVE_REPRESENTATION
                && callback.getCaseDetails().getCaseData().read(MOBILE_NUMBER, String.class).isPresent(),
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentativeAppellantEmailNotificationHandler(
        @Qualifier("removeRepresentativeAppellantEmailNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE
                && callback.getCaseDetails().getCaseData().read(EMAIL, String.class).isPresent(),
            notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> removeRepresentativeAppellantSmsNotificationHandler(
        @Qualifier("removeRepresentativeAppellantSmsNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new PostSubmitNotificationHandler(
            (callbackStage, callback) -> callbackStage == PostSubmitCallbackStage.CCD_SUBMITTED
                && callback.getEvent() == Event.REMOVE_LEGAL_REPRESENTATIVE
                && callback.getCaseDetails().getCaseData().read(MOBILE_NUMBER, String.class).isPresent(),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> requestFeeRemissionNotificationHandler(
        @Qualifier("requestFeeRemissionNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && !isAipJourney(callback.getCaseDetails().getCaseData())
                && callback.getEvent() == Event.REQUEST_FEE_REMISSION,
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
                    && paymentStatus.isPresent()
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
                    && smsPreferred
                    && !isAgeAssessmentAppeal(asylumCase);
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
                    && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                    && !isAgeAssessmentAppeal(asylumCase);
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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);

            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> recordOfTimeDecisionCanProceedEmailInternalNotificationHandler(
        @Qualifier("recordOfTimeDecisionCanProceedEmailInternalNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                OutOfTimeDecisionType outOfTimeDecisionType =
                    asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                        .orElse(UNKNOWN);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION
                    && outOfTimeDecisionType == OutOfTimeDecisionType.APPROVED
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase);

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
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);

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
                    && !isInternalCase(callback.getCaseDetails().getCaseData())
                    && (!paymentFailed || paymentFailedChangedToPayLater)
                    && !isPaymentPendingForEaOrHuAppeal(callback))
                    || (callback.getEvent() == Event.RECORD_REMISSION_DECISION
                    && isRemissionApproved
                    && isEaHuEuAppeal(asylumCase));
            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payAndSubmitAppealEmailInternalNotificationHandler(
        @Qualifier("payAndSubmitAppealEmailInternalNotificationGenerator")
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
                    && isInternalCase(asylumCase)
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
                    && !isInternalCase(asylumCase)
                    && paymentFailed
                    && isPaAppealType;
            }, notificationGenerators
        );
    }

    @Bean
    public PostSubmitCallbackHandler<AsylumCase> payAndSubmitAppealFailedEmailInternalNotificationHandler(
        @Qualifier("payAndSubmitAppealFailedEmailInternalNotificationGenerator")
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
                    && isInternalCase(asylumCase)
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
                    && !isRemissionRejectedAndPaymentChangedToCard(asylumCase));
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
                    && paymentStatus.isPresent()
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

                final String stitchStatus = getStitchStatus(callback);

                log.info("Entering notification handler config with values: event {}and state {} stitch status {}",
                    callback.getEvent(), callback.getCaseDetails().getState(), stitchStatus);

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
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Objects.equals(Event.END_APPEAL_AUTOMATICALLY, callback.getEvent())
                    && isRepJourney(asylumCase)
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppealEndedAutomaticallyNotificationHandler(
        @Qualifier("aipAppealEndedAutomaticallyNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Objects.equals(Event.END_APPEAL_AUTOMATICALLY, callback.getEvent())
                    && isAipJourney(asylumCase)
                    && !isInternalCase(asylumCase);
            }, notificationGenerators
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

    private boolean isListAssistIntegrated(AsylumCase asylumCase) {
        return asylumCase.read(IS_INTEGRATED, YesOrNo.class)
            .map(isIntegrated -> isIntegrated == YES)
            .orElse(false);
    }

    private boolean hasRepEmail(AsylumCase asylumCase) {
        return hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase) ? asylumCase.read(LEGAL_REP_EMAIL, String.class).isPresent()
            : asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).isPresent();
    }

    private boolean isSmsPreferred(AsylumCase asylumCase) {

        final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

        Set<IdValue<Subscriber>> smsPreferred = maybeSubscribers
            .orElse(Collections.emptyList()).stream()
            .filter(subscriber -> YES.equals(subscriber.getValue().getWantsSms()))
            .collect(Collectors.toSet());

        return !smsPreferred.isEmpty() && smsPreferred.stream().findFirst().map(subscriberIdValue ->
            subscriberIdValue.getValue().getMobileNumber()).isPresent();
    }

    private boolean isEmailPreferred(AsylumCase asylumCase) {

        final Optional<List<IdValue<Subscriber>>> maybeSubscribers = asylumCase.read(SUBSCRIPTIONS);

        Set<IdValue<Subscriber>> smsPreferred = maybeSubscribers
            .orElse(Collections.emptyList()).stream()
            .filter(subscriber -> YES.equals(subscriber.getValue().getWantsEmail()))
            .collect(Collectors.toSet());

        return !smsPreferred.isEmpty() && smsPreferred.stream().findFirst().map(subscriberIdValue ->
            subscriberIdValue.getValue().getEmail()).isPresent();
    }

    private ErrorHandler<AsylumCase> getErrorHandler() {
        return (callback, e) -> callback
            .getCaseDetails()
            .getCaseData()
            .write(SUBMIT_NOTIFICATION_STATUS, "Failed");
    }

    private ErrorHandler<AsylumCase> getSmsErrorHandling() {
        return (callback, e) -> log.error(
            "cannot send sms notification to the appellant on submitAppeal, caseId: {}",
            callback.getCaseDetails().getId(),
            e
        );
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
                    && paymentStatus.isPresent()
                    && !paymentStatus.equals(Optional.empty())
                    && !payLater
                    && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                    && paymentStatus.get().equals(PaymentStatus.PAID)
                    && !isAcceleratedDetainedAppeal(asylumCase);
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
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase)
                    && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                    && (isPaAppealType
                    && paAppealTypePaymentOption.equals("payNow"))
                    && !isAcceleratedDetainedAppeal(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealLrHoWaysToPayPaPayNowInternalNotificationHandler(
        @Qualifier("submitAppealLrHoWaysToPayPaPayNowInternalNotificationGenerator")
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
                    && isInternalCase(asylumCase)
                    && asylumCase.read(HAS_SERVICE_REQUEST_ALREADY, YesOrNo.class).isPresent()
                    && (isPaAppealType
                    && paAppealTypePaymentOption.equals("payNow"))
                    && !isAcceleratedDetainedAppeal(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealLegalRepAaaNotificationHandler(
        @Qualifier("submitAppealLegalRepAaaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && (isAgeAssessmentAppeal(asylumCase) || isAcceleratedDetainedAppeal(asylumCase));
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> startAppealLegalRepDisposalNotificationHandler(
        @Qualifier("startAppealLegalRepDisposalNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                boolean canBeHandled = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == START_APPEAL;

                if (canBeHandled) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return isRepJourney(asylumCase) && !isInternalCase(asylumCase);
                } else {
                    return false;
                }
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editAppealLegalRepDisposalNotificationHandler(
        @Qualifier("editAppealLegalRepDisposalNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                boolean canBeHandled = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == EDIT_APPEAL;
                if (canBeHandled) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    Optional<JourneyType> journeyTypeOpt = asylumCase.read(JOURNEY_TYPE);

                    return isRepJourney(asylumCase) && !isInternalCase(asylumCase);
                } else {
                    return false;
                }
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> startAppealAipAppellantDisposalNotificationHandler(
        @Qualifier("startAppealAipAppellantDisposalNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {
        return new NotificationHandler(
            (callbackStage, callback) -> {

                boolean canBeHandled = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == START_APPEAL;

                if (canBeHandled) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return isAipJourney(asylumCase);
                } else {
                    return false;
                }
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editAppealAipAppellantDisposalNotificationHandler(
        @Qualifier("editAppealAipAppellantDisposalNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                boolean canBeHandled = callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == EDIT_APPEAL;

                if (canBeHandled) {
                    AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                    return isAipJourney(asylumCase);
                } else {
                    return false;
                }
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adaSuitabilityNotificationHandler(
        @Qualifier("adaSuitabilityNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ADA_SUITABILITY_REVIEW
                    && isRepJourney(callback.getCaseDetails().getCaseData())
                    && !AsylumCaseUtils.isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adaSuitabilityInternalAdaNotificationHandler(
        @Qualifier("adaSuitabilityInternalAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.ADA_SUITABILITY_REVIEW
                    && AsylumCaseUtils.isInternalCase(asylumCase)
                    && AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> transferOutOfAdaNotificationHandler(
        @Qualifier("transferOutOfAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.TRANSFER_OUT_OF_ADA
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedTransferOutOfAdaNotificationHandler(
        @Qualifier("internalDetainedTransferOutOfAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.TRANSFER_OUT_OF_ADA
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAppealAsAdaNotificationHandler(
        @Qualifier("markAppealAsAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_APPEAL_AS_ADA
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> removeDetentionStatusNotificationHandler(
        @Qualifier("removeDetentionStatusNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REMOVE_DETAINED_STATUS
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> removeDetentionStatusInternalNotificationHandler(
        @Qualifier("removeDetentionStatusInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REMOVE_DETAINED_STATUS
                    && isInternalWithoutLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAsDetainedNotificationHandler(
        @Qualifier("markAsDetainedNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_APPEAL_AS_DETAINED
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealOutOfTimeWithFeeAppellantLetterNotificationHandler(
        @Qualifier("internalSubmitAppealWithFeeOutOfTimeAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaymentPending = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).map(status -> status == PAYMENT_PENDING).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isSubmissionOutOfTime(asylumCase)
                    && isPaymentPending;

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAsReadyForUtTransferNotificationHandler(
        @Qualifier("markAsReadyForUtTransferNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_AS_READY_FOR_UT_TRANSFER
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData())
                    && isRepJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAsReadyForUtTransferInternalNotificationHandler(
        @Qualifier("markAsReadyForUtTransferInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_AS_READY_FOR_UT_TRANSFER
                    && isInternalCase(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipMarkAsReadyForUtTransferNotificationHandler(
        @Qualifier("aipMarkAsReadyForUtTransferNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Objects.equals(Event.MARK_AS_READY_FOR_UT_TRANSFER, callback.getEvent())
                    && isAipJourney(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRequestCaseBuildingNotificationHandler(
        @Qualifier("internalRequestCaseBuildingNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(Event.REQUEST_CASE_BUILDING)
                    && isInternalCase(callback.getCaseDetails().getCaseData())
                    && isAppellantInDetention(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updateDetentionLocationNotificationHandler(
        @Qualifier("updateDetentionLocationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(Event.UPDATE_DETENTION_LOCATION)
                    && isNotInternalOrIsInternalWithLegalRepresentation(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetAppealDecidedNotificationHandler(
        @Qualifier("internalDetAppealDecidedNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(Event.SEND_DECISION_AND_REASONS)
                    && isInternalCase(callback.getCaseDetails().getCaseData())
                    && isAppellantInDetention(callback.getCaseDetails().getCaseData()),
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealInTimeNotificationHandler(
        @Qualifier("internalSubmitAppealInTimeNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAppealOnTime = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == NO).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && isAppealOnTime
                    && !isAcceleratedDetainedAppeal(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalEndAppealAutomaticallyNotificationHandler(
        @Qualifier("internalEndAppealAutomaticallyNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && Objects.equals(Event.END_APPEAL_AUTOMATICALLY, callback.getEvent())
                    && isInternalCase(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedAppealFeeDueNotificationHandler(
        @Qualifier("internalDetainedAppealFeeDueNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isNoRemission = asylumCase.read(REMISSION_TYPE, RemissionType.class)
                    .map(remission -> remission == RemissionType.NO_REMISSION).orElse(false);

                return callback.getEvent() == Event.SUBMIT_APPEAL
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getCaseDetails().getState().equals(State.PENDING_PAYMENT)
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase)
                    && isNoRemission
                    && isEaHuEuAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedAppealFeeDueRecordRemissionDecisionNotificationHandler(
        @Qualifier("internalDetainedAppealFeeDueNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRemissionPartiallyApprovedOrRejected = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> PARTIALLY_APPROVED == decision || REJECTED == decision)
                    .orElse(false);

                return callback.getEvent() == Event.RECORD_REMISSION_DECISION
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getCaseDetails().getState().equals(State.PENDING_PAYMENT)
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase)
                    && isRemissionPartiallyApprovedOrRejected
                    && isEaHuEuAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedReviewHomeOfficeResponseNotificationHandler(
        @Qualifier("internalDetainedReviewHomeOfficeResponseNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callback.getEvent() == Event.REQUEST_RESPONSE_REVIEW
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && !isAcceleratedDetainedAppeal(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedEndAppealNotificationHandler(
        @Qualifier("endAppealInternalDetainedNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callback.getEvent() == Event.END_APPEAL
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedEditCaseListingNotificationHandler(
        @Qualifier("editCaseListingInternalDetainedNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callback.getEvent() == Event.EDIT_CASE_LISTING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDetainedManageFeeUpdateNotificationHandler(
        @Qualifier("internalDetainedManageFeeUpdateNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callback.getEvent() == MANAGE_FEE_UPDATE
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isAppellantInDetention(asylumCase)
                    && isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentTurnOnNotificationsNotificationHandler(
        @Qualifier("respondentTurnOnNotificationsNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {
        //turn on notifications event means implicitly that it is an EJP case
        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();


                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == TURN_ON_NOTIFICATIONS
                    && isInternalCase(asylumCase);
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantRefundRequestedAipNotificationHandler(
        @Qualifier("appellantRefundRequestedAipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REQUEST_FEE_REMISSION
                    && isAipJourney(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantInPersonRemissionPaymentReminderEmailNotificationHandler(
        @Qualifier("appellantInPersonRemissionPaymentReminderEmailNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_REMISSION_REMINDER
                    && isRemissionRejectedOrPartiallyApproved(asylumCase)
                    && isAipJourney(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    private boolean isApplicationCreatedByAdmin(AsylumCase asylumCase) {
        String id = asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class).orElse("");
        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

        //get application from list of applications where application id matches to id
        Optional<MakeAnApplication> makeAnApplication = mayBeMakeAnApplications
            .flatMap(list -> list.stream()
                .filter(application -> application.getId().equals(id))
                .findFirst()
                .map(IdValue::getValue));
        // check if application is present and applicant type is admin
        UserRole applicantRole = UserRole.getById(makeAnApplication
            .map(MakeAnApplication::getApplicantRole).orElse(null));
        return makeAnApplication
            .map(application -> getAdminOfficerRoles().contains(applicantRole))
            .orElse(false);
    }

    private boolean isApplicationCreatedByRespondent(AsylumCase asylumCase) {
        String id = asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class).orElse("");
        Optional<List<IdValue<MakeAnApplication>>> mayBeMakeAnApplications = asylumCase.read(MAKE_AN_APPLICATIONS);

        Optional<MakeAnApplication> makeAnApplication = mayBeMakeAnApplications
            .flatMap(list -> list.stream()
                .filter(application -> application.getId().equals(id))
                .findFirst()
                .map(IdValue::getValue));

        return makeAnApplication
            .map(application -> application.getApplicant().equals(RESPONDENT_APPLICANT))
            .orElse(false);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalMarkAppealAsAdaNotificationHandler(
        @Qualifier("internalMarkAppealAsAdaNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_APPEAL_AS_ADA
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalAppellantFtpaDecidedByRjNotificationHandler(
        @Qualifier("internalAppellantFtpaDecidedByRjNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                if (!callback.getEvent().equals(RESIDENT_JUDGE_FTPA_DECISION)) {
                    return false;
                }

                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAppellantFtpaApplication = asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)
                    .map(applicantType -> APPELLANT == applicantType).orElse(false);

                if (!isAppellantFtpaApplication) {
                    return false;
                }

                Optional<FtpaDecisionOutcomeType> ftpaAppellantDecisionOutcomeType = asylumCase
                    .read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

                if (ftpaAppellantDecisionOutcomeType.isEmpty()) {
                    throw new RequiredFieldMissingException("FTPA decision not found");
                }

                if (List.of(FTPA_GRANTED, FTPA_PARTIALLY_GRANTED, FTPA_REFUSED).contains(ftpaAppellantDecisionOutcomeType.get())) {

                    return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && callback.getEvent() == Event.RESIDENT_JUDGE_FTPA_DECISION
                        && isInternalCase(asylumCase);
                } else {
                    return false;
                }
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalUpdateHearingAdjustmentsNotificationHandler(
        @Qualifier("internalUpdateHearingAdjustmentsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == UPDATE_HEARING_ADJUSTMENTS
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRequestResponseAmendNotificationHandler(
        @Qualifier("internalRequestResponseAmendNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == REQUEST_RESPONSE_AMEND
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalUploadAdditionalEvidenceNotificationHandler(
        @Qualifier("internalUploadAdditionalEvidenceNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == UPLOAD_ADDITIONAL_EVIDENCE
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalUploadAddendumEvidenceAdminNotificationHandler(
        @Qualifier("internalUploadAddendumEvidenceNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isAppellantRespondent = asylumCase.read(IS_APPELLANT_RESPONDENT, String.class)
                    .map(value -> value.equals(IS_APPELLANT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase)
                    && isAppellantRespondent;

            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalHomeOfficeUploadAdditionalAddendumEvidenceNotificationHandler(
        @Qualifier("internalHomeOfficeUploadAdditionalAddendumEvidenceNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && List.of(UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE, UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE).contains(callback.getEvent())
                    && isInternalCase(asylumCase)
                    && isAppellantInDetention(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalLegalOfficerUploadAddendumEvidenceNotificationHandler(
        @Qualifier("internalLegalOfficerUploadAddendumEvidenceNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                Optional<IdValue<DocumentWithMetadata>> latestAddendum = getLatestAddendumEvidenceDocument(asylumCase);
                if (latestAddendum.isEmpty()) {
                    return false;
                }


                final String legalOfficerAddendumUploadedByLabel = "TCW";
                final String legalOfficerAddendumUploadSuppliedByLabel = "The respondent";

                DocumentWithMetadata addendum = latestAddendum.get().getValue();

                if (addendum.getSuppliedBy() == null || addendum.getUploadedBy() == null) {
                    return false;
                }

                if (!addendum.getUploadedBy().equals(legalOfficerAddendumUploadedByLabel) || !addendum.getSuppliedBy().equals(legalOfficerAddendumUploadSuppliedByLabel)) {
                    return false;
                }

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(UPLOAD_ADDENDUM_EVIDENCE)
                    && isInternalCase(asylumCase);

            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalLegalOfficerUploadAdditionalAddendumEvidenceNotificationHandler(
        @Qualifier("internalLegalOfficerUploadAdditionalAddendumEvidenceNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(UPLOAD_ADDENDUM_EVIDENCE)
                    && isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> applyForCostsNotificationHandler(
        @Qualifier("applyForCostsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(APPLY_FOR_COSTS)
                    && !isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideCostsNotificationHandler(
        @Qualifier("decideCostsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(DECIDE_COSTS_APPLICATION)
                    && !isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> notificationsTurnedOnAppellantAndLegalRepNotificationHandler(
        @Qualifier("notificationsTurnedOnAppellantAndLegalRepNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == TURN_ON_NOTIFICATIONS
                    && isInternalCase(asylumCase)
                    && isLegalRepEjp(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondToCostsRespondentNotificationHandler(
        @Qualifier("respondToCostsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(RESPOND_TO_COSTS)
                    && !isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> additionalEvidenceSubmittedOtherPartyHandler(
        @Qualifier("additionalEvidenceSubmittedOtherPartyGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(ADD_EVIDENCE_FOR_COSTS)
                    && !isInternalCase(asylumCase);

            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> additionalEvidenceSubmittedSubmitterHandler(
        @Qualifier("additionalEvidenceSubmittedSubmitterGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(ADD_EVIDENCE_FOR_COSTS)
                    && !isInternalCase(asylumCase);

            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> considerMakingCostOrderNotificationHandler(
        @Qualifier("considerMakingCostOrderNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent().equals(CONSIDER_MAKING_COSTS_ORDER)
                    && !isInternalCase(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipDisposeUnderRule31Or32AppelantNotificationHandler(
        @Qualifier("aipDisposeUnderRule31Or32AppelantNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                    && isAipJourney(asylumCase)
                    && isFtpaDecisionOutcomeTypeUnderRule31OrRule32(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    private boolean isFtpaDecisionOutcomeTypeUnderRule31OrRule32(AsylumCase asylumCase) {
        List<String> rule31And32 = List.of(
            FtpaDecisionOutcomeType.FTPA_REMADE31.toString(),
            FtpaDecisionOutcomeType.FTPA_REMADE32.toString()
        );

        final ApplicantType ftpaApplicantType = retrieveApplicantType(asylumCase);

        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = retrieveFtpaDecisionApplicantType(asylumCase, ftpaApplicantType);

        return ftpaDecisionOutcomeType
            .map(decision -> rule31And32.contains(decision.toString()))
            .orElse(false);
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideFtpaApplicationLrHoRule31OrRule32NotificationHandler(
        @Qualifier("decideFtpaApplicationLrHoRule31OrRule32NotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                    && isFtpaDecisionOutcomeTypeUnderRule31OrRule32(asylumCase)
                    && isDlrmSetAsideEnabled(asylumCase)
                    && !isAipJourney(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipReheardUnderRule35AppelantNotificationHandler(
        @Qualifier("aipReheardUnderRule35AppelantNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                    && isAipJourney(asylumCase)
                    && isDlrmSetAsideEnabled(asylumCase)
                    && isReheard35DecisionOutcome(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updateTribunalDecisionRule31NotificationHandler(
        @Qualifier("updateTribunalDecisionRule31NotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                    && !isAipJourney(asylumCase)
                    && isRule31ReasonUpdatingDecision(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updateTribunalDecisionRule32NotificationHandler(
        @Qualifier("updateTribunalDecisionRule32NotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                    && !isAipJourney(asylumCase)
                    && isRule32ReasonUpdatingDecision(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updateTribunalDecisionRule32AipNotificationHandler(
        @Qualifier("updateTribunalDecisionRule32AipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                    && isAipJourney(asylumCase)
                    && isRule32ReasonUpdatingDecision(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantSubmittedWithRemissionRequestNotificationHandler(
        @Qualifier("appellantSubmittedWithRemissionRequestNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                RemissionOption remissionOption = asylumCase
                    .read(REMISSION_OPTION, RemissionOption.class).orElse(RemissionOption.NO_REMISSION);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isAipJourney(asylumCase)
                    && isDlrmFeeRemissionEnabled(asylumCase)
                    && remissionOption != RemissionOption.NO_REMISSION;
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> decideFtpaApplicationReheardUnderRule35NotificationHandler(
        @Qualifier("decideFtpaApplicationReheardUnderRule35NotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                    && isDlrmSetAsideEnabled(asylumCase)
                    && isReheard35DecisionOutcome(asylumCase)
                    && !isAipJourney(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppellantRecordRemissionDecisionNotificationHandler(
        @Qualifier("aipAppellantRecordRemissionDecisionNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                    && isAipJourney(asylumCase)
                    && isDlrmFeeRemissionEnabled(asylumCase)
                    && isEaHuEuAppeal(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppellantRecordRemissionDecisionPaNotificationHandler(
        @Qualifier("aipAppellantRecordRemissionDecisionPaNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                    && isAipJourney(asylumCase)
                    && isDlrmFeeRemissionEnabled(asylumCase)
                    && isPaAppeal(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantSubmittedWithRemissionMarkAppealAsPaidNotificationHandler(
        @Qualifier("appellantSubmittedWithRemissionMarkAppealAsPaidNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_APPEAL_PAID
                    && isAipJourney(asylumCase)
                    && isDlrmFeeRemissionEnabled(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> updateTribunalDecisionRule31AipNotificationHandler(
        @Qualifier("updateTribunalDecisionRule31AipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                    && isAipJourney(asylumCase)
                    && isRule31ReasonUpdatingDecision(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAppealAsRemittedNotificationHandler(
        @Qualifier("markAppealAsRemittedNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_APPEAL_AS_REMITTED
                    && isNotInternalOrIsInternalWithLegalRepresentation(asylumCase));
            }, notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> markAppealAsRemittedNonDetainedNotificationHandler(
        @Qualifier("markAppealAsRemittedNonDetainedNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MARK_APPEAL_AS_REMITTED
                    && isInternalCase(asylumCase)
                    && inCountryAppeal(asylumCase)
                    && !isAppellantInDetention(asylumCase));
            }, notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealWithExemptionAppellantLetterNotificationHandler(
        @Qualifier("internalSubmitAppealWithExemptionAppellantLetterNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRpOrDcAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == RP || type == DC).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && isRpOrDcAppealType
                    && !isAppellantInDetention(asylumCase)
                    && !isSubmissionOutOfTime(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealWithFeeAppellantLetterNotificationHandler(
        @Qualifier("internalSubmitAppealWithFeeAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaymentPending = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).map(status -> status == PAYMENT_PENDING).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && !isAriaMigrated(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && !isSubmissionOutOfTime(asylumCase)
                    && isPaymentPending
                    && hasAppellantAddressInCountryOrOutOfCountry(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalProgressMigratedCaseWithFeeAppellantLetterNotificationHandler(
        @Qualifier("internalProgressMigratedCaseWithFeeAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaymentPending = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).map(status -> status == PAYMENT_PENDING).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.PROGRESS_MIGRATED_CASE
                    && isInternalCase(asylumCase)
                    && isAriaMigrated(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && !isSubmissionOutOfTime(asylumCase)
                    && isPaymentPending
                    && hasAppellantAddressInCountryOrOutOfCountry(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealOnTimeWithRemissionAppellantLetterNotificationHandler(
        @Qualifier("internalSubmitAppealOnTimeWithRemissionAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                YesOrNo appellantHasFixedAddress = asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class).orElse(NO);

                RemissionType remissionType = asylumCase
                    .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);

                boolean isRemissionPresent = Arrays.asList(
                    HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isRemissionPresent
                    && !isSubmissionOutOfTime(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealOutOfTimeWithRemissionAppellantLetterNotificationHandler(
        @Qualifier("internalSubmitAppealOutOfTimeWithRemissionAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                RemissionType remissionType = asylumCase
                    .read(REMISSION_TYPE, RemissionType.class).orElse(NO_REMISSION);

                boolean isRemissionPresent = Arrays.asList(
                    HO_WAIVER_REMISSION, HELP_WITH_FEES, EXCEPTIONAL_CIRCUMSTANCES_REMISSION).contains(remissionType);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isRemissionPresent
                    && isSubmissionOutOfTime(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalHomeOfficeDirectedToReviewAppealAipNotificationHandler(
        @Qualifier("internalHomeOfficeDirectedToReviewAppealAipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == REQUEST_RESPONDENT_REVIEW
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> appellantInternalHomeOfficeApplyForFtpaNonDetainedOrOocAipNotificationHandler(
        @Qualifier("appellantInternalHomeOfficeApplyForFtpaLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == APPLY_FOR_FTPA_RESPONDENT
                    && isInternalCase(asylumCase)
                    && (!isAppellantInDetention(asylumCase) || !inCountryAppeal(asylumCase));

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSubmitAppealOutOfTimeWithExemptionAppellantLetterNotificationHandler(
        @Qualifier("internalSubmitAppealOutOfTimeWithExemptionAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRpAndDcAppealType = asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .map(type -> type == RP || type == DC).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && isInternalCase(asylumCase)
                    && isRpAndDcAppealType
                    && !isAppellantInDetention(asylumCase)
                    && isSubmissionOutOfTime(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalManageFeeUpdateLetterNotificationHandler(
        @Qualifier("internalManageFeeUpdateLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == MANAGE_FEE_UPDATE
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalEndAppealAppellantLetterNotificationHandler(
        @Qualifier("internalEndAppealAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == END_APPEAL
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && hasAppellantAddressInCountryOrOutOfCountry(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalHomeOfficeUploadBundleAipNotificationHandler(
        @Qualifier("internalHomeOfficeUploadBundleAipNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == REQUEST_RESPONDENT_EVIDENCE
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalDecisionWithoutHearingAppellantLetterNotificationHandler(
        @Qualifier("internalDecisionWithoutHearingAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == DECISION_WITHOUT_HEARING
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalNonStandardDirectionAppellantLetterNotificationHandler(
        @Qualifier("internalNonStandardDirectionAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators,
        DirectionFinder directionFinder) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                    && isInternalNonStdDirectionWithParty(asylumCase, Parties.APPELLANT, directionFinder)
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalEndAppealAutomaticallyAppellantLetterNotificationHandler(
        @Qualifier("internalEndAppealAutomaticallyAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                YesOrNo appellantHasFixedAddress = asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class).orElse(NO);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == END_APPEAL_AUTOMATICALLY
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalCaseListedAppellantLetterNotificationHandler(
        @Qualifier("internalCaseListedAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == LIST_CASE
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && hasAppellantAddressInCountryOrOutOfCountry(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalReinstateAppealAppellantLetterNotificationHandler(
        @Qualifier("internalReinstateAppealAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == REINSTATE_APPEAL
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internaLateRemissionRefusedLetterNotificationHandler(
        @Qualifier("internalLateRemissionRefusedLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRejected = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> REJECTED == decision)
                    .orElse(false);

                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == RECORD_REMISSION_DECISION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isRejected
                    && lateRemissionType.isPresent();

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalCaseAdjournedWithoutTimeLetterNotificationHandler(
        @Qualifier("internalCaseAdjournedWithoutTimeLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == ADJOURN_HEARING_WITHOUT_DATE
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);

            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRemissionPartiallyGrantedOrRefusedLetterNotificationHandler(
        @Qualifier("internalRemissionPartiallyGrantedOrRefusedLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRemissionPartiallyApprovedOrRejected = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> PARTIALLY_APPROVED == decision || REJECTED == decision)
                    .orElse(false);

                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == RECORD_REMISSION_DECISION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isRemissionPartiallyApprovedOrRejected
                    && lateRemissionType.isEmpty();
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalCaseDisposeUnderRule31Or32AppelantLetterHandler(
        @Qualifier("internalCaseDisposeUnderRule31Or32AppelantLetterGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_FTPA_APPLICATION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isFtpaDecisionOutcomeTypeUnderRule31OrRule32(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalCaseDecideApplicationByAppelantLetterHandler(
        @Qualifier("internalDecideApplicationAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == DECIDE_AN_APPLICATION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isApplicationCreatedByAdmin(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalMarkAsRemittedAppellantLetterNotificationHandler(
        @Qualifier("internalMarkAsRemittedAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == MARK_APPEAL_AS_REMITTED
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalUpdateTribunalDecisionRule31NotificationHandler(
        @Qualifier("internalUpdateTribunalDecisionRule31LetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.UPDATE_TRIBUNAL_DECISION
                    && isInternalCase(asylumCase)
                    && isRule31ReasonUpdatingDecision(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> generateHearingBundleNonDetainedOrOocNotificationHandler(
        @Qualifier("generateHearingBundleNonDetainedOrOocNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == GENERATE_HEARING_BUNDLE
                    && isInternalCase(asylumCase)
                    && ((inCountryAppeal(asylumCase) && !isAppellantInDetention(asylumCase)) || !inCountryAppeal(asylumCase));
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRespondentApplicationDecidedLetterNotificationHandler(
        @Qualifier("internalRespondentApplicationDecidedLetterGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.DECIDE_AN_APPLICATION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isApplicationCreatedByRespondent(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalOutOfTimeDecisionAppellantLetterNotificationHandler(
        @Qualifier("internalOutOfTimeDecisionAppellantLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == RECORD_OUT_OF_TIME_DECISION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && hasAppellantAddressInCountryOrOutOfCountry(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalEditAppealAfterSubmitLetterNotificationHandler(
        @Qualifier("internalEditAppealAfterSubmitLetterNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRemissionGrantedOotLetterNotificationHandler(
        @Qualifier("internalRemissionGrantedOotLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                boolean isOutOfTimeAppeal = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == YES).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == RECORD_REMISSION_DECISION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isApproved
                    && isOutOfTimeAppeal;
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalLateRemissionGrantedLetterNotificationHandler(
        @Qualifier("internalLateRemissionGrantedLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isRemissionPartiallyApprovedOrApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> PARTIALLY_APPROVED == decision || APPROVED == decision)
                    .orElse(false);

                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == RECORD_REMISSION_DECISION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isRemissionPartiallyApprovedOrApproved
                    && lateRemissionType.isPresent();
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRemissionGrantedInTimeLetterNotificationHandler(
        @Qualifier("internalRemissionGrantedInTimeLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isApproved = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                    .map(decision -> APPROVED == decision)
                    .orElse(false);

                boolean isOutOfTimeAppeal = asylumCase
                    .read(AsylumCaseDefinition.SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                    .map(outOfTime -> outOfTime == YES).orElse(false);

                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == RECORD_REMISSION_DECISION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isApproved
                    && !isOutOfTimeAppeal
                    && lateRemissionType.isEmpty();
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalEditCaseListingNotificationHandler(
        @Qualifier("editCaseListingInternalNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
                return callback.getEvent() == Event.EDIT_CASE_LISTING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && hasAppellantAddressInCountryOrOutOfCountry(asylumCase);
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalSendPaymentReminderEmailNotificationHandler(
        @Qualifier("sendPaymentReminderInternalNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaymentPending = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).map(status -> status == PAYMENT_PENDING).orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == SEND_PAYMENT_REMINDER_NOTIFICATION
                    && isInternalCase(asylumCase)
                    && !isAppellantInDetention(asylumCase)
                    && isPaymentPending;
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipAppellantRecordRefundDecisionNotificationHandler(
        @Qualifier("aipAppellantRecordRefundDecisionNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_REMISSION_DECISION
                    && isAipJourney(asylumCase)
                    && isDlrmFeeRefundEnabled(asylumCase)
                    && isLateRemissionRequest(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> legalRepRemissionPaymentReminderEmailNotificationHandler(
        @Qualifier("legalRepRemissionPaymentReminderEmailNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_REMISSION_REMINDER
                    && isRemissionRejectedOrPartiallyApproved(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipManageFeeUpdatePaymentInstructedNotificationHandler(
        @Qualifier("aipManageFeeUpdatePaymentInstructedNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isPaymentInstructed = asylumCase.read(FEE_UPDATE_TRIBUNAL_ACTION, FeeTribunalAction.class)
                    .map(action -> action.equals(ADDITIONAL_PAYMENT))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.MANAGE_FEE_UPDATE
                    && isPaymentInstructed
                    && isAipJourney(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> legalRepRefundConfirmationPersonalisationEmailNotificationHandler(
        @Qualifier("legalRepRefundConfirmationPersonalisationEmailNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REFUND_CONFIRMATION
                    && !isAipJourney(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> aipRefundConfirmationNotificationHandler(
        @Qualifier("aipRefundConfirmationNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REFUND_CONFIRMATION
                    && isAipJourney(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> internalRefundConfirmationLetterNotificationHandler(
        @Qualifier("internalRefundConfirmationLetterNotificationGenerator")
        List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.REFUND_CONFIRMATION
                    && isInternalCase(asylumCase);
            },
            notificationGenerators,
            getErrorHandler()
        );
    }

    private boolean isDlrmSetAsideEnabled(AsylumCase asylumCase) {
        return asylumCase.read(IS_DLRM_SET_ASIDE_ENABLED, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
    }

    private boolean isDlrmFeeRemissionEnabled(AsylumCase asylumCase) {
        return asylumCase.read(IS_DLRM_FEE_REMISSION_ENABLED, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
    }

    private ApplicantType retrieveApplicantType(AsylumCase asylumCase) {
        return asylumCase.read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .orElseThrow(() -> new IllegalStateException("FtpaApplicantType is not present"));
    }

    private Optional<FtpaDecisionOutcomeType> retrieveFtpaDecisionApplicantType(AsylumCase asylumCase, ApplicantType ftpaApplicantType) {
        return asylumCase.read(ftpaApplicantType.equals(APPELLANT)
            ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE
            : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);
    }

    private boolean isReheard35DecisionOutcome(AsylumCase asylumCase) {
        final ApplicantType ftpaApplicantType = retrieveApplicantType(asylumCase);

        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = retrieveFtpaDecisionApplicantType(asylumCase, ftpaApplicantType);

        return ftpaDecisionOutcomeType
            .map(decision -> decision.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD35.toString()))
            .orElse(false);
    }

    private boolean isRule31ReasonUpdatingDecision(AsylumCase asylumCase) {

        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class)
            .map(reason -> reason.equals("underRule31")).orElse(false);
    }

    private boolean isRule32ReasonUpdatingDecision(AsylumCase asylumCase) {

        return asylumCase.read(UPDATE_TRIBUNAL_DECISION_LIST, String.class)
            .map(reason -> reason.equals("underRule32")).orElse(false);
    }

    private boolean isInternalNonStdDirectionWithParty(AsylumCase asylumCase, Parties party, DirectionFinder directionFinder) {
        return isInternalCase(asylumCase)
            && directionFinder
            .findFirst(asylumCase, DirectionTag.NONE)
            .map(direction -> direction.getParties().equals(party))
            .orElse(false);
    }

    private boolean isNotInternalOrIsInternalWithLegalRepresentation(AsylumCase asylumCase) {
        return (!isInternalCase(asylumCase) ||
            isInternalCase(asylumCase) && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase));
    }

    private boolean isInternalWithoutLegalRepresentation(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && !hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
    }

    private boolean isRemissionRejectedOrPartiallyApproved(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .map(decision -> decision == RemissionDecision.REJECTED || decision == RemissionDecision.PARTIALLY_APPROVED)
            .orElse(false);
    }

    private boolean isLateRemissionRequest(AsylumCase asylumCase) {
        return asylumCase.read(IS_LATE_REMISSION_REQUEST, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
    }

    private boolean isDlrmFeeRefundEnabled(AsylumCase asylumCase) {
        return asylumCase.read(IS_DLRM_FEE_REFUND_ENABLED, YesOrNo.class)
            .map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
    }
}
