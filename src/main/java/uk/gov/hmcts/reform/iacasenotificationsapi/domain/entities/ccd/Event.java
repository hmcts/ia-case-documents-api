package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum Event {

    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    PAY_AND_SUBMIT_APPEAL("payAndSubmitAppeal"),
    SEND_DIRECTION("sendDirection"),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence"),
    UPLOAD_RESPONDENT_EVIDENCE("uploadRespondentEvidence"),
    UPLOAD_HOME_OFFICE_BUNDLE("uploadHomeOfficeBundle"),
    BUILD_CASE("buildCase"),
    SUBMIT_CASE("submitCase"),
    REQUEST_CASE_EDIT("requestCaseEdit"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    ADD_APPEAL_RESPONSE("addAppealResponse"),
    REQUEST_HEARING_REQUIREMENTS("requestHearingRequirements"),
    REVIEW_HEARING_REQUIREMENTS("reviewHearingRequirements"),
    REQUEST_HEARING_REQUIREMENTS_FEATURE("requestHearingRequirementsFeature"),
    DRAFT_HEARING_REQUIREMENTS("draftHearingRequirements"),
    CHANGE_DIRECTION_DUE_DATE("changeDirectionDueDate"),
    UPLOAD_ADDITIONAL_EVIDENCE("uploadAdditionalEvidence"),
    UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE("uploadAdditionalEvidenceHomeOffice"),
    LIST_CASE("listCase"),
    LIST_CASE_WITHOUT_HEARING_REQUIREMENTS("listCaseWithoutHearingRequirements"),
    CREATE_CASE_SUMMARY("createCaseSummary"),
    REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE("revertStateToAwaitingRespondentEvidence"),
    GENERATE_HEARING_BUNDLE("generateHearingBundle"),
    ASYNC_STITCHING_COMPLETE("asyncStitchingComplete"),
    CUSTOMISE_HEARING_BUNDLE("customiseHearingBundle"),
    EDIT_CASE_LISTING("editCaseListing"),
    END_APPEAL("endAppeal"),
    RECORD_APPLICATION("recordApplication"),
    REQUEST_CASE_BUILDING("requestCaseBuilding"),
    FORCE_REQUEST_CASE_BUILDING("forceRequestCaseBuilding"),
    UPLOAD_HOME_OFFICE_APPEAL_RESPONSE("uploadHomeOfficeAppealResponse"),
    UPLOAD_ADDENDUM_EVIDENCE("uploadAddendumEvidence"),
    UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP("uploadAddendumEvidenceLegalRep"),
    UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE("uploadAddendumEvidenceHomeOffice"),
    UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER("uploadAddendumEvidenceAdminOfficer"),
    REQUEST_RESPONSE_REVIEW("requestResponseReview"),
    SEND_DECISION_AND_REASONS("sendDecisionAndReasons"),
    REQUEST_REASONS_FOR_APPEAL("requestReasonsForAppeal"),
    SUBMIT_REASONS_FOR_APPEAL("submitReasonsForAppeal"),
    UPDATE_HEARING_ADJUSTMENTS("updateHearingAdjustments"),
    REMOVE_APPEAL_FROM_ONLINE("removeAppealFromOnline"),
    CHANGE_HEARING_CENTRE("changeHearingCentre"),
    APPLY_FOR_FTPA_APPELLANT("applyForFTPAAppellant"),
    APPLY_FOR_FTPA_RESPONDENT("applyForFTPARespondent"),
    SUBMIT_TIME_EXTENSION("submitTimeExtension"),
    REVIEW_TIME_EXTENSION("reviewTimeExtension"),
    SEND_DIRECTION_WITH_QUESTIONS("sendDirectionWithQuestions"),
    SUBMIT_CLARIFYING_QUESTION_ANSWERS("submitClarifyingQuestionAnswers"),
    FORCE_CASE_TO_CASE_UNDER_REVIEW("forceCaseToCaseUnderReview"),
    FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS("forceCaseToSubmitHearingRequirements"),
    ADJOURN_HEARING_WITHOUT_DATE("adjournHearingWithoutDate"),
    RESTORE_STATE_FROM_ADJOURN("restoreStateFromAdjourn"),
    REQUEST_CMA_REQUIREMENTS("requestCmaRequirements"),
    SUBMIT_CMA_REQUIREMENTS("submitCmaRequirements"),
    LIST_CMA("listCma"),
    EDIT_APPEAL_AFTER_SUBMIT("editAppealAfterSubmit"),
    LINK_APPEAL("linkAppeal"),
    UNLINK_APPEAL("unlinkAppeal"),
    EDIT_DOCUMENTS("editDocuments"),
    LEADERSHIP_JUDGE_FTPA_DECISION("leadershipJudgeFtpaDecision"),
    RESIDENT_JUDGE_FTPA_DECISION("residentJudgeFtpaDecision"),
    PAYMENT_APPEAL("paymentAppeal"),
    REQUEST_RESPONSE_AMEND("requestResponseAmend"),
    MARK_APPEAL_PAID("markAppealPaid"),
    REINSTATE_APPEAL("reinstateAppeal"),
    MAKE_AN_APPLICATION("makeAnApplication"),
    DECIDE_AN_APPLICATION("decideAnApplication"),
    REQUEST_NEW_HEARING_REQUIREMENTS("requestNewHearingRequirements"),
    RECORD_REMISSION_DECISION("recordRemissionDecision"),
    NOC_REQUEST("nocRequest"),
    REMOVE_REPRESENTATION("removeRepresentation"),
    REMOVE_LEGAL_REPRESENTATIVE("removeLegalRepresentative"),
    REQUEST_FEE_REMISSION("requestFeeRemission"),
    MANAGE_FEE_UPDATE("manageFeeUpdate"),
    RECORD_OUT_OF_TIME_DECISION("recordOutOfTimeDecision"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
