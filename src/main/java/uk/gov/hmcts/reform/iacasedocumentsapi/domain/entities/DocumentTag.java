package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseType;

public enum DocumentTag {

    CASE_ARGUMENT("caseArgument", CaseType.ASYLUM),
    RESPONDENT_EVIDENCE("respondentEvidence", CaseType.ASYLUM),
    APPEAL_RESPONSE("appealResponse", CaseType.ASYLUM),
    APPEAL_SUBMISSION("appealSubmission", CaseType.ASYLUM),
    INTERNAL_APPEAL_SUBMISSION("internalAppealSubmission", CaseType.ASYLUM),
    INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION("internalDetainedPrisonIrcAppealSubmission", CaseType.ASYLUM),
    ADDITIONAL_EVIDENCE("additionalEvidence", CaseType.ASYLUM),
    HEARING_REQUIREMENTS("hearingRequirements", CaseType.ASYLUM),
    HEARING_NOTICE("hearingNotice", CaseType.ASYLUM),
    REHEARD_HEARING_NOTICE("reheardHearingNotice", CaseType.ASYLUM),
    HEARING_NOTICE_RELISTED("hearingNoticeRelisted", CaseType.ASYLUM),
    REHEARD_HEARING_NOTICE_RELISTED("reheardHearingNoticeRelisted", CaseType.ASYLUM),
    ADA_SUITABILITY("adaSuitability", CaseType.ASYLUM),
    INTERNAL_ADA_SUITABILITY("internalAdaSuitability", CaseType.ASYLUM),
    CASE_SUMMARY("caseSummary", CaseType.ASYLUM),
    HEARING_BUNDLE("hearingBundle", CaseType.ASYLUM),
    UPDATED_HEARING_BUNDLE("updatedHearingBundle", CaseType.ASYLUM),
    ADDENDUM_EVIDENCE("addendumEvidence", CaseType.ASYLUM),
    DECISION_AND_REASONS_DRAFT("decisionAndReasons", CaseType.ASYLUM),
    REHEARD_DECISION_AND_REASONS_DRAFT("reheardDecisionAndReasons", CaseType.ASYLUM),
    DECISION_AND_REASONS_COVER_LETTER("decisionAndReasonsCoverLetter", CaseType.ASYLUM),
    UPDATED_DECISION_AND_REASONS_COVER_LETTER("updatedDecisionAndReasonsCoverLetter", CaseType.ASYLUM),
    INTERNAL_DET_DECISION_AND_REASONS_LETTER("internalDetDecisionAndReasonsLetter", CaseType.ASYLUM),
    FINAL_DECISION_AND_REASONS_PDF("finalDecisionAndReasonsPdf", CaseType.ASYLUM),
    UPDATED_FINAL_DECISION_AND_REASONS_PDF("updatedFinalDecisionAndReasonsPdf", CaseType.ASYLUM),
    FINAL_DECISION_AND_REASONS_DOCUMENT("finalDecisionAndReasonsDocument", CaseType.ASYLUM),
    APPEAL_SKELETON_BUNDLE("submitCaseBundle", CaseType.ASYLUM),
    END_APPEAL("endAppeal", CaseType.ASYLUM),
    END_APPEAL_AUTOMATICALLY("endAppealAutomatically", CaseType.ASYLUM),
    CMA_REQUIREMENTS("cmaRequirements", CaseType.ASYLUM),
    CMA_NOTICE("cmaNotice", CaseType.ASYLUM),
    HO_DECISION_LETTER("homeOfficeDecisionLetter", CaseType.ASYLUM),
    FTPA_APPELLANT("ftpaAppellant", CaseType.ASYLUM),
    FTPA_RESPONDENT("ftpaRespondent", CaseType.ASYLUM),
    FTPA_DECISION_AND_REASONS("ftpaDecisionAndReasons", CaseType.ASYLUM),
    RECORD_OUT_OF_TIME_DECISION_DOCUMENT("recordOutOfTimeDecisionDocument", CaseType.ASYLUM),
    UPPER_TRIBUNAL_BUNDLE("upperTribunalBundle", CaseType.ASYLUM),
    APPEAL_REASONS("appealReasons", CaseType.ASYLUM),
    CLARIFYING_QUESTIONS("clarifyingQuestions", CaseType.ASYLUM),

    NOTICE_OF_ADJOURNED_HEARING("noticeOfAdjournedHearing", CaseType.ASYLUM),

