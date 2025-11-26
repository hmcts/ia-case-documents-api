package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

public class EventTest {

    @ParameterizedTest
    @EnumSource(value = Event.class, names = { "APPLY_FOR_FTPA_APPELLANT", "APPLY_FOR_FTPA_RESPONDENT" }, mode = EXCLUDE)
    public void has_correct_values(Event event) {
        assertEquals(convertToCamelCase(event.name()), event.toString());
    }

    @Test
    public void has_correct_values() {
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("payAndSubmitAppeal", Event.PAY_AND_SUBMIT_APPEAL.toString());
        assertEquals("sendDirection", Event.SEND_DIRECTION.toString());
        assertEquals("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString());
        assertEquals("uploadRespondentEvidence", Event.UPLOAD_RESPONDENT_EVIDENCE.toString());
        assertEquals("buildCase", Event.BUILD_CASE.toString());
        assertEquals("submitCase", Event.SUBMIT_CASE.toString());
        assertEquals("requestCaseEdit", Event.REQUEST_CASE_EDIT.toString());
        assertEquals("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("addAppealResponse", Event.ADD_APPEAL_RESPONSE.toString());
        assertEquals("requestHearingRequirements", Event.REQUEST_HEARING_REQUIREMENTS.toString());
        assertEquals("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS.toString());
        assertEquals("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString());
        assertEquals("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE.toString());
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("createCaseSummary", Event.CREATE_CASE_SUMMARY.toString());
        assertEquals("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE.toString());
        assertEquals("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE.toString());
        assertEquals("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString());
        assertEquals("customiseHearingBundle", Event.CUSTOMISE_HEARING_BUNDLE.toString());
        assertEquals("generateUpdatedHearingBundle", Event.GENERATE_UPDATED_HEARING_BUNDLE.toString());
        assertEquals("generateDecisionAndReasons", Event.GENERATE_DECISION_AND_REASONS.toString());
        assertEquals("adaSuitabilityReview", Event.ADA_SUITABILITY_REVIEW.toString());
        assertEquals("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("manageFeeUpdate", Event.MANAGE_FEE_UPDATE.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("endAppealAutomatically", Event.END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("submitCmaRequirements", Event.SUBMIT_CMA_REQUIREMENTS.toString());
        assertEquals("listCma", Event.LIST_CMA.toString());
        assertEquals("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT.toString());
        assertEquals("generateUpperTribunalBundle", Event.GENERATE_UPPER_TRIBUNAL_BUNDLE.toString());
        assertEquals("submitReasonsForAppeal", Event.SUBMIT_REASONS_FOR_APPEAL.toString());
        assertEquals("submitClarifyingQuestionAnswers", Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS.toString());
        assertEquals("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString());
        assertEquals("updateTribunalDecision", Event.UPDATE_TRIBUNAL_DECISION.toString());
        assertEquals("requestCaseBuilding", Event.REQUEST_CASE_BUILDING.toString());
        assertEquals("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("recordOutOfTimeDecision", Event.RECORD_OUT_OF_TIME_DECISION.toString());
        assertEquals("markAppealPaid", Event.MARK_APPEAL_PAID.toString());
        assertEquals("recordRemissionDecision", Event.RECORD_REMISSION_DECISION.toString());
        assertEquals("requestResponseReview", Event.REQUEST_RESPONSE_REVIEW.toString());
        assertEquals("markAppealAsAda", Event.MARK_APPEAL_AS_ADA.toString());
        assertEquals("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString());
        assertEquals("transferOutOfAda", Event.TRANSFER_OUT_OF_ADA.toString());
        assertEquals("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION.toString());
        assertEquals("maintainCaseLinks", Event.MAINTAIN_CASE_LINKS.toString());
        assertEquals("uploadAddendumEvidenceAdminOfficer", Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER.toString());
        assertEquals("changeHearingCentre", Event.CHANGE_HEARING_CENTRE.toString());
        assertEquals("createCaseLink", Event.CREATE_CASE_LINK.toString());
        assertEquals("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND.toString());
        assertEquals("uploadAdditionalEvidenceHomeOffice", Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("uploadAddendumEvidenceHomeOffice", Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("uploadAddendumEvidence", Event.UPLOAD_ADDENDUM_EVIDENCE.toString());
        assertEquals("reinstateAppeal", Event.REINSTATE_APPEAL.toString());
        assertEquals("decisionWithoutHearing", Event.DECISION_WITHOUT_HEARING.toString());
        assertEquals("markAppealAsRemitted", Event.MARK_APPEAL_AS_REMITTED.toString());
        assertEquals("submitApplication", Event.SUBMIT_APPLICATION.toString());
        assertEquals("recordTheDecision", Event.RECORD_THE_DECISION.toString());
        assertEquals("endApplication", Event.END_APPLICATION.toString());
        assertEquals("makeNewApplication", Event.MAKE_NEW_APPLICATION.toString());
        assertEquals("editBailApplicationAfterSubmit", Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString());
        assertEquals("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE.toString());
        assertEquals("caseListing", Event.CASE_LISTING.toString());
        assertEquals("updateHearingRequirements", Event.UPDATE_HEARING_REQUIREMENTS.toString());
        assertEquals("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS.toString());
        assertEquals("saveNotificationsToData", Event.SAVE_NOTIFICATIONS_TO_DATA.toString());
        assertEquals("decideFtpaApplication", Event.DECIDE_FTPA_APPLICATION.toString());
        assertEquals("paymentAppeal", Event.PAYMENT_APPEAL.toString());
        assertEquals("payForAppeal", Event.PAY_FOR_APPEAL.toString());
        assertEquals("updatePaymentStatus", Event.UPDATE_PAYMENT_STATUS.toString());
        assertEquals("generateServiceRequest", Event.GENERATE_SERVICE_REQUEST.toString());
        assertEquals("removeRepresentation", Event.REMOVE_REPRESENTATION.toString());
        assertEquals("removeLegalRepresentative", Event.REMOVE_LEGAL_REPRESENTATIVE.toString());
        assertEquals("uploadHomeOfficeBundle", Event.UPLOAD_HOME_OFFICE_BUNDLE.toString());
        assertEquals("reviewHearingRequirements", Event.REVIEW_HEARING_REQUIREMENTS.toString());
        assertEquals("listCaseWithoutHearingRequirements", Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.toString());
        assertEquals("recordApplication", Event.RECORD_APPLICATION.toString());
        assertEquals("forceRequestCaseBuilding", Event.FORCE_REQUEST_CASE_BUILDING.toString());
        assertEquals("uploadAddendumEvidenceLegalRep", Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP.toString());
        assertEquals("requestReasonsForAppeal", Event.REQUEST_REASONS_FOR_APPEAL.toString());
        assertEquals("removeAppealFromOnline", Event.REMOVE_APPEAL_FROM_ONLINE.toString());
        assertEquals("submitTimeExtension", Event.SUBMIT_TIME_EXTENSION.toString());
        assertEquals("reviewTimeExtension", Event.REVIEW_TIME_EXTENSION.toString());
        assertEquals("sendDirectionWithQuestions", Event.SEND_DIRECTION_WITH_QUESTIONS.toString());
        assertEquals("forceCaseToCaseUnderReview", Event.FORCE_CASE_TO_CASE_UNDER_REVIEW.toString());
        assertEquals("forceCaseToSubmitHearingRequirements", Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("hearingCancelled", Event.HEARING_CANCELLED.toString());
        assertEquals("restoreStateFromAdjourn", Event.RESTORE_STATE_FROM_ADJOURN.toString());
        assertEquals("requestCmaRequirements", Event.REQUEST_CMA_REQUIREMENTS.toString());
        assertEquals("linkAppeal", Event.LINK_APPEAL.toString());
        assertEquals("unlinkAppeal", Event.UNLINK_APPEAL.toString());
        assertEquals("editDocuments", Event.EDIT_DOCUMENTS.toString());
        assertEquals("leadershipJudgeFtpaDecision", Event.LEADERSHIP_JUDGE_FTPA_DECISION.toString());
        assertEquals("makeAnApplication", Event.MAKE_AN_APPLICATION.toString());
        assertEquals("requestNewHearingRequirements", Event.REQUEST_NEW_HEARING_REQUIREMENTS.toString());
        assertEquals("nocRequest", Event.NOC_REQUEST.toString());
        assertEquals("requestFeeRemission", Event.REQUEST_FEE_REMISSION.toString());
        assertEquals("editPaymentMethod", Event.EDIT_PAYMENT_METHOD.toString());
        assertEquals("removeDetainedStatus", Event.REMOVE_DETAINED_STATUS.toString());
        assertEquals("markAppealAsDetained", Event.MARK_APPEAL_AS_DETAINED.toString());
        assertEquals("markAsReadyForUtTransfer", Event.MARK_AS_READY_FOR_UT_TRANSFER.toString());
        assertEquals("updateDetentionLocation", Event.UPDATE_DETENTION_LOCATION.toString());
        assertEquals("turnOnNotifications", Event.TURN_ON_NOTIFICATIONS.toString());
        assertEquals("applyForCosts", Event.APPLY_FOR_COSTS.toString());
        assertEquals("respondToCosts", Event.RESPOND_TO_COSTS.toString());
        assertEquals("addEvidenceForCosts", Event.ADD_EVIDENCE_FOR_COSTS.toString());
        assertEquals("considerMakingCostsOrder", Event.CONSIDER_MAKING_COSTS_ORDER.toString());
        assertEquals("decideCostsApplication", Event.DECIDE_COSTS_APPLICATION.toString());
        assertEquals("recordRemissionReminder", Event.RECORD_REMISSION_REMINDER.toString());
        assertEquals("sendPaymentReminderNotification", Event.SEND_PAYMENT_REMINDER_NOTIFICATION.toString());
        assertEquals("progressMigratedCase", Event.PROGRESS_MIGRATED_CASE.toString());
        assertEquals("refundConfirmation", Event.REFUND_CONFIRMATION.toString());
        assertEquals("uploadBailSummary", Event.UPLOAD_BAIL_SUMMARY.toString());
        assertEquals("uploadDocuments", Event.UPLOAD_DOCUMENTS.toString());
        assertEquals("sendBailDirection", Event.SEND_BAIL_DIRECTION.toString());
        assertEquals("editBailDocuments", Event.EDIT_BAIL_DOCUMENTS.toString());
        assertEquals("changeBailDirectionDueDate", Event.CHANGE_BAIL_DIRECTION_DUE_DATE.toString());
        assertEquals("stopLegalRepresenting", Event.STOP_LEGAL_REPRESENTING.toString());
        assertEquals("nocRequestBail", Event.NOC_REQUEST_BAIL.toString());
        assertEquals("createBailCaseLink", Event.CREATE_BAIL_CASE_LINK.toString());
        assertEquals("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS.toString());
        assertEquals("sendUploadBailSummaryDirection", Event.SEND_UPLOAD_BAIL_SUMMARY_DIRECTION.toString());
        assertEquals("forceCaseToHearing", Event.FORCE_CASE_TO_HEARING.toString());
        assertEquals("changeTribunalCentre", Event.CHANGE_TRIBUNAL_CENTRE.toString());
        assertEquals("startApplication", Event.START_APPLICATION.toString());
        assertEquals("editBailApplication", Event.EDIT_BAIL_APPLICATION.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    public void exclusions_have_correct_values() {
        assertEquals("applyForFTPAAppellant", APPLY_FOR_FTPA_APPELLANT.toString());
        assertEquals("applyForFTPARespondent", APPLY_FOR_FTPA_RESPONDENT.toString());
    }

    private static String convertToCamelCase(String snakeCase) {
        String[] words = snakeCase.split("_");
        StringBuilder camelCase = new StringBuilder();
        camelCase.append(words[0].toLowerCase());
        for (int i = 1; i < words.length; i++) {
            camelCase.append(words[i].substring(0, 1).toUpperCase());
            camelCase.append(words[i].substring(1).toLowerCase());
        }
        return camelCase.toString();
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(134, Event.values().length);
    }
}
