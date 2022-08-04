package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentTag {

    CASE_ARGUMENT("caseArgument"),
    RESPONDENT_EVIDENCE("respondentEvidence"),
    APPEAL_RESPONSE("appealResponse"),
    APPEAL_SUBMISSION("appealSubmission"),
    ADDITIONAL_EVIDENCE("additionalEvidence"),
    HEARING_NOTICE("hearingNotice"),
    CASE_SUMMARY("caseSummary"),
    HEARING_BUNDLE("hearingBundle"),
    ADDENDUM_EVIDENCE("addendumEvidence"),
    DECISION_AND_REASONS_DRAFT("decisionAndReasons"),
    DECISION_AND_REASONS_COVER_LETTER("decisionAndReasonsCoverLetter"),
    FINAL_DECISION_AND_REASONS_PDF("finalDecisionAndReasonsPdf"),
    APPEAL_SKELETON_BUNDLE("submitCaseBundle"),
    HO_DECISION_LETTER("homeOfficeDecisionLetter"),
    END_APPEAL("endAppeal"),
    RECORD_OUT_OF_TIME_DECISION_DOCUMENT("recordOutOfTimeDecisionDocument"),
    BAIL_EVIDENCE("uploadTheBailEvidenceDocs"),
    APPLICATION_SUBMISSION("applicationSubmission"),
    BAIL_SUMMARY("uploadBailSummary"),
    SIGNED_DECISION_NOTICE("signedDecisionNotice"),
    BAIL_DECISION_UNSIGNED("bailDecisionUnsigned"),
    UPLOAD_DOCUMENT("uploadDocument"),
    BAIL_SUBMISSION("bailSubmission"),
    B1_DOCUMENT("b1Document"),

    @JsonEnumDefaultValue
    NONE("");

    @JsonValue
    private final String id;

    DocumentTag(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id;
    }
}
