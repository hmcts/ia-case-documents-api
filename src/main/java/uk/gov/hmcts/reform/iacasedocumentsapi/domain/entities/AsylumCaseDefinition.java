package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.caselinking.CaseLink;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.enties.em.Bundle;

public enum AsylumCaseDefinition {

    BUNDLE_CONFIGURATION(
        "bundleConfiguration", new TypeReference<String>(){}),

    CASE_BUNDLES(
        "caseBundles", new TypeReference<List<IdValue<Bundle>>>(){}),

    STITCHING_STATUS(
        "stitchingStatus", new TypeReference<String>(){}),

    STITCHING_STATUS_UPPER_TRIBUNAL(
        "stitchingStatusUpperTribunal", new TypeReference<String>(){}),

    BUNDLE_FILE_NAME_PREFIX(
        "bundleFileNamePrefix", new TypeReference<String>(){}),

    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_DECISION_DATE(
        "homeOfficeDecisionDate", new TypeReference<String>(){}),

    DECISION_LETTER_RECEIVED_DATE(
            "decisionLetterReceivedDate", new TypeReference<String>(){}),

    APPEAL_OUT_OF_COUNTRY(
            "appealOutOfCountry", new TypeReference<YesOrNo>() {}),

    HAS_SPONSOR(
            "hasSponsor", new TypeReference<YesOrNo>(){}),

    GWF_REFERENCE_NUMBER(
            "gwfReferenceNumber", new TypeReference<String>(){}),

    DATE_ENTRY_CLEARANCE_DECISION(
            "dateEntryClearanceDecision", new TypeReference<String>(){}),

    DATE_CLIENT_LEAVE_UK(
            "dateClientLeaveUk", new TypeReference<String>(){}),

    OUT_OF_COUNTRY_DECISION_TYPE(
            "outOfCountryDecisionType", new TypeReference<OutOfCountryDecisionType>(){}),

    HAS_CORRESPONDENCE_ADDRESS(
            "hasCorrespondenceAddress", new TypeReference<YesOrNo>(){}),

    APPELLANT_OUT_OF_COUNTRY_ADDRESS(
            "appellantOutOfCountryAddress", new TypeReference<String>(){}),

    SPONSOR_GIVEN_NAMES(
            "sponsorGivenNames", new TypeReference<String>(){}),

    SPONSOR_FAMILY_NAME(
            "sponsorFamilyName", new TypeReference<String>(){}),

    SPONSOR_CONTACT_PREFERENCE(
            "sponsorContactPreference", new TypeReference<ContactPreference>(){}),

    SPONSOR_EMAIL(
            "sponsorEmail", new TypeReference<String>(){}),

    SPONSOR_MOBILE_NUMBER(
            "sponsorMobileNumber", new TypeReference<String>(){}),

    SPONSOR_ADDRESS_FOR_DISPLAY(
            "sponsorAddressForDisplay", new TypeReference<String>(){}),

    APPELLANT_TITLE(
        "appellantTitle", new TypeReference<String>(){}),

    APPELLANT_GIVEN_NAMES(
        "appellantGivenNames", new TypeReference<String>(){}),

    APPELLANT_FAMILY_NAME(
        "appellantFamilyName", new TypeReference<String>(){}),

    APPELLANT_DATE_OF_BIRTH(
        "appellantDateOfBirth", new TypeReference<String>(){}),

    APPELLANT_NATIONALITIES(
        "appellantNationalities", new TypeReference<List<IdValue<Map<String, String>>>>(){}),

    APPELLANT_HAS_FIXED_ADDRESS(
        "appellantHasFixedAddress", new TypeReference<YesOrNo>(){}),

    APPELLANT_ADDRESS(
        "appellantAddress", new TypeReference<AddressUk>(){}),

    APPEAL_TYPE(
        "appealType", new TypeReference<AsylumAppealType>(){}),

    REASONS_FOR_APPEAL_DECISION(
        "reasonsForAppealDecision", new TypeReference<String>(){}),

    REASONS_FOR_APPEAL_DOCUMENTS(
        "reasonsForAppealDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    JOURNEY_TYPE(
            "journeyType", new TypeReference<JourneyType>(){}),

    HEARING_CENTRE(
            "hearingCentre", new TypeReference<HearingCentre>(){}),

