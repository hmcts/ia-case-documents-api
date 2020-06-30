package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;

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
