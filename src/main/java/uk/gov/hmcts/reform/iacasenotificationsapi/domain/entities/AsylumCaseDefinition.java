package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.em.Bundle;

public enum AsylumCaseDefinition {

    TRIBUNAL_DOCUMENTS(
        "tribunalDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>() {}),

    HEARING_RECORDING_DOCUMENTS(
        "hearingRecordingDocuments", new TypeReference<List<IdValue<HearingRecordingDocument>>>(){}),

    FINAL_DECISION_AND_REASONS_DOCUMENTS(
        "finalDecisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    DRAFT_DECISION_AND_REASONS_DOCUMENTS(
        "draftDecisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RESPONDENT_DOCUMENTS(
        "respondentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ADDENDUM_EVIDENCE_DOCUMENTS(
        "addendumEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    LEGAL_REPRESENTATIVE_DOCUMENTS(
        "legalRepresentativeDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ADDITIONAL_EVIDENCE_DOCUMENTS(
        "additionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    HEARING_DOCUMENTS(
        "hearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    HOME_OFFICE_REFERENCE_NUMBER(
            "homeOfficeReferenceNumber", new TypeReference<String>(){}),

    APPELLANT_GIVEN_NAMES(
            "appellantGivenNames", new TypeReference<String>(){}),

    APPELLANT_FAMILY_NAME(
            "appellantFamilyName", new TypeReference<String>(){}),

    LEGAL_REP_REFERENCE_NUMBER(
            "legalRepReferenceNumber", new TypeReference<String>(){}),

    APPEAL_REFERENCE_NUMBER(
            "appealReferenceNumber", new TypeReference<String>(){}),

    HEARING_CENTRE(
            "hearingCentre", new TypeReference<HearingCentre>(){}),

    DIRECTIONS(
            "directions", new TypeReference<List<IdValue<Direction>>>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_HOME_OFFICE_ALL(
            "currentCaseStateVisibleToHomeOfficeAll", new TypeReference<State>(){}),

    CURRENT_CASE_STATE_VISIBLE_TO_LEGAL_REPRESENTATIVE(
        "currentCaseStateVisibleToLegalRepresentative", new TypeReference<State>(){}),

    DIRECTION_EDIT_DATE_DUE(
            "directionEditDateDue", new TypeReference<String>(){}),

    DIRECTION_EDIT_EXPLANATION(
            "directionEditExplanation", new TypeReference<String>(){}),

    DIRECTION_EDIT_PARTIES(
            "directionEditParties", new TypeReference<Parties>(){}),

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
            "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_CHANGE_ORG_EMAIL_ADDRESS(
            "legalRepresentativeChangeOrgEmailAddress", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_NAME(
            "legalRepresentativeName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_NAME(
            "legalRepCompanyName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_ADDRESS(
            "legalRepCompanyAddress", new TypeReference<AddressUk>(){}),

    NOTIFICATIONS_SENT(
            "notificationsSent",  new TypeReference<List<IdValue<String>>>(){}),

    LIST_CASE_HEARING_DATE(
            "listCaseHearingDate",  new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_VULNERABILITIES(
            "listCaseRequirementsVulnerabilities",  new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_MULTIMEDIA(
            "listCaseRequirementsMultimedia",  new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT(
            "listCaseRequirementsSingleSexCourt",  new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT(
            "listCaseRequirementsInCameraCourt",  new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_OTHER(
            "listCaseRequirementsOther",  new TypeReference<String>(){}),

    ARIA_LISTING_REFERENCE(
            "ariaListingReference",  new TypeReference<String>(){}),

    LIST_CASE_HEARING_CENTRE(
            "listCaseHearingCentre",  new TypeReference<HearingCentre>(){}),

    END_APPEAL_OUTCOME(
        "endAppealOutcome", new TypeReference<String>(){}),

    END_APPEAL_OUTCOME_REASON(
        "endAppealOutcomeReason", new TypeReference<String>(){}),

    END_APPEAL_DATE(
        "endAppealDate", new TypeReference<String>(){}),

    END_APPEAL_APPROVER_NAME(
        "endAppealApproverName", new TypeReference<String>(){}),

    END_APPEAL_APPROVER_TYPE(
        "endAppealApproverType", new TypeReference<String>(){}),

    APPLICATION_DECISION_REASON(
        "applicationDecisionReason", new TypeReference<String>(){}),

    APPLICATION_TYPE(
        "applicationType", new TypeReference<String>(){}),

    APPLICATION_SUPPLIER(
        "applicationSupplier", new TypeReference<String>(){}),

    APPLICATION_DECISION(
        "applicationDecision", new TypeReference<String>(){}),

    IS_DECISION_ALLOWED(
        "isDecisionAllowed", new TypeReference<AppealDecision>(){}),

    JOURNEY_TYPE(
            "journeyType", new TypeReference<JourneyType>(){}),

    SUBSCRIPTIONS(
            "subscriptions", new TypeReference<List<IdValue<Subscriber>>>(){}),
    VULNERABILITIES_TRIBUNAL_RESPONSE(
        "vulnerabilitiesTribunalResponse", new TypeReference<String>(){}),

    MULTIMEDIA_TRIBUNAL_RESPONSE(
        "multimediaTribunalResponse", new TypeReference<String>(){}),

    SINGLE_SEX_COURT_TRIBUNAL_RESPONSE(
        "singleSexCourtTribunalResponse", new TypeReference<String>(){}),

    IN_CAMERA_COURT_TRIBUNAL_RESPONSE(
        "inCameraCourtTribunalResponse", new TypeReference<String>(){}),

    ADDITIONAL_TRIBUNAL_RESPONSE(
        "additionalTribunalResponse", new TypeReference<String>(){}),

    CASE_BUNDLES(
        "caseBundles", new TypeReference<List<IdValue<Bundle>>>(){}),

    SUBMIT_HEARING_REQUIREMENTS_AVAILABLE(
        "submitHearingRequirementsAvailable", new TypeReference<YesOrNo>(){}),

    SUBMISSION_OUT_OF_TIME(
        "submissionOutOfTime", new TypeReference<YesOrNo>(){}),

    REVIEW_TIME_EXTENSION_DATE(
        "reviewTimeExtensionDate", new TypeReference<String>(){}),
    REVIEW_TIME_EXTENSION_PARTY(
        "reviewTimeExtensionParty", new TypeReference<Parties>(){}),
    REVIEW_TIME_EXTENSION_REASON(
        "reviewTimeExtensionReason", new TypeReference<String>(){}),
    REVIEW_TIME_EXTENSION_DECISION(
        "reviewTimeExtensionDecision", new TypeReference<TimeExtensionDecision>(){}),
    REVIEW_TIME_EXTENSION_DECISION_REASON(
        "reviewTimeExtensionDecisionReason", new TypeReference<String>(){}),
    TIME_EXTENSIONS(
        "timeExtensions", new TypeReference<List<IdValue<TimeExtension>>>(){}),

    ADJOURN_HEARING_WITHOUT_DATE_REASONS(
        "adjournHearingWithoutDateReasons", new TypeReference<String>() {}),

    REASON_FOR_LINK_APPEAL(
        "reasonForLinkAppeal", new TypeReference<ReasonForLinkAppealOptions>() {}),
    EDIT_DOCUMENTS_REASON(
        "editDocumentsReason", new TypeReference<String>(){}),

    CASE_NOTES(
        "caseNotes", new TypeReference<List<IdValue<CaseNote>>>(){}),

    FTPA_APPELLANT_DECISION_OUTCOME_TYPE(
        "ftpaAppellantDecisionOutcomeType", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE(
        "ftpaAppellantRjDecisionOutcomeType", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_RESPONDENT_DECISION_OUTCOME_TYPE(
        "ftpaRespondentDecisionOutcomeType", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE(
        "ftpaRespondentRjDecisionOutcomeType", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_APPELLANT_DECISION_REMADE_RULE_32(
        "ftpaAppellantDecisionRemadeRule32", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_RESPONDENT_DECISION_REMADE_RULE_32(
        "ftpaRespondentDecisionRemadeRule32", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_APPELLANT_SUBMITTED(
        "ftpaAppellantSubmitted", new TypeReference<YesOrNo>(){}),

    FTPA_RESPONDENT_SUBMITTED(
        "ftpaRespondentSubmitted", new TypeReference<YesOrNo>(){}),

    FTPA_APPLICANT_TYPE(
        "ftpaApplicantType", new TypeReference<String>(){}),

    IS_FTPA_APPELLANT_DECIDED(
        "isFtpaAppellantDecided", new TypeReference<YesOrNo>(){}),

    IS_FTPA_RESPONDENT_DECIDED(
        "isFtpaRespondentDecided", new TypeReference<YesOrNo>(){}),

    HOME_OFFICE_FTPA_APPELLANT_INSTRUCT_STATUS(
        "homeOfficeFtpaAppellantInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_RESPONDENT_INSTRUCT_STATUS(
        "homeOfficeFtpaRespondentInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_APPEAL_DECIDED_INSTRUCT_STATUS(
        "homeOfficeAppealDecidedInstructStatus", new TypeReference<String>() {}),

    PAYMENT_STATUS(
        "paymentStatus", new TypeReference<PaymentStatus>(){}),

    APPEAL_TYPE(
        "appealType", new TypeReference<AppealType>(){}),

    SUBMIT_NOTIFICATION_STATUS(
        "submitNotificationStatus", new TypeReference<String>(){}),

    PA_APPEAL_TYPE_PAYMENT_OPTION(
        "paAppealTypePaymentOption", new TypeReference<String>(){}),
    EA_HU_APPEAL_TYPE_PAYMENT_OPTION(
            "eaHuAppealTypePaymentOption", new TypeReference<String>() {}),

    STATE_BEFORE_END_APPEAL(
            "stateBeforeEndAppeal", new TypeReference<State>(){}),

    REINSTATE_APPEAL_DATE(
            "reinstateAppealDate", new TypeReference<String>(){}),

    REINSTATE_APPEAL_REASON(
            "reinstateAppealReason", new TypeReference<String>(){}),

    REINSTATED_DECISION_MAKER(
            "reinstatedDecisionMaker", new TypeReference<String>(){}),

    MAKE_AN_APPLICATIONS(
            "makeAnApplications", new TypeReference<List<IdValue<MakeAnApplication>>>(){}),

    DECIDE_AN_APPLICATION_ID(
            "decideAnApplicationId", new TypeReference<String>(){}),

    IS_REHEARD_APPEAL_ENABLED(
        "isReheardAppealEnabled", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_SET_ASIDE_REHEARD_EXISTS(
        "caseFlagSetAsideReheardExists", new TypeReference<YesOrNo>() {}),

    CURRENT_CASE_STATE_VISIBLE_TO_JUDGE(
        "currentCaseStateVisibleToJudge", new TypeReference<State>(){}),

    IS_REMISSIONS_ENABLED(
        "isRemissionsEnabled", new TypeReference<YesOrNo>(){}),

    REMISSION_TYPE(
        "remissionType", new TypeReference<RemissionType>(){}),

    REMISSION_CLAIM(
        "remissionClaim", new TypeReference<String>(){}),

    REMISSION_DECISION(
        "remissionDecision", new TypeReference<RemissionDecision>(){}),

    AMOUNT_REMITTED(
        "amountRemitted", new TypeReference<String>(){}),

    AMOUNT_LEFT_TO_PAY(
        "amountLeftToPay", new TypeReference<String>(){}),

    HOME_OFFICE_HEARING_BUNDLE_READY_INSTRUCT_STATUS(
        "homeOfficeHearingBundleReadyInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_APPELLANT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaAppellantDecidedInstructStatus", new TypeReference<String>() {}),

    HOME_OFFICE_FTPA_RESPONDENT_DECIDED_INSTRUCT_STATUS(
        "homeOfficeFtpaRespondentDecidedInstructStatus", new TypeReference<String>() {}),

    REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE(
        "remoteVideoCallTribunalResponse", new TypeReference<String>() {}),

    APPELLANT_IN_UK(
        "appellantInUk", new TypeReference<YesOrNo>() {}),

    APPELLANT_DATE_OF_BIRTH(
        "appellantDateOfBirth", new TypeReference<String>() {}),

    EMAIL(
        "email", new TypeReference<String>(){}),

    MOBILE_NUMBER(
        "mobileNumber", new TypeReference<String>(){}),

    CONTACT_PREFERENCE(
        "contactPreference", new TypeReference<ContactPreference>(){}),

    FEE_UPDATE_RECORDED(
        "feeUpdateRecorded", new TypeReference<CheckValues<String>>(){}),

    CHANGE_ORGANISATION_REQUEST_FIELD(
        "changeOrganisationRequestField", new TypeReference<ChangeOrganisationRequest>(){}),

    FEE_UPDATE_COMPLETED_STAGES(
        "feeUpdateCompletedStages", new TypeReference<List<String>>(){}),

    OUT_OF_TIME_DECISION_TYPE(
        "outOfTimeDecisionType", new TypeReference<OutOfTimeDecisionType>(){}),
    ;

    private final String value;
    private final TypeReference typeReference;

    AsylumCaseDefinition(String value, TypeReference typeReference) {
        this.value = value;
        this.typeReference = typeReference;
    }

    public String value() {
        return value;
    }

    public TypeReference getTypeReference() {
        return typeReference;
    }
}
