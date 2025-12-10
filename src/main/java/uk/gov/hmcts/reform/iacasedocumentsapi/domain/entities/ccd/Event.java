package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    START_APPEAL("startAppeal", CaseType.ASYLUM),
    EDIT_APPEAL("editAppeal", CaseType.ASYLUM),
    SUBMIT_APPEAL("submitAppeal", CaseType.ASYLUM),
    PAY_AND_SUBMIT_APPEAL("payAndSubmitAppeal", CaseType.ASYLUM),
    SEND_DIRECTION("sendDirection", CaseType.ASYLUM),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence", CaseType.ASYLUM),
    UPLOAD_RESPONDENT_EVIDENCE("uploadRespondentEvidence", CaseType.ASYLUM),
    BUILD_CASE("buildCase", CaseType.ASYLUM),
    SUBMIT_CASE("submitCase", CaseType.ASYLUM),
    REQUEST_CASE_EDIT("requestCaseEdit", CaseType.ASYLUM),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview", CaseType.ASYLUM),
    ADD_APPEAL_RESPONSE("addAppealResponse", CaseType.ASYLUM),
    REQUEST_HEARING_REQUIREMENTS("requestHearingRequirements", CaseType.ASYLUM),
    DRAFT_HEARING_REQUIREMENTS("draftHearingRequirements", CaseType.ASYLUM),
    CHANGE_DIRECTION_DUE_DATE("changeDirectionDueDate", CaseType.ASYLUM),
    UPLOAD_ADDITIONAL_EVIDENCE("uploadAdditionalEvidence", CaseType.ASYLUM),
    LIST_CASE("listCase", CaseType.ASYLUM),
    CREATE_CASE_SUMMARY("createCaseSummary", CaseType.ASYLUM),
    REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE("revertStateToAwaitingRespondentEvidence", CaseType.ASYLUM),
    GENERATE_HEARING_BUNDLE("generateHearingBundle", CaseType.ASYLUM),
    ASYNC_STITCHING_COMPLETE("asyncStitchingComplete", CaseType.ASYLUM),
    REQUEST_HEARING_REQUIREMENTS_FEATURE("requestHearingRequirementsFeature", CaseType.ASYLUM),
    CUSTOMISE_HEARING_BUNDLE("customiseHearingBundle", CaseType.ASYLUM),
    GENERATE_UPDATED_HEARING_BUNDLE("generateUpdatedHearingBundle", CaseType.ASYLUM),
    GENERATE_DECISION_AND_REASONS("generateDecisionAndReasons", CaseType.ASYLUM),
    ADA_SUITABILITY_REVIEW("adaSuitabilityReview", CaseType.ASYLUM),
    SEND_DECISION_AND_REASONS("sendDecisionAndReasons", CaseType.ASYLUM),
    EDIT_CASE_LISTING("editCaseListing", CaseType.ASYLUM),
    MANAGE_FEE_UPDATE("manageFeeUpdate", CaseType.ASYLUM),
    END_APPEAL("endAppeal", CaseType.ASYLUM),
    END_APPEAL_AUTOMATICALLY("endAppealAutomatically", CaseType.ASYLUM),
    ADJOURN_HEARING_WITHOUT_DATE("adjournHearingWithoutDate", CaseType.ASYLUM),
    SUBMIT_CMA_REQUIREMENTS("submitCmaRequirements", CaseType.ASYLUM),
    LIST_CMA("listCma", CaseType.ASYLUM),
    EDIT_APPEAL_AFTER_SUBMIT("editAppealAfterSubmit", CaseType.ASYLUM),
    GENERATE_UPPER_TRIBUNAL_BUNDLE("generateUpperTribunalBundle", CaseType.ASYLUM),
    SUBMIT_REASONS_FOR_APPEAL("submitReasonsForAppeal", CaseType.ASYLUM),
    SUBMIT_CLARIFYING_QUESTION_ANSWERS("submitClarifyingQuestionAnswers", CaseType.ASYLUM),

    RECORD_ADJOURNMENT_DETAILS("recordAdjournmentDetails", CaseType.ASYLUM),

    UPDATE_TRIBUNAL_DECISION("updateTribunalDecision", CaseType.ASYLUM),
    REQUEST_CASE_BUILDING("requestCaseBuilding", CaseType.ASYLUM),
    UPLOAD_HOME_OFFICE_APPEAL_RESPONSE("uploadHomeOfficeAppealResponse", CaseType.ASYLUM),
    RECORD_OUT_OF_TIME_DECISION("recordOutOfTimeDecision", CaseType.ASYLUM),
    MARK_APPEAL_PAID("markAppealPaid", CaseType.ASYLUM),
    RECORD_REMISSION_DECISION("recordRemissionDecision", CaseType.ASYLUM),
    REQUEST_RESPONSE_REVIEW("requestResponseReview", CaseType.ASYLUM),
    MARK_APPEAL_AS_ADA("markAppealAsAda", CaseType.ASYLUM),
    APPLY_FOR_FTPA_RESPONDENT("applyForFTPARespondent", CaseType.ASYLUM),
    APPLY_FOR_FTPA_APPELLANT("applyForFTPAAppellant", CaseType.ASYLUM),
    DECIDE_AN_APPLICATION("decideAnApplication", CaseType.ASYLUM),
    TRANSFER_OUT_OF_ADA("transferOutOfAda", CaseType.ASYLUM),
    RESIDENT_JUDGE_FTPA_DECISION("residentJudgeFtpaDecision", CaseType.ASYLUM),
    MAINTAIN_CASE_LINKS("maintainCaseLinks", CaseType.ASYLUM),
    UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER("uploadAddendumEvidenceAdminOfficer", CaseType.ASYLUM),
    CHANGE_HEARING_CENTRE("changeHearingCentre", CaseType.ASYLUM),
    CREATE_CASE_LINK("createCaseLink", CaseType.ASYLUM),
    REQUEST_RESPONSE_AMEND("requestResponseAmend", CaseType.ASYLUM),
    UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE("uploadAdditionalEvidenceHomeOffice", CaseType.ASYLUM),
    UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE("uploadAddendumEvidenceHomeOffice", CaseType.ASYLUM),
    UPLOAD_ADDENDUM_EVIDENCE("uploadAddendumEvidence", CaseType.ASYLUM),
    REINSTATE_APPEAL("reinstateAppeal", CaseType.ASYLUM),
    DECISION_WITHOUT_HEARING("decisionWithoutHearing", CaseType.ASYLUM),
    MARK_APPEAL_AS_REMITTED("markAppealAsRemitted", CaseType.ASYLUM),

    SUBMIT_APPLICATION("submitApplication", CaseType.BAIL),
    RECORD_THE_DECISION("recordTheDecision", CaseType.BAIL),
    END_APPLICATION("endApplication", CaseType.BAIL),
    MAKE_NEW_APPLICATION("makeNewApplication", CaseType.BAIL),
    EDIT_BAIL_APPLICATION_AFTER_SUBMIT("editBailApplicationAfterSubmit", CaseType.BAIL),
    UPLOAD_SIGNED_DECISION_NOTICE("uploadSignedDecisionNotice", CaseType.BAIL),
    UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT("uploadSignedDecisionNoticeConditionalGrant", CaseType.BAIL),
    CASE_LISTING("caseListing", CaseType.BAIL),

    UPDATE_HEARING_REQUIREMENTS("updateHearingRequirements", CaseType.ASYLUM),
    UPDATE_HEARING_ADJUSTMENTS("updateHearingAdjustments", CaseType.ASYLUM),
    SAVE_NOTIFICATIONS_TO_DATA("saveNotificationsToData", CaseType.ASYLUM),
    DECIDE_FTPA_APPLICATION("decideFtpaApplication", CaseType.ASYLUM),

    REMOVE_REPRESENTATION("removeRepresentation", CaseType.ASYLUM),
    REMOVE_LEGAL_REPRESENTATIVE("removeLegalRepresentative", CaseType.ASYLUM),

    @JsonEnumDefaultValue
    UNKNOWN("unknown", CaseType.UNKNOWN);

    @JsonValue
    private final String id;

    private final CaseType caseType;

    Event(String id, CaseType caseType) {
        this.id = id;
        this.caseType = caseType;
    }

    @Override
    public String toString() {
        return id;
    }

}
