package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EventTest {

    @Test
    public void has_correct_values() {
        assertEquals("startAppeal", Event.START_APPEAL.toString());
        assertEquals("editAppeal", Event.EDIT_APPEAL.toString());
        assertEquals("paymentAppeal", Event.PAYMENT_APPEAL.toString());
        assertEquals("payForAppeal", Event.PAY_FOR_APPEAL.toString());
        assertEquals("submitAppeal", Event.SUBMIT_APPEAL.toString());
        assertEquals("updatePaymentStatus", Event.UPDATE_PAYMENT_STATUS.toString());
        assertEquals("generateServiceRequest", Event.GENERATE_SERVICE_REQUEST.toString());
        assertEquals("unknown", Event.UNKNOWN.toString());
        assertEquals("requestCaseBuilding", Event.REQUEST_CASE_BUILDING.toString());
        assertEquals("uploadHomeOfficeAppealResponse", Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE.toString());
        assertEquals("recordOutOfTimeDecision", Event.RECORD_OUT_OF_TIME_DECISION.toString());
        assertEquals("markAppealPaid", Event.MARK_APPEAL_PAID.toString());
        assertEquals("recordRemissionDecision", Event.RECORD_REMISSION_DECISION.toString());
        assertEquals("requestHearingRequirementsFeature", Event.REQUEST_HEARING_REQUIREMENTS_FEATURE.toString());
        assertEquals("markAppealAsAda", Event.MARK_APPEAL_AS_ADA.toString());
        assertEquals("decideAnApplication", Event.DECIDE_AN_APPLICATION.toString());
        assertEquals("applyForFTPARespondent", Event.APPLY_FOR_FTPA_RESPONDENT.toString());
        assertEquals("transferOutOfAda", Event.TRANSFER_OUT_OF_ADA.toString());
        assertEquals("applyForFTPAAppellant", Event.APPLY_FOR_FTPA_APPELLANT.toString());
        assertEquals("residentJudgeFtpaDecision", Event.RESIDENT_JUDGE_FTPA_DECISION.toString());
        assertEquals("maintainCaseLinks", Event.MAINTAIN_CASE_LINKS.toString());
        assertEquals("uploadAddendumEvidenceAdminOfficer", Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER.toString());
        assertEquals("changeHearingCentre", Event.CHANGE_HEARING_CENTRE.toString());
        assertEquals("createCaseLink", Event.CREATE_CASE_LINK.toString());
        assertEquals("requestResponseAmend", Event.REQUEST_RESPONSE_AMEND.toString());
        assertEquals("uploadAdditionalEvidenceHomeOffice", Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("uploadAddendumEvidenceHomeOffice", Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE.toString());
        assertEquals("uploadAddendumEvidence", Event.UPLOAD_ADDENDUM_EVIDENCE.toString());
        assertEquals("reinstateAppeal", Event.REINSTATE_APPEAL.toString());
        assertEquals("updateHearingAdjustments", Event.UPDATE_HEARING_ADJUSTMENTS.toString());
        assertEquals("saveNotificationsToData", Event.SAVE_NOTIFICATIONS_TO_DATA.toString());
        assertEquals("manageFeeUpdate", Event.MANAGE_FEE_UPDATE.toString());
    }

    @Test
    public void if_this_test_fails_it_is_because_it_needs_updating_with_your_changes() {
        assertEquals(76, Event.values().length);
    }
}