    APPEAL_SUBMISSION_DATE(
        "appealSubmissionDate", new TypeReference<String>(){}),

    REMOVAL_ORDER_OPTIONS(
            "removalOrderOptions", new TypeReference<YesOrNo>(){}),

    REMOVAL_ORDER_DATE(
            "removalOrderDate", new TypeReference<String>(){}),
    NEW_MATTERS(
        "newMatters", new TypeReference<String>(){}),

    CONTACT_PREFERENCE(
        "contactPreference", new TypeReference<ContactPreference>(){}),

    OTHER_APPEALS(
        "otherAppeals", new TypeReference<List<IdValue<Map<String, String>>>>(){}),

    LEGAL_REP_REFERENCE_NUMBER(
        "legalRepReferenceNumber", new TypeReference<String>(){}),

    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),

    APPEAL_GROUNDS_FOR_DISPLAY(
        "appealGroundsForDisplay", new TypeReference<List<String>>(){}),

    HEARING_DOCUMENTS(
        "hearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    REHEARD_HEARING_DOCUMENTS(
        "reheardHearingDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
        "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_DOCUMENTS(
        "legalRepresentativeDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    EMAIL(
        "email", new TypeReference<String>(){}),

    MOBILE_NUMBER(
        "mobileNumber", new TypeReference<String>(){}),

    RESPONDENT_DOCUMENTS(
        "respondentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CASE_ARGUMENT_EVIDENCE(
        "caseArgumentEvidence", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    LIST_CASE_HEARING_CENTRE(
        "listCaseHearingCentre", new TypeReference<HearingCentre>(){}),

    LIST_CASE_HEARING_DATE(
        "listCaseHearingDate", new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_VULNERABILITIES(
        "listCaseRequirementsVulnerabilities", new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_MULTIMEDIA(
        "listCaseRequirementsMultimedia", new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT(
        "listCaseRequirementsSingleSexCourt", new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT(
        "listCaseRequirementsInCameraCourt", new TypeReference<String>(){}),

    LIST_CASE_REQUIREMENTS_OTHER(
        "listCaseRequirementsOther", new TypeReference<String>(){}),

    DRAFT_DECISION_AND_REASONS_DOCUMENTS(
        "draftDecisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    FINAL_DECISION_AND_REASONS_DOCUMENTS(
        "finalDecisionAndReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    REHEARD_DECISION_REASONS_DOCUMENTS(
        "reheardDecisionReasonsDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ANONYMITY_ORDER(
        "anonymityOrder", new TypeReference<YesOrNo>(){}),

    APPELLANT_REPRESENTATIVE(
        "appellantRepresentative", new TypeReference<String>(){}),

    RESPONDENT_REPRESENTATIVE(
        "respondentRepresentative", new TypeReference<String>(){}),

    APPELLANT_CASE_SUMMARY_DESCRIPTION(
        "appellantCaseSummaryDescription", new TypeReference<String>(){}),

    IMMIGRATION_HISTORY_AGREEMENT(
        "immigrationHistoryAgreement", new TypeReference<YesOrNo>(){}),

    AGREED_IMMIGRATION_HISTORY_DESCRIPTION(
        "agreedImmigrationHistoryDescription", new TypeReference<String>(){}),

    RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION(
        "respondentsImmigrationHistoryDescription", new TypeReference<String>(){}),

    IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION(
        "immigrationHistoryDisagreementDescription", new TypeReference<String>(){}),

    SCHEDULE_OF_ISSUES_AGREEMENT(
        "scheduleOfIssuesAgreement", new TypeReference<YesOrNo>(){}),

    APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION(
        "appellantsAgreedScheduleOfIssuesDescription", new TypeReference<String>(){}),

    APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION(
        "appellantsDisputedScheduleOfIssuesDescription", new TypeReference<String>(){}),

    SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION(
        "scheduleOfIssuesDisagreementDescription", new TypeReference<String>(){}),

    CASE_INTRODUCTION_DESCRIPTION(
        "caseIntroductionDescription", new TypeReference<String>(){}),

    FINAL_DECISION_AND_REASONS_PDF(
        "finalDecisionAndReasonsPdf", new TypeReference<Document>(){}),

    FINAL_DECISION_AND_REASONS_DOCUMENT(
        "finalDecisionAndReasonsDocument", new TypeReference<Document>(){}),

    IS_DECISION_ALLOWED(
        "isDecisionAllowed", new TypeReference<AppealDecision>(){}),

    DECISION_AND_REASONS_COVER_LETTER(
        "decisionAndReasonsCoverLetter", new TypeReference<Document>(){}),

    DECISION_AND_REASONS_AVAILABLE(
        "decisionAndReasonsAvailable", new TypeReference<YesOrNo>(){}),

    APPLICATION_OUT_OF_TIME_EXPLANATION(
        "applicationOutOfTimeExplanation", new TypeReference<String>(){}),

    APPLICATION_OUT_OF_TIME_DOCUMENT(
        "applicationOutOfTimeDocument", new TypeReference<Document>(){}),

    SUBMISSION_OUT_OF_TIME(
        "submissionOutOfTime", new TypeReference<YesOrNo>(){}),

    ARIA_LISTING_REFERENCE(
        "ariaListingReference", new TypeReference<String>(){}),

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

    TRIBUNAL_DOCUMENTS(
        "tribunalDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ADDITIONAL_EVIDENCE_DOCUMENTS(
        "additionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    HEARING_REQUIREMENTS(
        "hearingRequirements", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    IS_APPELLANT_ATTENDING_THE_HEARING(
        "isAppellantAttendingTheHearing", new TypeReference<YesOrNo>(){}),

    IS_APPELLANT_GIVING_ORAL_EVIDENCE(
        "isAppellantGivingOralEvidence", new TypeReference<YesOrNo>(){}),

    IS_WITNESSES_ATTENDING(
        "isWitnessesAttending", new TypeReference<YesOrNo>(){}),

    WITNESS_DETAILS(
        "witnessDetails", new TypeReference<List<IdValue<WitnessDetails>>>(){}),

    IS_INTERPRETER_SERVICES_NEEDED(
        "isInterpreterServicesNeeded", new TypeReference<YesOrNo>(){}),

    INTERPRETER_LANGUAGE(
        "interpreterLanguage", new TypeReference<List<IdValue<InterpreterLanguage>>>(){}),

    IS_HEARING_ROOM_NEEDED(
        "isHearingRoomNeeded", new TypeReference<YesOrNo>(){}),

    IS_HEARING_LOOP_NEEDED(
        "isHearingLoopNeeded", new TypeReference<YesOrNo>(){}),

    PHYSICAL_OR_MENTAL_HEALTH_ISSUES(
        "physicalOrMentalHealthIssues", new TypeReference<YesOrNo>(){}),

    PHYSICAL_OR_MENTAL_HEALTH_ISSUES_DESCRIPTION(
        "physicalOrMentalHealthIssuesDescription", new TypeReference<String>(){}),

    PAST_EXPERIENCES(
        "pastExperiences", new TypeReference<YesOrNo>(){}),

    PAST_EXPERIENCES_DESCRIPTION(
        "pastExperiencesDescription", new TypeReference<String>(){}),

    MULTIMEDIA_EVIDENCE(
        "multimediaEvidence", new TypeReference<YesOrNo>(){}),

    MULTIMEDIA_EVIDENCE_DESCRIPTION(
        "multimediaEvidenceDescription", new TypeReference<String>(){}),

    SINGLE_SEX_COURT(
        "singleSexCourt", new TypeReference<YesOrNo>(){}),

    SINGLE_SEX_COURT_TYPE(
        "singleSexCourtType", new TypeReference<MaleOrFemale>(){}),

    SINGLE_SEX_COURT_TYPE_DESCRIPTION(
        "singleSexCourtTypeDescription", new TypeReference<String>(){}),

    IN_CAMERA_COURT(
        "inCameraCourt", new TypeReference<YesOrNo>(){}),

    IN_CAMERA_COURT_DESCRIPTION(
        "inCameraCourtDescription", new TypeReference<String>(){}),

    ADDITIONAL_REQUESTS(
        "additionalRequests", new TypeReference<YesOrNo>(){}),

    ADDITIONAL_REQUESTS_DESCRIPTION(
        "additionalRequestsDescription", new TypeReference<String>(){}),

    DATES_TO_AVOID(
        "datesToAvoid", new TypeReference<List<IdValue<DatesToAvoid>>>(){}),

    DATES_TO_AVOID_YES_NO(
        "datesToAvoidYesNo", new TypeReference<YesOrNo>(){}),

    IS_OUT_OF_COUNTRY_ENABLED(
            "isOutOfCountryEnabled", new TypeReference<YesOrNo>(){}),
    REMOTE_VIDEO_CALL(
            "remoteVideoCall", new TypeReference<YesOrNo>(){}),

    REMOTE_VIDEO_CALL_DESCRIPTION(
            "remoteVideoCallDescription", new TypeReference<String>(){}),

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

    DATES_TO_AVOID_TRIBUNAL_RESPONSE(
        "datesToAvoidTribunalResponse", new TypeReference<String>(){}),

    SUBMIT_HEARING_REQUIREMENTS_AVAILABLE(
        "submitHearingRequirementsAvailable", new TypeReference<YesOrNo>(){}),

    DIRECTIONS(
        "directions", new TypeReference<List<IdValue<Direction>>>(){}),

    PAYMENT_STATUS(
        "paymentStatus", new TypeReference<PaymentStatus>(){}),

    PA_APPEAL_TYPE_PAYMENT_OPTION(
        "paAppealTypePaymentOption", new TypeReference<String>() {}),

    IS_REHEARD_APPEAL_ENABLED(
        "isReheardAppealEnabled", new TypeReference<YesOrNo>() {}),

    CASE_FLAG_SET_ASIDE_REHEARD_EXISTS(
        "caseFlagSetAsideReheardExists", new TypeReference<YesOrNo>() {}),

    DRAFT_REHEARD_DECISION_AND_REASONS(
        "draftReheardDecisionAndReasons", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    HMCTS(
        "hmcts", new TypeReference<String>(){}),

    CUSTOM_HEARING_DOCUMENTS(
        "customHearingDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_LEGAL_REP_DOCUMENTS(
        "customLegalRepDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_ADDITIONAL_EVIDENCE_DOCUMENTS(
        "customAdditionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_RESPONDENT_DOCUMENTS(
        "customRespondentDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    ADDENDUM_EVIDENCE_DOCUMENTS(
        "addendumEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CUSTOM_FTPA_APPELLANT_EVIDENCE_DOCS(
        "customFtpaAppellantEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_APP_ADDITIONAL_EVIDENCE_DOCS(
        "customAppAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_RESP_ADDITIONAL_EVIDENCE_DOCS(
        "customRespAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_FTPA_APPELLANT_DOCS(
        "customFtpaAppellantDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_FTPA_RESPONDENT_DOCS(
        "customFtpaRespondentDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_FINAL_DECISION_AND_REASONS_DOCS(
        "customFinalDecisionAndReasonsDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    CUSTOM_APP_ADDENDUM_EVIDENCE_DOCS(
        "customAppAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    APPELLANT_ADDENDUM_EVIDENCE_DOCS(
        "appellantAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CUSTOM_RESP_ADDENDUM_EVIDENCE_DOCS(
        "customRespAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    RESPONDENT_ADDENDUM_EVIDENCE_DOCS(
        "respondentAddendumEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CUSTOM_REHEARD_HEARING_DOCS(
        "customReheardHearingDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    APP_ADDITIONAL_EVIDENCE_DOCS(
        "appAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CUSTOM_TRIBUNAL_DOCUMENTS(
            "customTribunalDocuments", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),

    RESP_ADDITIONAL_EVIDENCE_DOCS(
        "respAdditionalEvidenceDocs", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    FTPA_APPELLANT_DOCUMENTS(
        "ftpaAppellantDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    FTPA_RESPONDENT_DOCUMENTS(
        "ftpaRespondentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE(
            "remoteVideoCallTribunalResponse", new TypeReference<String>(){}),

    IS_EVIDENCE_FROM_OUTSIDE_UK_OOC(
        "isEvidenceFromOutsideUkOoc", new TypeReference<YesOrNo>() {}),

    IS_EVIDENCE_FROM_OUTSIDE_UK_IN_COUNTRY(
        "isEvidenceFromOutsideUkInCountry", new TypeReference<YesOrNo>() {}),

    UPPER_TRIBUNAL_DOCUMENTS(
        "upperTribunalDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    CLARIFYING_QUESTIONS_ANSWERS("clarifyingQuestionsAnswers",
        new TypeReference<List<IdValue<ClarifyingQuestionAnswer>>>() {}),

    APPELLANT_IN_DETENTION(
            "appellantInDetention", new TypeReference<YesOrNo>() {}
    ),
    DETENTION_FACILITY(
            "detentionFacility", new TypeReference<String>(){}),

    IRC_NAME(
            "ircName", new TypeReference<String>(){}),

    PRISON_NAME(
            "prisonName", new TypeReference<String>(){}),

    HAS_PENDING_BAIL_APPLICATIONS(
            "hasPendingBailApplications", new TypeReference<BailApplicationStatus>() {}),

    BAIL_APPLICATION_NUMBER(
            "bailApplicationNumber", new TypeReference<String>(){}),

    IS_ACCELERATED_DETAINED_APPEAL(
            "isAcceleratedDetainedAppeal", new TypeReference<YesOrNo>(){}),

    SUITABILITY_REVIEW_DECISION(
            "suitabilityReviewDecision", new TypeReference<AdaSuitabilityReviewDecision>(){}),

    SUITABILITY_REVIEW_REASON(
            "suitabilityReviewReason", new TypeReference<String>(){}),

    SUITABILITY_REVIEW_JUDGE(
            "suitabilityReviewJudge", new TypeReference<String>(){}),

    IS_ADMIN(
            "isAdmin", new TypeReference<YesOrNo>() {}),

    HAS_OTHER_APPEALS(
            "hasOtherAppeals", new TypeReference<HasOtherAppeals>() {}),

    // Case data section to hold generated documents,so they can be attached to email notifications
    NOTIFICATION_ATTACHMENT_DOCUMENTS(
            "notificationAttachmentDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    RECORDED_OUT_OF_TIME_DECISION(
            "recordedOutOfTimeDecision", new TypeReference<YesOrNo>(){}),

    APPEAL_REVIEW_OUTCOME(
        "appealReviewOutcome", new TypeReference<AppealReviewOutcome>(){}),

    REMISSION_TYPE(
        "remissionType", new TypeReference<RemissionType>(){}),

    REMISSION_REJECTED_DATE_PLUS_14DAYS(
            "remissionRejectedDatePlus14days", new TypeReference<String>(){}),

    CCD_REFERENCE_NUMBER_FOR_DISPLAY(
            "ccdReferenceNumberForDisplay", new TypeReference<String>(){}),

    FEE_AMOUNT_GBP(
            "feeAmountGbp", new TypeReference<String>(){}),

    AMOUNT_REMITTED(
            "amountRemitted", new TypeReference<String>(){}),

    PAID_AMOUNT(
            "paidAmount", new TypeReference<String>(){}),

    REMISSION_DECISION(
            "remissionDecision", new TypeReference<RemissionDecision>(){}),

    REASON_APPEAL_MARKED_AS_ADA(
            "reasonAppealMarkedAsAda", new TypeReference<String>(){}),

    MAKE_AN_APPLICATIONS(
            "makeAnApplications", new TypeReference<List<IdValue<MakeAnApplication>>>(){}),

    DECIDE_AN_APPLICATION_ID(
            "decideAnApplicationId", new TypeReference<String>(){}),

    TRANSFER_OUT_OF_ADA_REASON(
            "transferOutOfAdaReason", new TypeReference<String>(){}),

    TRANSFER_OUT_OF_ADA_MORE_DETAILS(
            "transferOutOfAdaMoreDetails", new TypeReference<String>(){}),

    FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE(
            "ftpaAppellantRjDecisionOutcomeType", new TypeReference<FtpaDecisionOutcomeType>(){}),

    FTPA_APPLICANT_TYPE(
            "ftpaApplicantType", new TypeReference<String>(){}),

    SEND_DIRECTION_PARTIES(
        "sendDirectionParties", new TypeReference<Parties>(){}),

    FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE(
            "ftpaRespondentRjDecisionOutcomeType", new TypeReference<FtpaDecisionOutcomeType>(){}),

    CASE_LINKS(
            "caseLinks", new TypeReference<List<IdValue<CaseLink>>>(){}),

    DIRECTION_EDIT_DATE_DUE(
            "directionEditDateDue", new TypeReference<String>(){}),

    DIRECTION_EDIT_EXPLANATION(
            "directionEditExplanation", new TypeReference<String>(){}),

    DIRECTION_EDIT_PARTIES(
            "directionEditParties", new TypeReference<Parties>(){});


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
