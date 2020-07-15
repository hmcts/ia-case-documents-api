package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DIRECTION_EDIT_PARTIES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.REP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.BUILD_CASE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit.NotificationHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationGenerator;

@Configuration
public class NotificationHandlerConfiguration {

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

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DECISION_AND_REASONS,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> respondentChangeDirectionDueDateNotificationHandler(
        @Qualifier("respondentChangeDirectionDueDateNotificationGenerator") List<NotificationGenerator> notificationGenerators,
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
                       && isRespondent;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> legalRepChangeDirectionDueDateNotificationHandler(
        @Qualifier("legalRepChangeDirectionDueDateNotificationGenerator") List<NotificationGenerator> notificationGenerators,
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
        @Qualifier("bothPartiesChangeDirectionDueDateNotificationGenerator") List<NotificationGenerator> notificationGenerators,
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
        @Qualifier("requestHearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            notificationGenerators
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
                    && isAppealOnTime;
            }, notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submitAppealRepNotificationHandler(
        @Qualifier("submitAppealRepNotificationGenerator") List<NotificationGenerator> notificationGenerators
    ) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL
                    && callback.getCaseDetails().getCaseData()
                    .read(JOURNEY_TYPE, JourneyType.class)
                    .map(type -> type == REP).orElse(true),
            notificationGenerators
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

        return new NotificationHandler(
            (callbackStage, callback) -> callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SUBMIT_APPEAL, notificationGenerators
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
                    && isOutOfTimeAppeal;
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
        @Qualifier("submitReasonsForAppealAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

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

        return new NotificationHandler(
            (callbackStage, callback) -> {
                AsylumCase asylumCase =
                    callback
                        .getCaseDetails()
                        .getCaseData();

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.SEND_DIRECTION
                       && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.RESPONDENT);
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
                    && isValidUserDirection(directionFinder, asylumCase, DirectionTag.NONE, Parties.LEGAL_REPRESENTATIVE);
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
        @Qualifier("recordApplicationNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) -> {

                AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

                boolean isApplicationRefused = asylumCase
                    .read(AsylumCaseDefinition.APPLICATION_DECISION, String.class)
                    .map(decision -> decision.equals(ApplicationDecision.REFUSED.toString()))
                    .orElse(false);

                return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.RECORD_APPLICATION
                    && isApplicationRefused;
            },
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> editCaseListingNotificationHandler(
        @Qualifier("editCaseListingNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.EDIT_CASE_LISTING,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> uploadHomeOfficeAppealResponseNotificationHandler(
        @Qualifier("uploadHomeOfficeAppealResponseNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

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

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.GENERATE_HEARING_BUNDLE,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> submittedHearingRequirementsNotificationHandler(
        @Qualifier("submittedHearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS,
            notificationGenerators
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adjustedHearingRequirementsNotificationHandler(
        @Qualifier("adjustedHearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REVIEW_HEARING_REQUIREMENTS,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> withoutHearingRequirementsNotificationHandler(
        @Qualifier("withoutHearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

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
        @Qualifier("changeToHearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

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

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.APPLY_FOR_FTPA_APPELLANT,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> ftpaSubmittedLegNotificationHandler(
        @Qualifier("ftpaSubmittedRespondentNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

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

                final Optional<List<IdValue<TimeExtension>>> maybeTimeExtensions = asylumCase.read(AsylumCaseDefinition.TIME_EXTENSIONS);

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

                final Optional<List<IdValue<TimeExtension>>> maybeTimeExtensions = asylumCase.read(AsylumCaseDefinition.TIME_EXTENSIONS);

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
        @Qualifier("requestClarifyingQuestionsAipNotificationGenerator") List<NotificationGenerator> notificationGenerators
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
        @Qualifier("submitClarifyingQuestionAnswersNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

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
        @Qualifier("forceCaseProgressionToCaseUnderReviewNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> forceCaseToSubmitHearingRequirementsNotificationHandler(
        @Qualifier("forceCaseToSubmitHearingRequirementsNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

        return new NotificationHandler(
            (callbackStage, callback) ->
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && callback.getEvent() == Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS,
            notificationGenerator
        );
    }

    @Bean
    public PreSubmitCallbackHandler<AsylumCase> adjournHearingWithoutDateHandler(
        @Qualifier("adjournHearingWithoutDateNotificationGenerator") List<NotificationGenerator> notificationGenerator) {

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
        @Qualifier("submitCmaRequirementsAipNotificationGenerator") List<NotificationGenerator> notificationGenerators) {

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
}
