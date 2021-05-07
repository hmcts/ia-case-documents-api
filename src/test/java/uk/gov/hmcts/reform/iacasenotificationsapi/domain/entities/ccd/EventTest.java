package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event.*;

import org.junit.jupiter.api.Test;

public class EventTest {

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
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(78, Event.values().length);
    }
}
