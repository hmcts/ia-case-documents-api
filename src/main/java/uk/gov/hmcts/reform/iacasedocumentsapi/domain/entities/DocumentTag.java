package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DocumentTag {

    CASE_ARGUMENT("caseArgument"),
    RESPONDENT_EVIDENCE("respondentEvidence"),
    APPEAL_RESPONSE("appealResponse"),
    APPEAL_SUBMISSION("appealSubmission"),
    ADDITIONAL_EVIDENCE("additionalEvidence"),
    HEARING_REQUIREMENTS("hearingRequirements"),
    HEARING_NOTICE("hearingNotice"),
    REHEARD_HEARING_NOTICE("reheardHearingNotice"),
    CASE_SUMMARY("caseSummary"),
    HEARING_BUNDLE("hearingBundle"),
    ADDENDUM_EVIDENCE("addendumEvidence"),
    DECISION_AND_REASONS_DRAFT("decisionAndReasons"),
    REHEARD_DECISION_AND_REASONS_DRAFT("reheardDecisionAndReasons"),
    DECISION_AND_REASONS_COVER_LETTER("decisionAndReasonsCoverLetter"),
    FINAL_DECISION_AND_REASONS_PDF("finalDecisionAndReasonsPdf"),
    APPEAL_SKELETON_BUNDLE("submitCaseBundle"),
    END_APPEAL("endAppeal"),
    CMA_REQUIREMENTS("cmaRequirements"),
    CMA_NOTICE("cmaNotice"),
    HO_DECISION_LETTER("homeOfficeDecisionLetter"),
    FTPA_APPELLANT("ftpaAppellant"),
    FTPA_RESPONDENT("ftpaRespondent"),
    FTPA_DECISION_AND_REASONS("ftpaDecisionAndReasons"),
    RECORD_OUT_OF_TIME_DECISION_DOCUMENT("recordOutOfTimeDecisionDocument"),
    UPPER_TRIBUNAL_BUNDLE("upperTribunalBundle"),
    APPEAL_REASONS("appealReasons"),

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

