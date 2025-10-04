package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DocumentTagTest {

    @Test
    void has_correct_values() {
        assertEquals("caseArgument", DocumentTag.CASE_ARGUMENT.toString());
        assertEquals("respondentEvidence", DocumentTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("appealResponse", DocumentTag.APPEAL_RESPONSE.toString());
        assertEquals("appealSubmission", DocumentTag.APPEAL_SUBMISSION.toString());
        assertEquals("additionalEvidence", DocumentTag.ADDITIONAL_EVIDENCE.toString());
        assertEquals("hearingRequirements", DocumentTag.HEARING_REQUIREMENTS.toString());
        assertEquals("hearingNotice", DocumentTag.HEARING_NOTICE.toString());
        assertEquals("reheardHearingNotice", DocumentTag.REHEARD_HEARING_NOTICE.toString());
        assertEquals("hearingNoticeRelisted", DocumentTag.HEARING_NOTICE_RELISTED.toString());
        assertEquals("reheardHearingNoticeRelisted", DocumentTag.REHEARD_HEARING_NOTICE_RELISTED.toString());
        assertEquals("caseSummary", DocumentTag.CASE_SUMMARY.toString());
        assertEquals("hearingBundle", DocumentTag.HEARING_BUNDLE.toString());
        assertEquals("updatedHearingBundle", DocumentTag.UPDATED_HEARING_BUNDLE.toString());
        assertEquals("addendumEvidence", DocumentTag.ADDENDUM_EVIDENCE.toString());
        assertEquals("decisionAndReasons", DocumentTag.DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("reheardDecisionAndReasons", DocumentTag.REHEARD_DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("submitCaseBundle", DocumentTag.APPEAL_SKELETON_BUNDLE.toString());
        assertEquals("endAppeal", DocumentTag.END_APPEAL.toString());
        assertEquals("endAppealAutomatically", DocumentTag.END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("cmaRequirements", DocumentTag.CMA_REQUIREMENTS.toString());
        assertEquals("cmaNotice", DocumentTag.CMA_NOTICE.toString());
        assertEquals("homeOfficeDecisionLetter", DocumentTag.HO_DECISION_LETTER.toString());
        assertEquals("finalDecisionAndReasonsDocument", DocumentTag.FINAL_DECISION_AND_REASONS_DOCUMENT.toString());
        assertEquals("recordOutOfTimeDecisionDocument", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT.toString());
        assertEquals("upperTribunalBundle", DocumentTag.UPPER_TRIBUNAL_BUNDLE.toString());
        assertEquals("appealReasons", DocumentTag.APPEAL_REASONS.toString());
        assertEquals("clarifyingQuestions", DocumentTag.CLARIFYING_QUESTIONS.toString());
        assertEquals("noticeOfAdjournedHearing", DocumentTag.NOTICE_OF_ADJOURNED_HEARING.toString());
        assertEquals("appealForm", DocumentTag.APPEAL_FORM.toString());
        assertEquals("noticeOfDecisionUtTransfer", DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER.toString());
        assertEquals("remittalDecision", DocumentTag.REMITTAL_DECISION.toString());
        assertEquals("decisionAndReasonsCoverLetter", DocumentTag.DECISION_AND_REASONS_COVER_LETTER.toString());
        assertEquals("updatedDecisionAndReasonsCoverLetter", DocumentTag.UPDATED_DECISION_AND_REASONS_COVER_LETTER.toString());
        assertEquals("bailSubmission", DocumentTag.BAIL_SUBMISSION.toString());
        assertEquals("uploadTheBailEvidenceDocs", DocumentTag.BAIL_EVIDENCE.toString());
        assertEquals("bailDecisionUnsigned", DocumentTag.BAIL_DECISION_UNSIGNED.toString());
        assertEquals("bailEndApplication", DocumentTag.BAIL_END_APPLICATION.toString());
        assertEquals("signedDecisionNotice", DocumentTag.SIGNED_DECISION_NOTICE.toString());
        assertEquals("uploadDocument", DocumentTag.UPLOAD_DOCUMENT.toString());
        assertEquals("uploadBailSummary", DocumentTag.BAIL_SUMMARY.toString());
        assertEquals("b1Document", DocumentTag.B1_DOCUMENT.toString());
        assertEquals("updatedFinalDecisionAndReasonsPdf", DocumentTag.UPDATED_FINAL_DECISION_AND_REASONS_PDF.toString());
        assertEquals("bailNoticeOfHearing", DocumentTag.BAIL_NOTICE_OF_HEARING.toString());
        assertEquals("requestCaseBuilding", DocumentTag.REQUEST_CASE_BUILDING.toString());
        assertEquals("internalAdaSuitability", DocumentTag.INTERNAL_ADA_SUITABILITY.toString());
        assertEquals("requestRespondentReview", DocumentTag.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("internalDetDecisionAndReasonsLetter", DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER.toString());
        assertEquals("uploadTheAppealResponse", DocumentTag.UPLOAD_THE_APPEAL_RESPONSE.toString());
        assertEquals("hearingBundleReadyLetter", DocumentTag.HEARING_BUNDLE_READY_LETTER.toString());
        assertEquals("internalAppealSubmission", DocumentTag.INTERNAL_APPEAL_SUBMISSION.toString());
        assertEquals("internalDetainedPrisonIrcAppealSubmission", DocumentTag.INTERNAL_DETAINED_PRISON_IRC_APPEAL_SUBMISSION.toString());
        assertEquals("internalEndAppealAutomatically", DocumentTag.INTERNAL_END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("internalDetMarkAsPaidLetter", DocumentTag.INTERNAL_DET_MARK_AS_PAID_LETTER.toString());
        assertEquals("internalListCaseLetter", DocumentTag.INTERNAL_LIST_CASE_LETTER.toString());
        assertEquals("internalRequestHearingRequirementsLetter", DocumentTag.INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER.toString());
        assertEquals("internalDetainedRequestHomeOfficeResponseReview", DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW.toString());
        assertEquals("internalDetainedEditCaseListingLetter", DocumentTag.INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER.toString());
        assertEquals("internalDetainedManageFeeUpdateLetter", DocumentTag.INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER.toString());
        assertEquals("internalDetMarkAsAdaLetter", DocumentTag.INTERNAL_DET_MARK_AS_ADA_LETTER.toString());
        assertEquals("internalDecideAnAppellantApplicationLetter", DocumentTag.INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER.toString());
        assertEquals("internalApplyForFtpaRespondent", DocumentTag.INTERNAL_APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("internalDetainedTransferOutOfAdaLetter", DocumentTag.INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER.toString());
        assertEquals("internalFtpaSubmittedAppellantLetter", DocumentTag.INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER.toString());
        assertEquals("internalAppellantFtpaDecidedLetter", DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER.toString());
        assertEquals("internalHoFtpaDecidedLetter", DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER.toString());
        assertEquals("internalHearingAdjustmentsUpdatedLetter", DocumentTag.INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER.toString());
        assertEquals("maintainCaseUnlinkAppealLetter", DocumentTag.MAINTAIN_CASE_UNLINK_APPEAL_LETTER.toString());
        assertEquals("internalUploadAdditionalEvidenceLetter", DocumentTag.INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER.toString());
        assertEquals("internalChangeHearingCentreLetter", DocumentTag.INTERNAL_CHANGE_HEARING_CENTRE_LETTER.toString());
        assertEquals("maintainCaseLinkAppealLetter", DocumentTag.MAINTAIN_CASE_LINK_APPEAL_LETTER.toString());
        assertEquals("amendHomeOfficeAppealResponse", DocumentTag.AMEND_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("internalNonStandardDirectionToAppellantLetter", DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER.toString());
        assertEquals("internalChangeDirectionDueDateLetter", DocumentTag.INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER.toString());
        assertEquals("internalEditAppealLetter", DocumentTag.INTERNAL_EDIT_APPEAL_LETTER.toString());
        assertEquals("homeOfficeUploadAdditionalAddendumEvidenceLetter", DocumentTag.HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER.toString());
        assertEquals("legalOfficerUploadAdditionalEvidenceLetter", DocumentTag.LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER.toString());
        assertEquals("internalHoChangeDirectionDueDateLetter", DocumentTag.INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER.toString());
        assertEquals("internalReinstateAppealLetter", DocumentTag.INTERNAL_REINSTATE_APPEAL_LETTER.toString());
        assertEquals("internalAdjournHearingWithoutDate", DocumentTag.INTERNAL_ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("upperTribunalTransferOrderDocument", DocumentTag.UPPER_TRIBUNAL_TRANSFER_ORDER_DOCUMENT.toString());
        assertEquals("internalEndAppealLetter", DocumentTag.INTERNAL_END_APPEAL_LETTER.toString());
        assertEquals("internalEndAppealLetterBundle", DocumentTag.INTERNAL_END_APPEAL_LETTER_BUNDLE.toString());
        assertEquals("internalCaseListedLetter", DocumentTag.INTERNAL_CASE_LISTED_LETTER.toString());
        assertEquals("internalCaseListedLetterBundle", DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE.toString());
        assertEquals("iAUT2Form", DocumentTag.IAUT_2_FORM.toString());
        assertEquals("", DocumentTag.NONE.toString());
        assertEquals("internalOutOfTimeDecisionLetter", DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER.toString());
        assertEquals("internalOutOfTimeDecisionLetter", DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER_BUNDLE.toString());
        assertEquals("internalEditCaseListingLetter", DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER.toString());
        assertEquals("internalEditCaseListingLetterBundle", DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE.toString());
        assertEquals("internalDetainedAppealSubmittedOutOfTimeWithExemptionLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_EXEMPTION_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedInTimeWithFeeToPayLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_IN_TIME_WITH_FEE_TO_PAY_LETTER.toString());
        assertEquals("internalDetainedAppealRemissionGrantedInTimeLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER.toString());
        assertEquals("internalDetainedOutOfTimeDecisionAllowedLetter", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER.toString());
        assertEquals("internalDetainedAppealHOUploadBundleAppellantLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedOutOfTimeWithFeeLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_FEE_LETTER.toString());
        assertEquals("detainedAppealAdjournHearingWithoutDateIrcPrisonLetter", DocumentTag.DETAINED_APPEAL_ADJOURN_HEARING_WITHOUT_DATE_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedOutOfTimeRemissionIrcPrisonLetter", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedOutOfTimeRemissionGrantedIrcPrisonLetter", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_GRANTED_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedAppealRemissionPartiallyGrantedOrRefusedTemplateLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_REMISSION_PARTIALLY_GRANTED_OR_REFUSED_TEMPLATE_LETTER.toString());
        assertEquals("internalDetainedLateRemissionPartiallyGrantedOrRefusedTemplateLetter", DocumentTag.INTERNAL_DETAINED_LATE_REMISSION_PARTIALLY_GRANTED_OR_REFUSED_TEMPLATE_LETTER.toString());
        assertEquals("internalDetainedLateRemissionGrantedTemplateLetter", DocumentTag.INTERNAL_DETAINED_LATE_REMISSION_GRANTED_TEMPLATE_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedWithExemptionLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_WITH_EXEMPTION_LETTER.toString());
        assertEquals("internalDetainedLateRemissionRefusedTemplateLetter", DocumentTag.INTERNAL_DETAINED_LATE_REMISSION_REFUSED_TEMPLATE_LETTER.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(113, DocumentTag.values().length);
    }
}
