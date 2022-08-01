package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Event {

    START_APPEAL("startAppeal"),
    EDIT_APPEAL("editAppeal"),
    SUBMIT_APPEAL("submitAppeal"),
    PAY_AND_SUBMIT_APPEAL("payAndSubmitAppeal"),
    SEND_DIRECTION("sendDirection"),
    REQUEST_RESPONDENT_EVIDENCE("requestRespondentEvidence"),
    UPLOAD_RESPONDENT_EVIDENCE("uploadRespondentEvidence"),
    BUILD_CASE("buildCase"),
    SUBMIT_CASE("submitCase"),
    REQUEST_CASE_EDIT("requestCaseEdit"),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview"),
    ADD_APPEAL_RESPONSE("addAppealResponse"),
    REQUEST_HEARING_REQUIREMENTS("requestHearingRequirements"),
    DRAFT_HEARING_REQUIREMENTS("draftHearingRequirements"),
    UPDATE_HEARING_REQUIREMENTS("updateHearingRequirements"),
    CHANGE_DIRECTION_DUE_DATE("changeDirectionDueDate"),
    UPLOAD_ADDITIONAL_EVIDENCE("uploadAdditionalEvidence"),
    LIST_CASE("listCase"),
    CREATE_CASE_SUMMARY("createCaseSummary"),
    REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE("revertStateToAwaitingRespondentEvidence"),
    GENERATE_HEARING_BUNDLE("generateHearingBundle"),
    CUSTOMISE_HEARING_BUNDLE("customiseHearingBundle"),
    GENERATE_DECISION_AND_REASONS("generateDecisionAndReasons"),
    SEND_DECISION_AND_REASONS("sendDecisionAndReasons"),
    EDIT_CASE_LISTING("editCaseListing"),
    END_APPEAL("endAppeal"),
    END_APPEAL_AUTOMATICALLY("endAppealAutomatically"),
    ADJOURN_HEARING_WITHOUT_DATE("adjournHearingWithoutDate"),
    SUBMIT_CMA_REQUIREMENTS("submitCmaRequirements"),
    LIST_CMA("listCma"),
    EDIT_APPEAL_AFTER_SUBMIT("editAppealAfterSubmit"),
    GENERATE_UPPER_TRIBUNAL_BUNDLE("generateUpperTribunalBundle"),
    SUBMIT_REASONS_FOR_APPEAL("submitReasonsForAppeal"),
    SUBMIT_CLARIFYING_QUESTION_ANSWERS("submitClarifyingQuestionAnswers"),

    @JsonEnumDefaultValue
    UNKNOWN("unknown");

    @JsonValue
    private final String id;

    Event(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }

}
