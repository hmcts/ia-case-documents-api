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
        assertEquals("finalDecisionAndReasonsDocument", DocumentTag.FINAL_DECISION_AND_REASONS_DOCUMENT.toString());
        assertEquals("recordOutOfTimeDecisionDocument", DocumentTag.RECORD_OUT_OF_TIME_DECISION_DOCUMENT.toString());
        assertEquals("upperTribunalBundle", DocumentTag.UPPER_TRIBUNAL_BUNDLE.toString());
        assertEquals("appealReasons", DocumentTag.APPEAL_REASONS.toString());
        assertEquals("clarifyingQuestions", DocumentTag.CLARIFYING_QUESTIONS.toString());
        assertEquals("bailSubmission", DocumentTag.BAIL_SUBMISSION.toString());
        assertEquals("uploadTheBailEvidenceDocs", DocumentTag.BAIL_EVIDENCE.toString());
        assertEquals("bailDecisionUnsigned", DocumentTag.BAIL_DECISION_UNSIGNED.toString());
        assertEquals("bailEndApplication", DocumentTag.BAIL_END_APPLICATION.toString());
        assertEquals("signedDecisionNotice", DocumentTag.SIGNED_DECISION_NOTICE.toString());
        assertEquals("uploadDocument", DocumentTag.UPLOAD_DOCUMENT.toString());
        assertEquals("uploadBailSummary", DocumentTag.BAIL_SUMMARY.toString());
        assertEquals("b1Document", DocumentTag.B1_DOCUMENT.toString());
        assertEquals("bailNoticeOfHearing", DocumentTag.BAIL_NOTICE_OF_HEARING.toString());
        assertEquals("", DocumentTag.NONE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(38, DocumentTag.values().length);
    }
}
