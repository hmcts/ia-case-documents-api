package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CheckValues;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.caselinking.CaseLink;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.em.Bundle;

import java.util.List;

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

    APPELLANT_ADDRESS(
        "appellantAddress", new TypeReference<AddressUk>(){}),

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

    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),

    LEGAL_REP_FAMILY_NAME(
        "legalRepFamilyName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY_NAME(
            "legalRepCompanyName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),

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

    IS_CASE_USING_LOCATION_REF_DATA(
        "isCaseUsingLocationRefData", new TypeReference<YesOrNo>(){}),

    LIST_CASE_HEARING_CENTRE(
            "listCaseHearingCentre",  new TypeReference<HearingCentre>(){}),

    LIST_CASE_HEARING_CENTRE_ADDRESS(
        "listCaseHearingCentreAddress",  new TypeReference<String>(){}),

    APPEAL_REVIEW_OUTCOME(
        "appealReviewOutcome", new TypeReference<AppealReviewOutcome>(){}),

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

    PAST_EXPERIENCES_TRIBUNAL_RESPONSE(
            "pastExperiencesTribunalResponse", new TypeReference<String>(){}),

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

    RELIST_CASE_IMMEDIATELY(
            "relistCaseImmediately", new TypeReference<YesOrNo>(){}),

    REASON_FOR_LINK_APPEAL(
        "reasonForLinkAppeal", new TypeReference<ReasonForLinkAppealOptions>() {}),

    EDIT_DOCUMENTS_REASON(
        "editDocumentsReason", new TypeReference<String>(){}),

    REMOVE_APPEAL_FROM_ONLINE_REASON(
        "removeAppealFromOnlineReason", new TypeReference<String>(){}),

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
        "ftpaApplicantType", new TypeReference<ApplicantType>(){}),

    IS_FTPA_APPELLANT_DECIDED(
        "isFtpaAppellantDecided", new TypeReference<YesOrNo>(){}),

    IS_FTPA_RESPONDENT_DECIDED(
        "isFtpaRespondentDecided", new TypeReference<YesOrNo>(){}),
    FTPA_RESPONDENT_DECISION_DOCUMENT(
        "ftpaRespondentDecisionDocument", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),
    FTPA_RESPONDENT_GROUNDS_DOCUMENTS(
        "ftpaRespondentGroundsDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

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
    REMISSION_OPTION(
        "remissionOption", new TypeReference<RemissionOption>(){}),
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

    APPEAL_OUT_OF_COUNTRY(
        "appealOutOfCountry", new TypeReference<YesOrNo>() {}),

    APPELLANT_DATE_OF_BIRTH(
        "appellantDateOfBirth", new TypeReference<String>() {}),

    EMAIL(
        "email", new TypeReference<String>(){}),

    INTERNAL_APPELLANT_EMAIL(
        "internalAppellantEmail", new TypeReference<String>(){}),

    MOBILE_NUMBER(
        "mobileNumber", new TypeReference<String>(){}),

    INTERNAL_APPELLANT_MOBILE_NUMBER(
        "internalAppellantMobileNumber", new TypeReference<String>(){}),

    CONTACT_PREFERENCE(
        "contactPreference", new TypeReference<ContactPreference>(){}),

    CONTACT_PREFERENCE_UN_REP(
            "contactPreferenceUnRep", new TypeReference<List<String>>(){}),

    FEE_UPDATE_RECORDED(
        "feeUpdateRecorded", new TypeReference<CheckValues<String>>(){}),

    CHANGE_ORGANISATION_REQUEST_FIELD(
        "changeOrganisationRequestField", new TypeReference<ChangeOrganisationRequest>(){}),

    FEE_UPDATE_COMPLETED_STAGES(
        "feeUpdateCompletedStages", new TypeReference<List<String>>(){}),

    OUT_OF_TIME_DECISION_TYPE(
        "outOfTimeDecisionType", new TypeReference<OutOfTimeDecisionType>(){}),

    HAS_SERVICE_REQUEST_ALREADY(
        "hasServiceRequestAlready", new TypeReference<YesOrNo>(){}),
    APPELLANT_PIN_IN_POST(
        "appellantPinInPost", new TypeReference<PinInPostDetails>(){}),

    APPELLANT_HAS_FIXED_ADDRESS("appellantHasFixedAddress", new TypeReference<YesOrNo>(){}),

    IS_INTEGRATED(
            "isIntegrated", new TypeReference<YesOrNo>(){}),

    CASE_LINKS(
            "caseLinks", new TypeReference<List<IdValue<CaseLink>>>(){}),

    IS_DLRM_SET_ASIDE_ENABLED(
            "isDlrmSetAsideEnabled", new TypeReference<YesOrNo>(){}),

    IS_DLRM_FEE_REMISSION_ENABLED(
            "isDlrmFeeRemissionEnabled", new TypeReference<YesOrNo>(){}),
    IS_DLRM_FEE_REFUND_ENABLED(
        "isDlrmFeeRefundEnabled", new TypeReference<YesOrNo>(){}),
    UPDATE_TRIBUNAL_DECISION_AND_REASONS(
            "updateTribunalDecisionAndReasons", new TypeReference<YesOrNo>(){}),
    UPDATE_TRIBUNAL_DECISION_AND_REASONS_FINAL_CHECK(
            "updateTribunalDecisionAndReasonsFinalCheck", new TypeReference<YesOrNo>(){}),
    UPDATE_TRIBUNAL_DECISION_LIST(
            "updateTribunalDecisionList", new TypeReference<String>(){}),
    TYPES_OF_UPDATE_TRIBUNAL_DECISION(
            "typesOfUpdateTribunalDecision", new TypeReference<DynamicList>(){}),
    UPDATED_APPEAL_DECISION(
            "updatedAppealDecision", new TypeReference<String>(){}),

    REMOTE_HEARING_DECISION_FOR_DISPLAY(
        "remoteHearingDecisionForDisplay", new TypeReference<String>() {}),

    MULTIMEDIA_DECISION_FOR_DISPLAY(
        "multimediaDecisionForDisplay", new TypeReference<String>() {}),

    SINGLE_SEX_COURT_DECISION_FOR_DISPLAY(
        "singleSexCourtDecisionForDisplay", new TypeReference<String>() {}),

    IN_CAMERA_COURT_DECISION_FOR_DISPLAY(
        "inCameraCourtDecisionForDisplay", new TypeReference<String>() {}),

    VULNERABILITIES_DECISION_FOR_DISPLAY(
        "vulnerabilitiesDecisionForDisplay", new TypeReference<String>() {}),

    OTHER_DECISION_FOR_DISPLAY(
        "otherDecisionForDisplay", new TypeReference<String>() {}),

    REQUEST_FEE_REMISSION_DATE(
        "requestFeeRemissionDate", new TypeReference<String>(){}),
    REMISSION_REJECTED_DATE_PLUS_14DAYS(
        "remissionRejectedDatePlus14days", new TypeReference<String>(){}),
    FEE_AMOUNT_GBP(
        "feeAmountGbp", new TypeReference<String>(){}),
    NEW_FEE_AMOUNT(
        "newFeeAmount", new TypeReference<String>(){}),
    PREVIOUS_FEE_AMOUNT_GBP(
        "previousFeeAmountGbp", new TypeReference<String>(){}),
    FEE_UPDATE_REASON(
        "feeUpdateReason", new TypeReference<FeeUpdateReason>(){}),
    MANAGE_FEE_REQUESTED_AMOUNT(
        "manageFeeRequestedAmount", new TypeReference<String>(){}),
    FEE_UPDATE_TRIBUNAL_ACTION(
        "feeUpdateTribunalAction", new TypeReference<FeeTribunalAction>(){}),
    IS_ACCELERATED_DETAINED_APPEAL(
        "isAcceleratedDetainedAppeal", new TypeReference<YesOrNo>(){}),
    SUITABILITY_REVIEW_DECISION(
        "suitabilityReviewDecision", new TypeReference<AdaSuitabilityReviewDecision>(){}),
    TRANSFER_OUT_OF_ADA_REASON(
            "transferOutOfAdaReason", new TypeReference<String>(){}),
    IS_ADMIN(
        "isAdmin", new TypeReference<YesOrNo>() {}),
    IS_ARIA_MIGRATED(
        "isAriaMigrated", new TypeReference<YesOrNo>() {}),
    UT_APPEAL_REFERENCE_NUMBER(
        "utAppealReferenceNumber", new TypeReference<String>() {}),
    NOTIFICATION_ATTACHMENT_DOCUMENTS(
            "notificationAttachmentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>() {}),
    IRC_NAME(
        "ircName", new TypeReference<String>(){}),
    DETENTION_FACILITY(
        "detentionFacility", new TypeReference<String>(){}),
    PREVIOUS_DETENTION_LOCATION(
            "previousDetentionLocation", new TypeReference<String>() {}),
    PRISON_NAME(
            "prisonName", new TypeReference<String>(){}),
    OTHER_DETENTION_FACILITY_NAME(
            "otherDetentionFacilityName", new TypeReference<OtherDetentionFacilityName>(){}),
    APPELLANT_IN_DETENTION(
            "appellantInDetention", new TypeReference<YesOrNo>(){}),
    IS_APPELLANT_RESPONDENT(
            "isAppellantRespondent", new TypeReference<String>(){}),
    IS_EJP("isEjp", new TypeReference<YesOrNo>() {}),
    LEGAL_REP_COMPANY_EJP(
            "legalRepCompanyEjp", new TypeReference<String>() {}),
    LEGAL_REP_NAME_EJP(
            "legalRepNameEjp", new TypeReference<String>() {}),
    LEGAL_REP_GIVEN_NAME_EJP(
            "legalRepGivenNameEjp", new TypeReference<String>() {}),
    LEGAL_REP_FAMILY_NAME_EJP(
            "legalRepFamilyNameEjp", new TypeReference<String>() {}),
    LEGAL_REP_EMAIL_EJP(
            "legalRepEmailEjp", new TypeReference<String>() {}),
    LEGAL_REP_REFERENCE_EJP(
            "legalRepReferenceEjp", new TypeReference<String>() {}),
    IS_LEGALLY_REPRESENTED_EJP(
            "isLegallyRepresentedEjp", new TypeReference<YesOrNo>() {}),
    APPLIES_FOR_COSTS(
            "appliesForCosts", new TypeReference<List<IdValue<ApplyForCosts>>>(){}),
    UPPER_TRIBUNAL_REFERENCE_NUMBER(
            "upperTribunalReferenceNumber", new TypeReference<String>() {}),
    CCD_REFERENCE_NUMBER_FOR_DISPLAY(
        "ccdReferenceNumberForDisplay", new TypeReference<String>() {}),
    RESPOND_TO_COSTS_LIST(
        "respondToCostsList", new TypeReference<DynamicList>(){}),
    ADD_EVIDENCE_FOR_COSTS_LIST(
        "addEvidenceForCostsList", new TypeReference<DynamicList>() {}),
    DECIDE_COSTS_APPLICATION_LIST(
        "decideCostsApplicationList", new TypeReference<DynamicList>(){}
    ),

    SEND_DIRECTION_DATE_DUE(
        "sendDirectionDateDue", new TypeReference<String>() {}),

    SOURCE_OF_REMITTAL(
        "sourceOfRemittal", new TypeReference<SourceOfRemittal>(){}),

    LETTER_BUNDLE_DOCUMENTS(
        "letterBundleDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    PAID_AMOUNT(
        "paidAmount", new TypeReference<String>(){}),

    IS_REMOTE_HEARING(
        "isRemoteHearing", new TypeReference<YesOrNo>(){}),

    IS_VIRTUAL_HEARING("isVirtualHearing", new TypeReference<YesOrNo>(){}),

    NOTIFICATIONS("notifications", new TypeReference<List<IdValue<StoredNotification>>>(){}),
  
    LISTING_LOCATION(
        "listingLocation", new TypeReference<DynamicList>(){}),

    IS_DECISION_WITHOUT_HEARING("isDecisionWithoutHearing", new TypeReference<YesOrNo>(){}),

    APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J(
        "appellantHasFixedAddressAdminJ", new TypeReference<YesOrNo>(){}),

    ADDRESS_LINE_1_ADMIN_J(
        "addressLine1AdminJ", new TypeReference<String>(){}),

    ADDRESS_LINE_2_ADMIN_J(
        "addressLine2AdminJ", new TypeReference<String>(){}),

    ADDRESS_LINE_3_ADMIN_J(
        "addressLine3AdminJ", new TypeReference<String>(){}),

    ADDRESS_LINE_4_ADMIN_J(
        "addressLine4AdminJ", new TypeReference<String>(){}),

    COUNTRY_GOV_UK_OOC_ADMIN_J(
        "countryGovUkOocAdminJ", new TypeReference<NationalityFieldValue>(){}),

    REMISSION_DECISION_REASON(
        "remissionDecisionReason", new TypeReference<String>(){}),

    LATE_REMISSION_TYPE(
        "lateRemissionType", new TypeReference<RemissionType>(){}),

    FTPA_APPELLANT_DECISION_REMADE_RULE_32_TEXT(
        "ftpaAppellantDecisionRemadeRule32Text", new TypeReference<String>(){}),

    FTPA_RESPONDENT_DECISION_REMADE_RULE_32_TEXT(
        "ftpaRespondentDecisionRemadeRule32Text", new TypeReference<String>(){}),

    LEGAL_REP_ADDRESS_U_K(
        "legalRepAddressUK", new TypeReference<AddressUk>(){}),

    OOC_ADDRESS_LINE_1(
        "oocAddressLine1", new TypeReference<String>(){}),

    OOC_ADDRESS_LINE_2(
        "oocAddressLine2", new TypeReference<String>(){}),

    OOC_ADDRESS_LINE_3(
        "oocAddressLine3", new TypeReference<String>(){}),

    OOC_ADDRESS_LINE_4(
        "oocAddressLine4", new TypeReference<String>(){}),

    OOC_COUNTRY_LINE(
        "oocCountryLine", new TypeReference<String>(){}),

    OOC_LR_COUNTRY_GOV_UK_ADMIN_J(
        "oocLrCountryGovUkAdminJ", new TypeReference<NationalityFieldValue>(){}),

    LEGAL_REP_HAS_ADDRESS(
        "legalRepHasAddress", new TypeReference<YesOrNo>(){}),

    //Paper journey legal representative email
    LEGAL_REP_EMAIL(
        "legalRepEmail", new TypeReference<String>(){}),

    COUNTRY_ADMIN_J(
            "countryAdminJ", new TypeReference<String>(){}),

    APPELLANTS_REPRESENTATION(
        "appellantsRepresentation", new TypeReference<YesOrNo>(){}),

    IS_LATE_REMISSION_REQUEST(
            "isLateRemissionRequest", new TypeReference<YesOrNo>(){}),

    PREVIOUS_DECISION_HEARING_FEE_OPTION(
            "previousDecisionHearingFeeOption", new TypeReference<String>(){}),

    DECISION_HEARING_FEE_OPTION(
            "decisionHearingFeeOption", new TypeReference<String>(){}),

    TTL(
            "TTL", new TypeReference<TtlCcdObject>(){}),
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
