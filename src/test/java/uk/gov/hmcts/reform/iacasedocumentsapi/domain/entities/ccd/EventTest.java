package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class EventTest {

    @ParameterizedTest
    @MethodSource("eventMapping")
    void has_correct_values(String expected, CaseType caseType, Event event) {
        assertEquals(expected, event.toString());
        assertEquals(caseType, event.getCaseType());
    }

    @Test
    void if_this_test_fails_it_is_because_eventMapping_needs_updating_with_your_changes() {
        List<Event> eventMappingStrings = eventMapping().map(arg -> arg.get()[2])
            .map(Event.class::cast)
            .toList();
        List<Event> missingEvents = Arrays.stream(Event.values())
            .filter(event -> !eventMappingStrings.contains(event)).toList();
        assertTrue(missingEvents.isEmpty(), "The following events are missing from the eventMapping method: " + missingEvents);
    }

    static Stream<Arguments> eventMapping() {
        return Stream.of(
            Arguments.of("startAppeal", CaseType.ASYLUM, Event.START_APPEAL),
            Arguments.of("submitAppeal", CaseType.ASYLUM, Event.SUBMIT_APPEAL),
            Arguments.of("editAppeal", CaseType.ASYLUM, Event.EDIT_APPEAL),
            Arguments.of("payAndSubmitAppeal", CaseType.ASYLUM, Event.PAY_AND_SUBMIT_APPEAL),
            Arguments.of("sendDirection", CaseType.ASYLUM, Event.SEND_DIRECTION),
            Arguments.of("requestRespondentEvidence", CaseType.ASYLUM, Event.REQUEST_RESPONDENT_EVIDENCE),
            Arguments.of("uploadRespondentEvidence", CaseType.ASYLUM, Event.UPLOAD_RESPONDENT_EVIDENCE),
            Arguments.of("buildCase", CaseType.ASYLUM, Event.BUILD_CASE),
            Arguments.of("submitCase", CaseType.ASYLUM, Event.SUBMIT_CASE),
            Arguments.of("requestCaseEdit", CaseType.ASYLUM, Event.REQUEST_CASE_EDIT),
            Arguments.of("requestRespondentReview", CaseType.ASYLUM, Event.REQUEST_RESPONDENT_REVIEW),
            Arguments.of("addAppealResponse", CaseType.ASYLUM, Event.ADD_APPEAL_RESPONSE),
            Arguments.of("requestHearingRequirements", CaseType.ASYLUM, Event.REQUEST_HEARING_REQUIREMENTS),
            Arguments.of("draftHearingRequirements", CaseType.ASYLUM, Event.DRAFT_HEARING_REQUIREMENTS),
            Arguments.of("changeDirectionDueDate", CaseType.ASYLUM, Event.CHANGE_DIRECTION_DUE_DATE),
            Arguments.of("uploadAdditionalEvidence", CaseType.ASYLUM, Event.UPLOAD_ADDITIONAL_EVIDENCE),
            Arguments.of("listCase", CaseType.ASYLUM, Event.LIST_CASE),
            Arguments.of("createCaseSummary", CaseType.ASYLUM, Event.CREATE_CASE_SUMMARY),
            Arguments.of("revertStateToAwaitingRespondentEvidence", CaseType.ASYLUM, Event.REVERT_STATE_TO_AWAITING_RESPONDENT_EVIDENCE),
            Arguments.of("generateHearingBundle", CaseType.ASYLUM, Event.GENERATE_HEARING_BUNDLE),
            Arguments.of("asyncStitchingComplete", CaseType.ASYLUM, Event.ASYNC_STITCHING_COMPLETE),
            Arguments.of("requestHearingRequirementsFeature", CaseType.ASYLUM, Event.REQUEST_HEARING_REQUIREMENTS_FEATURE),
            Arguments.of("customiseHearingBundle", CaseType.ASYLUM, Event.CUSTOMISE_HEARING_BUNDLE),
            Arguments.of("generateUpdatedHearingBundle", CaseType.ASYLUM, Event.GENERATE_UPDATED_HEARING_BUNDLE),
            Arguments.of("generateDecisionAndReasons", CaseType.ASYLUM, Event.GENERATE_DECISION_AND_REASONS),
            Arguments.of("adaSuitabilityReview", CaseType.ASYLUM, Event.ADA_SUITABILITY_REVIEW),
            Arguments.of("sendDecisionAndReasons", CaseType.ASYLUM, Event.SEND_DECISION_AND_REASONS),
            Arguments.of("editCaseListing", CaseType.ASYLUM, Event.EDIT_CASE_LISTING),
            Arguments.of("manageFeeUpdate", CaseType.ASYLUM, Event.MANAGE_FEE_UPDATE),
            Arguments.of("endAppeal", CaseType.ASYLUM, Event.END_APPEAL),
            Arguments.of("endAppealAutomatically", CaseType.ASYLUM, Event.END_APPEAL_AUTOMATICALLY),
            Arguments.of("adjournHearingWithoutDate", CaseType.ASYLUM, Event.ADJOURN_HEARING_WITHOUT_DATE),
            Arguments.of("submitCmaRequirements", CaseType.ASYLUM, Event.SUBMIT_CMA_REQUIREMENTS),
            Arguments.of("listCma", CaseType.ASYLUM, Event.LIST_CMA),
            Arguments.of("editAppealAfterSubmit", CaseType.ASYLUM, Event.EDIT_APPEAL_AFTER_SUBMIT),
            Arguments.of("generateUpperTribunalBundle", CaseType.ASYLUM, Event.GENERATE_UPPER_TRIBUNAL_BUNDLE),
            Arguments.of("submitReasonsForAppeal", CaseType.ASYLUM, Event.SUBMIT_REASONS_FOR_APPEAL),
            Arguments.of("submitClarifyingQuestionAnswers", CaseType.ASYLUM, Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS),
            Arguments.of("recordAdjournmentDetails", CaseType.ASYLUM, Event.RECORD_ADJOURNMENT_DETAILS),
            Arguments.of("updateTribunalDecision", CaseType.ASYLUM, Event.UPDATE_TRIBUNAL_DECISION),
            Arguments.of("requestCaseBuilding", CaseType.ASYLUM, Event.REQUEST_CASE_BUILDING),
            Arguments.of("uploadHomeOfficeAppealResponse", CaseType.ASYLUM, Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE),
            Arguments.of("recordOutOfTimeDecision", CaseType.ASYLUM, Event.RECORD_OUT_OF_TIME_DECISION),
            Arguments.of("markAppealPaid", CaseType.ASYLUM, Event.MARK_APPEAL_PAID),
            Arguments.of("recordRemissionDecision", CaseType.ASYLUM, Event.RECORD_REMISSION_DECISION),
            Arguments.of("requestResponseReview", CaseType.ASYLUM, Event.REQUEST_RESPONSE_REVIEW),
            Arguments.of("markAppealAsAda", CaseType.ASYLUM, Event.MARK_APPEAL_AS_ADA),
            Arguments.of("applyForFTPARespondent", CaseType.ASYLUM, Event.APPLY_FOR_FTPA_RESPONDENT),
            Arguments.of("applyForFTPAAppellant", CaseType.ASYLUM, Event.APPLY_FOR_FTPA_APPELLANT),
            Arguments.of("decideAnApplication", CaseType.ASYLUM, Event.DECIDE_AN_APPLICATION),
            Arguments.of("transferOutOfAda", CaseType.ASYLUM, Event.TRANSFER_OUT_OF_ADA),
            Arguments.of("residentJudgeFtpaDecision", CaseType.ASYLUM, Event.RESIDENT_JUDGE_FTPA_DECISION),
            Arguments.of("maintainCaseLinks", CaseType.ASYLUM, Event.MAINTAIN_CASE_LINKS),
            Arguments.of("uploadAddendumEvidenceAdminOfficer", CaseType.ASYLUM, Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER),
            Arguments.of("changeHearingCentre", CaseType.ASYLUM, Event.CHANGE_HEARING_CENTRE),
            Arguments.of("createCaseLink", CaseType.ASYLUM, Event.CREATE_CASE_LINK),
            Arguments.of("requestResponseAmend", CaseType.ASYLUM, Event.REQUEST_RESPONSE_AMEND),
            Arguments.of("uploadAdditionalEvidenceHomeOffice", CaseType.ASYLUM, Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE),
            Arguments.of("uploadAddendumEvidenceHomeOffice", CaseType.ASYLUM, Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE),
            Arguments.of("uploadAddendumEvidence", CaseType.ASYLUM, Event.UPLOAD_ADDENDUM_EVIDENCE),
            Arguments.of("reinstateAppeal", CaseType.ASYLUM, Event.REINSTATE_APPEAL),
            Arguments.of("decisionWithoutHearing", CaseType.ASYLUM, Event.DECISION_WITHOUT_HEARING),
            Arguments.of("markAppealAsRemitted", CaseType.ASYLUM, Event.MARK_APPEAL_AS_REMITTED),
            Arguments.of("submitApplication", CaseType.BAIL, Event.SUBMIT_APPLICATION),
            Arguments.of("regenerateBailSubmissionDocument", CaseType.BAIL, Event.REGENERATE_BAIL_SUBMISSION_DOCUMENT),
            Arguments.of("recordTheDecision", CaseType.BAIL, Event.RECORD_THE_DECISION),
            Arguments.of("endApplication", CaseType.BAIL, Event.END_APPLICATION),
            Arguments.of("makeNewApplication", CaseType.BAIL, Event.MAKE_NEW_APPLICATION),
            Arguments.of("editBailApplicationAfterSubmit", CaseType.BAIL, Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT),
            Arguments.of("uploadSignedDecisionNotice", CaseType.BAIL, Event.UPLOAD_SIGNED_DECISION_NOTICE),
            Arguments.of("uploadSignedDecisionNoticeConditionalGrant", CaseType.BAIL, Event.UPLOAD_SIGNED_DECISION_NOTICE_CONDITIONAL_GRANT),
            Arguments.of("caseListing", CaseType.BAIL, Event.CASE_LISTING),
            Arguments.of("saveNotificationsToDataBail", CaseType.BAIL, Event.SAVE_NOTIFICATIONS_TO_DATA_BAIL),
            Arguments.of("updateHearingRequirements", CaseType.ASYLUM, Event.UPDATE_HEARING_REQUIREMENTS),
            Arguments.of("updateHearingAdjustments", CaseType.ASYLUM, Event.UPDATE_HEARING_ADJUSTMENTS),
            Arguments.of("saveNotificationsToData", CaseType.ASYLUM, Event.SAVE_NOTIFICATIONS_TO_DATA),
            Arguments.of("decideFtpaApplication", CaseType.ASYLUM, Event.DECIDE_FTPA_APPLICATION),
            Arguments.of("removeRepresentation", CaseType.ASYLUM, Event.REMOVE_REPRESENTATION),
            Arguments.of("removeLegalRepresentative", CaseType.ASYLUM, Event.REMOVE_LEGAL_REPRESENTATIVE),
            Arguments.of("unknown", CaseType.UNKNOWN, Event.UNKNOWN)
        );
    }
}
