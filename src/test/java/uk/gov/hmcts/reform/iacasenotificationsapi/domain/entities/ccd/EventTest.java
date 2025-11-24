package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;

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
        assertEquals("payForAppeal", Event.PAY_FOR_APPEAL.toString());
        assertEquals("sendDirection", Event.SEND_DIRECTION.toString());
        assertEquals("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString());
        assertEquals("uploadRespondentEvidence", Event.UPLOAD_RESPONDENT_EVIDENCE.toString());
        assertEquals("buildCase", Event.BUILD_CASE.toString());
        assertEquals("submitCase", Event.SUBMIT_CASE.toString());
        assertEquals("requestCaseEdit", Event.REQUEST_CASE_EDIT.toString());
        assertEquals("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("addAppealResponse", Event.ADD_APPEAL_RESPONSE.toString());
        assertEquals("requestHearingRequirements", Event.REQUEST_HEARING_REQUIREMENTS.toString());
        assertEquals("reviewHearingRequirements", Event.REVIEW_HEARING_REQUIREMENTS.toString());
        assertEquals("listCaseWithoutHearingRequirements", Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS.toString());
        assertEquals("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString());
        assertEquals("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS.toString());
        assertEquals("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString());
        assertEquals("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE.toString());
        assertEquals("uploadAdditionalEvidenceHomeOffice", Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("createCaseSummary", Event.CREATE_CASE_SUMMARY.toString());
        assertEquals("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE.toString());
        assertEquals("asyncStitchingComplete", Event.ASYNC_STITCHING_COMPLETE.toString());
        assertEquals("customiseHearingBundle", CUSTOMISE_HEARING_BUNDLE.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("recordApplication", Event.RECORD_APPLICATION.toString());
        assertEquals("requestCaseBuilding", Event.REQUEST_CASE_BUILDING.toString());
        assertEquals("forceRequestCaseBuilding", Event.FORCE_REQUEST_CASE_BUILDING.toString());
        assertEquals("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("uploadAddendumEvidence", Event.UPLOAD_ADDENDUM_EVIDENCE.toString());
        assertEquals("uploadAddendumEvidenceLegalRep", Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP.toString());
        assertEquals("uploadAddendumEvidenceHomeOffice", Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("uploadAddendumEvidenceAdminOfficer", Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER.toString());
        assertEquals("requestResponseReview", Event.REQUEST_RESPONSE_REVIEW.toString());
        assertEquals("decisionWithoutHearing", Event.DECISION_WITHOUT_HEARING.toString());
        assertEquals("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString());
        assertEquals("requestReasonsForAppeal", Event.REQUEST_REASONS_FOR_APPEAL.toString());
        assertEquals("submitReasonsForAppeal", Event.SUBMIT_REASONS_FOR_APPEAL.toString());
        assertEquals("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS.toString());
        assertEquals("removeAppealFromOnline", Event.REMOVE_APPEAL_FROM_ONLINE.toString());
        assertEquals("changeHearingCentre", Event.CHANGE_HEARING_CENTRE.toString());
        assertEquals("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString());
        assertEquals("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("submitTimeExtension", Event.SUBMIT_TIME_EXTENSION.toString());
        assertEquals("reviewTimeExtension", Event.REVIEW_TIME_EXTENSION.toString());
        assertEquals("sendDirectionWithQuestions", Event.SEND_DIRECTION_WITH_QUESTIONS.toString());
        assertEquals("submitClarifyingQuestionAnswers", Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS.toString());
        assertEquals("forceCaseToCaseUnderReview", Event.FORCE_CASE_TO_CASE_UNDER_REVIEW.toString());
        assertEquals("forceCaseToSubmitHearingRequirements", Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS.toString());
        assertEquals("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString());
        assertEquals("restoreStateFromAdjourn", Event.RESTORE_STATE_FROM_ADJOURN.toString());
        assertEquals("requestCmaRequirements", Event.REQUEST_CMA_REQUIREMENTS.toString());
        assertEquals("submitCmaRequirements", Event.SUBMIT_CMA_REQUIREMENTS.toString());
        assertEquals("listCma", Event.LIST_CMA.toString());
        assertEquals("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT.toString());
        assertEquals("linkAppeal", Event.LINK_APPEAL.toString());
        assertEquals("unlinkAppeal", Event.UNLINK_APPEAL.toString());
        assertEquals("leadershipJudgeFtpaDecision", Event.LEADERSHIP_JUDGE_FTPA_DECISION.toString());
        assertEquals("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION.toString());
        assertEquals("editDocuments", Event.EDIT_DOCUMENTS.toString());
        assertEquals("paymentAppeal", Event.PAYMENT_APPEAL.toString());
        assertEquals("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND.toString());
        assertEquals("markAppealPaid", Event.MARK_APPEAL_PAID.toString());
        assertEquals("reinstateAppeal", REINSTATE_APPEAL.toString());
        assertEquals("makeAnApplication", Event.MAKE_AN_APPLICATION.toString());
        assertEquals("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString());
        assertEquals("requestNewHearingRequirements", Event.REQUEST_NEW_HEARING_REQUIREMENTS.toString());
        assertEquals("recordRemissionDecision", RECORD_REMISSION_DECISION.toString());
        assertEquals("nocRequest", NOC_REQUEST.toString());
        assertEquals("removeRepresentation", REMOVE_REPRESENTATION.toString());
        assertEquals("removeLegalRepresentative", REMOVE_LEGAL_REPRESENTATIVE.toString());
        assertEquals("requestFeeRemission", REQUEST_FEE_REMISSION.toString());
        assertEquals("manageFeeUpdate", MANAGE_FEE_UPDATE.toString());
        assertEquals("recordOutOfTimeDecision", RECORD_OUT_OF_TIME_DECISION.toString());
        assertEquals("editPaymentMethod", EDIT_PAYMENT_METHOD.toString());
        assertEquals("submitApplication", SUBMIT_APPLICATION.toString());
        assertEquals("uploadBailSummary", UPLOAD_BAIL_SUMMARY.toString());
        assertEquals("uploadSignedDecisionNotice", UPLOAD_SIGNED_DECISION_NOTICE.toString());
        assertEquals("endApplication", END_APPLICATION.toString());
        assertEquals("uploadDocuments", UPLOAD_DOCUMENTS.toString());
        assertEquals("editBailDocuments", EDIT_BAIL_DOCUMENTS.toString());
        assertEquals("changeBailDirectionDueDate", CHANGE_BAIL_DIRECTION_DUE_DATE.toString());
        assertEquals("sendBailDirection", SEND_BAIL_DIRECTION.toString());
        assertEquals("makeNewApplication", MAKE_NEW_APPLICATION.toString());
        assertEquals("editBailApplicationAfterSubmit", EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString());
        assertEquals("stopLegalRepresenting", STOP_LEGAL_REPRESENTING.toString());
        assertEquals("nocRequestBail", NOC_REQUEST_BAIL.toString());
        assertEquals("endAppealAutomatically", END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("updatePaymentStatus", UPDATE_PAYMENT_STATUS.toString());
        assertEquals("createCaseLink", Event.CREATE_CASE_LINK.toString());
        assertEquals("maintainCaseLinks", Event.MAINTAIN_CASE_LINKS.toString());
        assertEquals("createBailCaseLink", CREATE_BAIL_CASE_LINK.toString());
        assertEquals("maintainBailCaseLinks", Event.MAINTAIN_BAIL_CASE_LINKS.toString());
        assertEquals("adaSuitabilityReview", ADA_SUITABILITY_REVIEW.toString());
        assertEquals("transferOutOfAda", TRANSFER_OUT_OF_ADA.toString());
        assertEquals("markAppealAsAda", MARK_APPEAL_AS_ADA.toString());
        assertEquals("markAsReadyForUtTransfer", MARK_AS_READY_FOR_UT_TRANSFER.toString());
        assertEquals("updateDetentionLocation", UPDATE_DETENTION_LOCATION.toString());
        assertEquals("updateHearingAdjustments", UPDATE_HEARING_ADJUSTMENTS.toString());
        assertEquals("applyForCosts", APPLY_FOR_COSTS.toString());
        assertEquals("turnOnNotifications", TURN_ON_NOTIFICATIONS.toString());
        assertEquals("respondToCosts", RESPOND_TO_COSTS.toString());
        assertEquals("decideFtpaApplication", Event.DECIDE_FTPA_APPLICATION.toString());
        assertEquals("updateTribunalDecision", Event.UPDATE_TRIBUNAL_DECISION.toString());
        assertEquals("caseListing", CASE_LISTING.toString());
        assertEquals("markAppealAsRemitted", Event.MARK_APPEAL_AS_REMITTED.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("addEvidenceForCosts", ADD_EVIDENCE_FOR_COSTS.toString());
        assertEquals("considerMakingCostsOrder", CONSIDER_MAKING_COSTS_ORDER.toString());
        assertEquals("decideCostsApplication", DECIDE_COSTS_APPLICATION.toString());
        assertEquals("recordTheDecision", RECORD_THE_DECISION.toString());
        assertEquals("changeTribunalCentre", CHANGE_TRIBUNAL_CENTRE.toString());
        assertEquals("sendPaymentReminderNotification", SEND_PAYMENT_REMINDER_NOTIFICATION.toString());
        assertEquals("progressMigratedCase", PROGRESS_MIGRATED_CASE.toString());
        assertEquals("recordRemissionReminder", Event.RECORD_REMISSION_REMINDER.toString());
        assertEquals("refundConfirmation", Event.REFUND_CONFIRMATION.toString());
        assertEquals("hearingCancelled", HEARING_CANCELLED.toString());
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
        assertEquals(128, Event.values().length);
    }
}
