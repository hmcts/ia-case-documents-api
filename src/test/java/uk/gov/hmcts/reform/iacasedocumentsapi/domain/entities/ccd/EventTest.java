package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static junit.framework.TestCase.assertEquals;

import org.junit.Test;

public class EventTest {

    @Test
    public void has_correct_values() {
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("sendDirection", Event.SEND_DIRECTION.toString());
        assertEquals("requestRespondentEvidence", Event.REQUEST_RESPONDENT_EVIDENCE.toString());
        assertEquals("uploadRespondentEvidence", Event.UPLOAD_RESPONDENT_EVIDENCE.toString());
        assertEquals("buildCase", Event.BUILD_CASE.toString());
        assertEquals("submitCase", Event.SUBMIT_CASE.toString());
        assertEquals("requestCaseEdit", Event.REQUEST_CASE_EDIT.toString());
        assertEquals("requestRespondentReview", Event.REQUEST_RESPONDENT_REVIEW.toString());
        assertEquals("addAppealResponse", Event.ADD_APPEAL_RESPONSE.toString());
        assertEquals("requestHearingRequirements", Event.REQUEST_HEARING_REQUIREMENTS.toString());
        assertEquals("draftHearingRequirements", Event.DRAFT_HEARING_REQUIREMENTS.toString());
        assertEquals("updateHearingRequirements", Event.UPDATE_HEARING_REQUIREMENTS.toString());
        assertEquals("changeDirectionDueDate", Event.CHANGE_DIRECTION_DUE_DATE.toString());
        assertEquals("uploadAdditionalEvidence", Event.UPLOAD_ADDITIONAL_EVIDENCE.toString());
        assertEquals("listCase", Event.LIST_CASE.toString());
        assertEquals("createCaseSummary", Event.CREATE_CASE_SUMMARY.toString());
        assertEquals("revertStateToAwaitingRespondentEvidence", Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE.toString());
        assertEquals("generateHearingBundle", Event.GENERATE_HEARING_BUNDLE.toString());
        assertEquals("generateDecisionAndReasons", Event.GENERATE_DECISION_AND_REASONS.toString());
        assertEquals("sendDecisionAndReasons", Event.SEND_DECISION_AND_REASONS.toString());
        assertEquals("editCaseListing", Event.EDIT_CASE_LISTING.toString());
        assertEquals("endAppeal", Event.END_APPEAL.toString());
        assertEquals("adjournHearingWithoutDate", Event.ADJOURN_HEARING_WITHOUT_DATE.toString());
        assertEquals("submitCmaRequirements", Event.SUBMIT_CMA_REQUIREMENTS.toString());
        assertEquals("listCma", Event.LIST_CMA.toString());
        assertEquals("editAppealAfterSubmit", Event.EDIT_APPEAL_AFTER_SUBMIT.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(28, Event.values().length);
    }
}
