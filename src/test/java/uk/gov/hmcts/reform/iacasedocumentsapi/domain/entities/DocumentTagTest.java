package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DocumentTagTest {

    @Test
    public void has_correct_values() {
        assertEquals("caseArgument", DocumentTag.CASE_ARGUMENT.toString());
        assertEquals("respondentEvidence", DocumentTag.RESPONDENT_EVIDENCE.toString());
        assertEquals("appealResponse", DocumentTag.APPEAL_RESPONSE.toString());
        assertEquals("appealSubmission", DocumentTag.APPEAL_SUBMISSION.toString());
        assertEquals("additionalEvidence", DocumentTag.ADDITIONAL_EVIDENCE.toString());
        assertEquals("hearingRequirements", DocumentTag.HEARING_REQUIREMENTS.toString());
        assertEquals("hearingNotice", DocumentTag.HEARING_NOTICE.toString());
        assertEquals("reheardHearingNotice", DocumentTag.REHEARD_HEARING_NOTICE.toString());
        assertEquals("caseSummary", DocumentTag.CASE_SUMMARY.toString());
        assertEquals("hearingBundle", DocumentTag.HEARING_BUNDLE.toString());
        assertEquals("addendumEvidence", DocumentTag.ADDENDUM_EVIDENCE.toString());
        assertEquals("decisionAndReasons", DocumentTag.DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("reheardDecisionAndReasons", DocumentTag.REHEARD_DECISION_AND_REASONS_DRAFT.toString());
        assertEquals("submitCaseBundle", DocumentTag.APPEAL_SKELETON_BUNDLE.toString());
        assertEquals("endAppeal", DocumentTag.END_APPEAL.toString());
        assertEquals("endAppealAutomatically", DocumentTag.END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("cmaRequirements", DocumentTag.CMA_REQUIREMENTS.toString());
        assertEquals("cmaNotice", DocumentTag.CMA_NOTICE.toString());
        assertEquals("homeOfficeDecisionLetter", DocumentTag.HO_DECISION_LETTER.toString());
        assertEquals("recordOutOfTimeDecisionDocument", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT.toString());
        assertEquals("upperTribunalBundle", DocumentTag.UPPER_TRIBUNAL_BUNDLE.toString());
        assertEquals("appealReasons", DocumentTag.APPEAL_REASONS.toString());
        assertEquals("clarifyingQuestions", DocumentTag.CLARIFYING_QUESTIONS.toString());
        assertEquals("appealForm", DocumentTag.APPEAL_FORM.toString());
        assertEquals("noticeOfDecisionUtTransfer", DocumentTag.NOTICE_OF_DECISION_UT_TRANSFER.toString());
        assertEquals("bailSubmission", DocumentTag.BAIL_SUBMISSION.toString());
        assertEquals("uploadTheBailEvidenceDocs", DocumentTag.BAIL_EVIDENCE.toString());
        assertEquals("bailDecisionUnsigned", DocumentTag.BAIL_DECISION_UNSIGNED.toString());
        assertEquals("bailEndApplication", DocumentTag.BAIL_END_APPLICATION.toString());
        assertEquals("signedDecisionNotice", DocumentTag.SIGNED_DECISION_NOTICE.toString());
        assertEquals("uploadDocument", DocumentTag.UPLOAD_DOCUMENT.toString());
        assertEquals("uploadBailSummary", DocumentTag.BAIL_SUMMARY.toString());
        assertEquals("b1Document", DocumentTag.B1_DOCUMENT.toString());
        assertEquals("", DocumentTag.NONE.toString());
        assertEquals("requestCaseBuilding", DocumentTag.REQUEST_CASE_BUILDING.toString());
        assertEquals("internalAdaSuitability", DocumentTag.INTERNAL_ADA_SUITABILITY.toString());
        assertEquals("requestRespondentReview", DocumentTag.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("internalDetDecisionAndReasonsLetter", DocumentTag.INTERNAL_DET_DECISION_AND_REASONS_LETTER.toString());
        assertEquals("uploadTheAppealResponse", DocumentTag.UPLOAD_THE_APPEAL_RESPONSE.toString());
        assertEquals("hearingBundleReadyLetter", DocumentTag.HEARING_BUNDLE_READY_LETTER.toString());
        assertEquals("internalAppealSubmission", DocumentTag.INTERNAL_APPEAL_SUBMISSION.toString());
        assertEquals("internalEndAppealAutomatically", DocumentTag.INTERNAL_END_APPEAL_AUTOMATICALLY.toString());
        assertEquals("internalAppealFeeDueLetter", DocumentTag.INTERNAL_APPEAL_FEE_DUE_LETTER.toString());
        assertEquals("internalDetMarkAsPaidLetter", DocumentTag.INTERNAL_DET_MARK_AS_PAID_LETTER.toString());
        assertEquals("internalListCaseLetter", DocumentTag.INTERNAL_LIST_CASE_LETTER.toString());
        assertEquals("internalRequestHearingRequirementsLetter", DocumentTag.INTERNAL_REQUEST_HEARING_REQUIREMENTS_LETTER.toString());
        assertEquals("internalDetainedRequestHomeOfficeResponseReview", DocumentTag.INTERNAL_DETAINED_REQUEST_HO_RESPONSE_REVIEW.toString());
        assertEquals("internalDetainedEditCaseListingLetter", DocumentTag.INTERNAL_DETAINED_EDIT_CASE_LISTING_LETTER.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(55, DocumentTag.values().length);
    }
}
