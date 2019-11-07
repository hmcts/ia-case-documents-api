package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

public enum AsylumCaseDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
            "homeOfficeReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_DECISION_DATE(
            "homeOfficeDecisionDate", new TypeReference<String>(){}),

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
            "appealType", new TypeReference<String>(){}),

    APPEAL_SUBMISSION_DATE(
            "appealSubmissionDate", new TypeReference<String>(){}),

    NEW_MATTERS(
            "newMatters", new TypeReference<String>(){}),

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

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
            "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    LEGAL_REP_NAME(
            "legalRepName", new TypeReference<String>(){}),

    LEGAL_REP_COMPANY(
            "legalRepCompany", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_DOCUMENTS(
            "legalRepresentativeDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

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

    TRIBUNAL_DOCUMENTS(
        "tribunalDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

    ADDITIONAL_EVIDENCE_DOCUMENTS(
        "additionalEvidenceDocuments", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),

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