    APPEAL_FORM("appealForm", CaseType.ASYLUM),
    NOTICE_OF_DECISION_UT_TRANSFER("noticeOfDecisionUtTransfer", CaseType.ASYLUM),
    REQUEST_CASE_BUILDING("requestCaseBuilding", CaseType.ASYLUM),
    REQUEST_RESPONDENT_REVIEW("requestRespondentReview", CaseType.ASYLUM),
    UPLOAD_THE_APPEAL_RESPONSE("uploadTheAppealResponse", CaseType.ASYLUM),
    HEARING_BUNDLE_READY_LETTER("hearingBundleReadyLetter", CaseType.ASYLUM),
    INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER("internalRequestRespondentEvidenceLetter", CaseType.ASYLUM),
    INTERNAL_END_APPEAL_AUTOMATICALLY("internalEndAppealAutomatically", CaseType.ASYLUM),
    INTERNAL_DET_MARK_AS_PAID_LETTER("internalDetMarkAsPaidLetter", CaseType.ASYLUM),
    INTERNAL_LIST_CASE_LETTER("internalListCaseLetter", CaseType.ASYLUM),
    INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER("internalRequestHearingRequirementsLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW("internalDetainedRequestHomeOfficeResponseReview", CaseType.ASYLUM),
    INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER("internalDetainedEditCaseListingLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER("internalDetainedManageFeeUpdateLetter", CaseType.ASYLUM),
    INTERNAL_DET_MARK_AS_ADA_LETTER("internalDetMarkAsAdaLetter", CaseType.ASYLUM),
    INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER("internalDecideAnAppellantApplicationLetter", CaseType.ASYLUM),
    INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER("internalDecideHomeOfficeApplicationLetter", CaseType.ASYLUM),
    INTERNAL_APPLY_FOR_FTPA_RESPONDENT("internalApplyForFtpaRespondent", CaseType.ASYLUM),
    INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER("internalDetainedTransferOutOfAdaLetter", CaseType.ASYLUM),
    INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER("internalFtpaSubmittedAppellantLetter", CaseType.ASYLUM),
    INTERNAL_APPELLANT_FTPA_DECIDED_LETTER("internalAppellantFtpaDecidedLetter", CaseType.ASYLUM),
    INTERNAL_HO_FTPA_DECIDED_LETTER("internalHoFtpaDecidedLetter", CaseType.ASYLUM),
    INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER("internalHearingAdjustmentsUpdatedLetter", CaseType.ASYLUM),
    MAINTAIN_CASE_UNLINK_APPEAL_LETTER("maintainCaseUnlinkAppealLetter", CaseType.ASYLUM),
    MAINTAIN_CASE_LINK_APPEAL_LETTER("maintainCaseLinkAppealLetter", CaseType.ASYLUM),
    INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER("internalUploadAdditionalEvidenceLetter", CaseType.ASYLUM),
    INTERNAL_CHANGE_HEARING_CENTRE_LETTER("internalChangeHearingCentreLetter", CaseType.ASYLUM),
    HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER("homeOfficeUploadAdditionalAddendumEvidenceLetter", CaseType.ASYLUM),
    LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER("legalOfficerUploadAdditionalEvidenceLetter", CaseType.ASYLUM),
    AMEND_HOME_OFFICE_APPEAL_RESPONSE("amendHomeOfficeAppealResponse", CaseType.ASYLUM),
    INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER("internalNonStandardDirectionToAppellantLetter", CaseType.ASYLUM),
    INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER("internalChangeDirectionDueDateLetter", CaseType.ASYLUM),
    INTERNAL_EDIT_APPEAL_LETTER("internalEditAppealLetter", CaseType.ASYLUM),
    INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER("internalNonStandardDirectionToRespondentLetter", CaseType.ASYLUM),
    INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER("internalHoChangeDirectionDueDateLetter", CaseType.ASYLUM),
    INTERNAL_REINSTATE_APPEAL_LETTER("internalReinstateAppealLetter", CaseType.ASYLUM),
    UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT("upperTribunalTransferOrderDocument", CaseType.ASYLUM),
    IAUT_2_FORM("iAUT2Form", CaseType.ASYLUM),
    INTERNAL_END_APPEAL_LETTER("internalEndAppealLetter", CaseType.ASYLUM),
    INTERNAL_END_APPEAL_LETTER_BUNDLE("internalEndAppealLetterBundle", CaseType.ASYLUM),
    REMITTAL_DECISION("remittalDecision", CaseType.ASYLUM),
    INTERNAL_CASE_LISTED_LETTER("internalCaseListedLetter", CaseType.ASYLUM),
    INTERNAL_CASE_LISTED_LETTER_BUNDLE("internalCaseListedLetterBundle", CaseType.ASYLUM),
    APPEAL_WAS_NOT_SUBMITTED_SUPPORTING_DOCUMENT("appealWasNotSubmittedSupportingDocument", CaseType.ASYLUM),
    INTERNAL_OUT_OF_TIME_DECISION_LETTER("internalOutOfTimeDecisionLetter", CaseType.ASYLUM),
    INTERNAL_OUT_OF_TIME_DECISION_LETTER_BUNDLE("internalOutOfTimeDecisionLetter", CaseType.ASYLUM),
    INTERNAL_EDIT_CASE_LISTING_LETTER("internalEditCaseListingLetter", CaseType.ASYLUM),
    INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE("internalEditCaseListingLetterBundle", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_EXEMPTION_LETTER("internalDetainedAppealSubmittedOutOfTimeWithExemptionLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_SUBMITTED_IN_TIME_WITH_FEE_TO_PAY_LETTER("internalDetainedAppealSubmittedInTimeWithFeeToPayLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER("internalDetainedAppealRemissionGrantedInTimeLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER("internalDetainedOutOfTimeDecisionAllowedLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER("internalDetainedAppealHOUploadBundleAppellantLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_IRC_PRISON_LETTER("internalDetainedOutOfTimeRemissionIrcPrisonLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_FEE_LETTER("internalDetainedAppealSubmittedOutOfTimeWithFeeLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_GRANTED_IRC_PRISON_LETTER("internalDetainedOutOfTimeRemissionGrantedIrcPrisonLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_REMISSION_PARTIALLY_GRANTED_OR_REFUSED_TEMPLATE_LETTER("internalDetainedAppealRemissionPartiallyGrantedOrRefusedTemplateLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_LATE_REMISSION_PARTIALLY_GRANTED_OR_REFUSED_TEMPLATE_LETTER("internalDetainedLateRemissionPartiallyGrantedOrRefusedTemplateLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_LATE_REMISSION_GRANTED_TEMPLATE_LETTER("internalDetainedLateRemissionGrantedTemplateLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_LATE_REMISSION_REFUSED_TEMPLATE_LETTER("internalDetainedLateRemissionRefusedTemplateLetter", CaseType.ASYLUM),
    DETAINED_LEGAL_REP_REMOVED_IRC_PRISON_LETTER("detainedLegalRepRemovedIrcPrisonLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_UPDATE_TRIBUNAL_DECISION_RULE_31_IRC_PRISON_LETTER("internalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_SUBMITTED_WITH_EXEMPTION_LETTER("internalDetainedAppealSubmittedWithExemptionLetter", CaseType.ASYLUM),
    INTERNAL_DETAINED_DECISION_WITHOUT_HEARING("internalDetainedDecisionWithoutHearing", CaseType.ASYLUM),
    INTERNAL_DETAINED_IRC_PRISON_FTPA_DISPOSED_RULES_31_OR_32_LETTER("internalDetainedIrcPrisonFtpaDisposedRules31Or32Letter", CaseType.ASYLUM),
    INTERNAL_DETAINED_APPEAL_REMITTED_AIP_IRC_PRISON_LETTER("internalDetainedAppealRemittedAipIrcPrisonLetter", CaseType.ASYLUM),
    DETAINED_APPEAL_ADJOURN_HEARING_WITHOUT_DATE_IRC_PRISON_LETTER("detainedAppealAdjournHearingWithoutDateIrcPrisonLetter", CaseType.ASYLUM),
    HOME_OFFICE_APPLICATION_DECIDED_LETTER("homeOfficeApplicationDecidedLetter", CaseType.ASYLUM),

    BAIL_SUBMISSION("bailSubmission", CaseType.BAIL),
    BAIL_EVIDENCE("uploadTheBailEvidenceDocs", CaseType.BAIL),
    BAIL_DECISION_UNSIGNED("bailDecisionUnsigned", CaseType.BAIL),
    BAIL_END_APPLICATION("bailEndApplication", CaseType.BAIL),
    SIGNED_DECISION_NOTICE("signedDecisionNotice", CaseType.BAIL),
    UPLOAD_DOCUMENT("uploadDocument", CaseType.BAIL),
    BAIL_SUMMARY("uploadBailSummary", CaseType.BAIL),
    B1_DOCUMENT("b1Document", CaseType.BAIL),
    BAIL_NOTICE_OF_HEARING("bailNoticeOfHearing", CaseType.BAIL),

    @JsonEnumDefaultValue
    NONE("", CaseType.UNKNOWN);


    @JsonValue
    private final String id;
    private final CaseType caseType;

    DocumentTag(String id, CaseType caseType) {
        this.id = id;
        this.caseType = caseType;
    }

    public CaseType getCaseType() {
        return caseType;
    }

    @Override
    public String toString() {
        return id;
    }
}

