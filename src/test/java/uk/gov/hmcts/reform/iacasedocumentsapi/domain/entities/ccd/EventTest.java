package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS.toString());
        assertEquals("updateHearingRequirements", Event.UPDATE_HEARING_REQUIREMENTS.toString());
        assertEquals("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString());
        assertEquals("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE.toString());
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("createCaseSummary", Event.CREATE_CASE_SUMMARY.toString());
        assertEquals("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE.toString());
        assertEquals("customiseHearingBundle", Event.CUSTOMISE_HEARING_BUNDLE.toString());
        assertEquals("generateUpdatedHearingBundle", Event.GENERATE_UPDATED_HEARING_BUNDLE.toString());
        assertEquals("generateDecisionAndReasons", Event.GENERATE_DECISION_AND_REASONS.toString());
        assertEquals("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("endAppealAutomatically", Event.END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("submitCmaRequirements", Event.SUBMIT_CMA_REQUIREMENTS.toString());
        assertEquals("listCma", Event.LIST_CMA.toString());
        assertEquals("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT.toString());
        assertEquals("generateUpperTribunalBundle", Event.GENERATE_UPPER_TRIBUNAL_BUNDLE.toString());
        assertEquals("submitReasonsForAppeal", Event.SUBMIT_REASONS_FOR_APPEAL.toString());
        assertEquals("submitClarifyingQuestionAnswers",Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS.toString());
        assertEquals("recordAdjournmentDetails", Event.RECORD_ADJOURNMENT_DETAILS.toString());
        assertEquals("updateTribunalDecision",Event.UPDATE_TRIBUNAL_DECISION.toString());
        assertEquals("submitApplication",Event.SUBMIT_APPLICATION.toString());
        assertEquals("recordTheDecision",Event.RECORD_THE_DECISION.toString());
        assertEquals("endApplication",Event.END_APPLICATION.toString());
        assertEquals("makeNewApplication",Event.MAKE_NEW_APPLICATION.toString());
        assertEquals("editBailApplicationAfterSubmit",Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT.toString());
        assertEquals("uploadSignedDecisionNotice", Event.UPLOAD_SIGNED_DECISION_NOTICE.toString());
        assertEquals("caseListing", Event.CASE_LISTING.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("requestCaseBuilding", Event.REQUEST_CASE_BUILDING.toString());
        assertEquals("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("recordOutOfTimeDecision", Event.RECORD_OUT_OF_TIME_DECISION.toString());
        assertEquals("markAppealPaid", Event.MARK_APPEAL_PAID.toString());
        assertEquals("recordRemissionDecision", Event.RECORD_REMISSION_DECISION.toString());
        assertEquals("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString());
        assertEquals("markAppealAsAda", Event.MARK_APPEAL_AS_ADA.toString());
        assertEquals("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString());
        assertEquals("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("transferOutOfAda", Event.TRANSFER_OUT_OF_ADA.toString());
        assertEquals("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString());
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
        assertEquals("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS.toString());
        assertEquals("saveNotificationsToData", Event.SAVE_NOTIFICATIONS_TO_DATA.toString());
        assertEquals("manageFeeUpdate", Event.MANAGE_FEE_UPDATE.toString());
        assertEquals("removeRepresentation", Event.REMOVE_REPRESENTATION.toString());
        assertEquals("removeLegalRepresentative", Event.REMOVE_LEGAL_REPRESENTATIVE.toString());
        assertEquals("decisionWithoutHearing", Event.DECISION_WITHOUT_HEARING.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(75, Event.values().length);
    }
}
