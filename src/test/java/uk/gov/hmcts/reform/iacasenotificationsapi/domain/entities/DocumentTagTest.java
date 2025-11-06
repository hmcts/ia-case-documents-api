package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

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
        assertEquals("hearingNotice", DocumentTag.HEARING_NOTICE.toString());
        assertEquals("caseSummary", DocumentTag.CASE_SUMMARY.toString());
        assertEquals("hearingBundle", DocumentTag.HEARING_BUNDLE.toString());
        assertEquals("addendumEvidence", DocumentTag.ADDENDUM_EVIDENCE.toString());
        assertEquals("decisionAndReasons", DocumentTag.DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("decisionAndReasonsCoverLetter", DocumentTag.DECISION_AND_REASONS_COVER_LETTER.toString());
        assertEquals("finalDecisionAndReasonsPdf", DocumentTag.FINAL_DECISION_AND_REASONS_PDF.toString());
        assertEquals("submitCaseBundle", DocumentTag.APPEAL_SKELETON_BUNDLE.toString());
        assertEquals("endAppeal", DocumentTag.END_APPEAL.toString());
        assertEquals("homeOfficeDecisionLetter", DocumentTag.HO_DECISION_LETTER.toString());
        assertEquals("recordOutOfTimeDecisionDocument", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT.toString());
        assertEquals("", DocumentTag.NONE.toString());
        assertEquals("uploadTheBailEvidenceDocs", DocumentTag.BAIL_EVIDENCE.toString());
        assertEquals("applicationSubmission", DocumentTag.APPLICATION_SUBMISSION.toString());
        assertEquals("uploadBailSummary", DocumentTag.BAIL_SUMMARY.toString());
        assertEquals("signedDecisionNotice", DocumentTag.SIGNED_DECISION_NOTICE.toString());
        assertEquals("bailDecisionUnsigned", DocumentTag.BAIL_DECISION_UNSIGNED.toString());
        assertEquals("uploadDocument", DocumentTag.UPLOAD_DOCUMENT.toString());
        assertEquals("bailSubmission", DocumentTag.BAIL_SUBMISSION.toString());
        assertEquals("b1Document", DocumentTag.B1_DOCUMENT.toString());
        assertEquals("internalAdaSuitability", DocumentTag.INTERNAL_ADA_SUITABILITY.toString());
        assertEquals("requestRespondentReview", DocumentTag.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("uploadTheAppealResponse", DocumentTag.UPLOAD_THE_APPEAL_RESPONSE.toString());
        assertEquals("hearingBundleReadyLetter", DocumentTag.HEARING_BUNDLE_READY_LETTER.toString());
        assertEquals("internalDetDecisionAndReasonsLetter", DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER.toString());
        assertEquals("internalAppealSubmission", DocumentTag.INTERNAL_APPEAL_SUBMISSION.toString());
        assertEquals("internalRequestRespondentEvidenceLetter", DocumentTag.INTERNAL_REQUEST_RESPONDENT_EVIDENCE_LETTER.toString());
        assertEquals("internalEndAppealAutomatically", DocumentTag.INTERNAL_END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("internalDetMarkAsPaidLetter", DocumentTag.INTERNAL_DET_MARK_AS_PAID_LETTER.toString());
        assertEquals("internalListCaseLetter", DocumentTag.INTERNAL_LIST_CASE_LETTER.toString());
        assertEquals("internalRequestHearingRequirementsLetter", DocumentTag.INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER.toString());
        assertEquals("internalDetainedRequestHomeOfficeResponseReview", DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW.toString());
        assertEquals("internalDecideAnAppellantApplicationLetter", DocumentTag.INTERNAL_DECIDE_AN_APPELLANT_APPLICATION_LETTER.toString());
        assertEquals("internalDecideHomeOfficeApplicationLetter", DocumentTag.INTERNAL_DECIDE_HOME_OFFICE_APPLICATION_LETTER.toString());
        assertEquals("internalDetMarkAsAdaLetter", DocumentTag.INTERNAL_DET_MARK_AS_ADA_LETTER.toString());
        assertEquals("internalDetainedEditCaseListingLetter", DocumentTag.INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER.toString());
        assertEquals("internalApplyForFtpaRespondent", DocumentTag.INTERNAL_APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("internalFtpaSubmittedAppellantLetter", DocumentTag.INTERNAL_FTPA_SUBMITTED_APPELLANT_LETTER.toString());
        assertEquals("internalDetainedTransferOutOfAdaLetter", DocumentTag.INTERNAL_DETAINED_TRANSFER_OUT_OF_ADA_LETTER.toString());
        assertEquals("internalHoFtpaDecidedLetter", DocumentTag.INTERNAL_HO_FTPA_DECIDED_LETTER.toString());
        assertEquals("internalAppellantFtpaDecidedLetter", DocumentTag.INTERNAL_APPELLANT_FTPA_DECIDED_LETTER.toString());
        assertEquals("internalNonStandardDirectionToAppellantLetter", DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_TO_APPELLANT_LETTER.toString());
        assertEquals("internalNonStandardDirectionToRespondentLetter", DocumentTag.INTERNAL_NON_STANDARD_DIRECTION_RESPONDENT_LETTER.toString());
        assertEquals("internalHearingAdjustmentsUpdatedLetter", DocumentTag.INTERNAL_HEARING_ADJUSTMENTS_UPDATED_LETTER.toString());
        assertEquals("maintainCaseUnlinkAppealLetter", DocumentTag.MAINTAIN_CASE_UNLINK_APPEAL_LETTER.toString());
        assertEquals("internalChangeHearingCentreLetter", DocumentTag.INTERNAL_CHANGE_HEARING_CENTRE_LETTER.toString());
        assertEquals("maintainCaseLinkAppealLetter", DocumentTag.MAINTAIN_CASE_LINK_APPEAL_LETTER.toString());
        assertEquals("internalUploadAdditionalEvidenceLetter", DocumentTag.INTERNAL_UPLOAD_ADDITIONAL_EVIDENCE_LETTER.toString());
        assertEquals("amendHomeOfficeAppealResponse", DocumentTag.AMEND_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("internalChangeDirectionDueDateLetter", DocumentTag.INTERNAL_CHANGE_DIRECTION_DUE_DATE_LETTER.toString());
        assertEquals("internalEditAppealLetter", DocumentTag.INTERNAL_EDIT_APPEAL_LETTER.toString());
        assertEquals("homeOfficeUploadAdditionalAddendumEvidenceLetter", DocumentTag.HOME_OFFICE_UPLOAD_ADDITIONAL_ADDENDUM_EVIDENCE_LETTER.toString());
        assertEquals("legalOfficerUploadAdditionalEvidenceLetter", DocumentTag.LEGAL_OFFICER_UPLOAD_ADDITIONAL_EVIDENCE_LETTER.toString());
        assertEquals("internalHoChangeDirectionDueDateLetter", DocumentTag.INTERNAL_HO_CHANGE_DIRECTION_DUE_DATE_LETTER.toString());
        assertEquals("homeOfficeNonStandardDirectionToHOLetter", DocumentTag.HOME_OFFICE_NON_STANDARD_DIRECTION_LETTER.toString());
        assertEquals("internalReinstateAppealLetter", DocumentTag.INTERNAL_REINSTATE_APPEAL_LETTER.toString());
        assertEquals("internalAdjournHearingWithoutDate", DocumentTag.INTERNAL_ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("internalEndAppealLetterBundle", DocumentTag.INTERNAL_END_APPEAL_LETTER_BUNDLE.toString());
        assertEquals("internalCaseListedLetter", DocumentTag.INTERNAL_CASE_LISTED_LETTER.toString());
        assertEquals("internalCaseListedLrLetter", DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER.toString());
        assertEquals("internalCaseListedLetterBundle", DocumentTag.INTERNAL_CASE_LISTED_LETTER_BUNDLE.toString());
        assertEquals("internalCaseListedLrLetterBundle", DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER_BUNDLE.toString());
        assertEquals("internalEditCaseListingLetterBundle", DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER_BUNDLE.toString());
        assertEquals("internalEditCaseListingLrLetterBundle", DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER_BUNDLE.toString());
        assertEquals("internalOutOfTimeDecisionLetter", DocumentTag.INTERNAL_OUT_OF_TIME_DECISION_LETTER_BUNDLE.toString());
        assertEquals("internalDetainedManageFeeUpdateLetter", DocumentTag.INTERNAL_DETAINED_MANAGE_FEE_UPDATE_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedOutOfTimeWithExemptionLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_EXEMPTION_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedInTimeWithFeeToPayLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_IN_TIME_WITH_FEE_TO_PAY_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedOutOfTimeWithFeeLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_OUT_OF_TIME_WITH_FEE_LETTER.toString());
        assertEquals("internalDetainedAppealRemissionGrantedInTimeLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_REMISSION_GRANTED_IN_TIME_LETTER.toString());
        assertEquals("internalDetainedOutOfTimeDecisionAllowedLetter", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_DECISION_ALLOWED_LETTER.toString());
        assertEquals("detainedAppealAdjournHearingWithoutDateIrcPrisonLetter", DocumentTag.DETAINED_APPEAL_ADJOURN_HEARING_WITHOUT_DATE_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedAppealHOUploadBundleAppellantLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_HO_UPLOAD_BUNDLE_APPELLANT_LETTER.toString());
        assertEquals("internalDetainedOutOfTimeRemissionIrcPrisonLetter", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedOutOfTimeRemissionGrantedIrcPrisonLetter", DocumentTag.INTERNAL_DETAINED_OUT_OF_TIME_REMISSION_GRANTED_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedAppealRemissionPartiallyGrantedOrRefusedTemplateLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_REMISSION_PARTIALLY_GRANTED_OR_REFUSED_TEMPLATE_LETTER.toString());
        assertEquals("detainedLegalRepRemovedIrcPrisonLetter", DocumentTag.DETAINED_LEGAL_REP_REMOVED_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedLateRemissionPartiallyGrantedOrRefusedTemplateLetter", DocumentTag.INTERNAL_DETAINED_LATE_REMISSION_PARTIALLY_GRANTED_OR_REFUSED_TEMPLATE_LETTER.toString());
        assertEquals("internalDetainedAppealUpdateTribunalDecisionRule31IrcPrisonLetter", DocumentTag.INTERNAL_APPEAL_DETAINED_UPDATE_TRIBUNAL_DECISION_RULE_31_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedLateRemissionGrantedTemplateLetter", DocumentTag.INTERNAL_DETAINED_LATE_REMISSION_GRANTED_TEMPLATE_LETTER.toString());
        assertEquals("internalDetainedAppealSubmittedWithExemptionLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_SUBMITTED_WITH_EXEMPTION_LETTER.toString());
        assertEquals("internalDetainedDecisionWithoutHearing", DocumentTag.INTERNAL_DETAINED_DECISION_WITHOUT_HEARING.toString());
        assertEquals("internalDetainedLateRemissionRefusedTemplateLetter", DocumentTag.INTERNAL_DETAINED_LATE_REMISSION_REFUSED_TEMPLATE_LETTER.toString());
        assertEquals("internalDetainedAppealRemittedAipIrcPrisonLetter", DocumentTag.INTERNAL_DETAINED_APPEAL_REMITTED_AIP_IRC_PRISON_LETTER.toString());
        assertEquals("internalDetainedIrcPrisonFtpaDisposedRules31Or32Letter", DocumentTag.INTERNAL_DETAINED_IRC_PRISON_FTPA_DISPOSED_RULES_31_OR_32_LETTER.toString());
        assertEquals("homeOfficeApplicationDecidedLetter", DocumentTag.HOME_OFFICE_APPLICATION_DECIDED_LETTER.toString());
        assertEquals("aipmDetainedInPrisonOrIrcReinstateAppealLetter", DocumentTag.AIPM_DETAINED_IN_PRISON_IRC_REINSTATE_APPEAL_LETTER.toString());
        assertEquals("internalEditCaseListingLetter", DocumentTag.INTERNAL_EDIT_CASE_LISTING_LETTER.toString());
        assertEquals("internalEditCaseListingLrLetter", DocumentTag.INTERNAL_EDIT_CASE_LISTING_LR_LETTER.toString());

    }

    @Test
    void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(101, DocumentTag.values().length);
    }
}
