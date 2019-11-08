package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

public enum AsylumCaseDefinition {

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

    LEGAL_REPRESENTATIVE_EMAIL_ADDRESS(
            "legalRepresentativeEmailAddress", new TypeReference<String>(){}),

    LEGAL_REPRESENTATIVE_NAME(
            "legalRepresentativeName", new TypeReference<String>(){}),

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
