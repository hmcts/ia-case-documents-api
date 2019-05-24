package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

public enum AsylumCaseDefinition {

    HOME_OFFICE_REFERENCE_NUMBER(
            "homeOfficeReferenceNumber", new TypeReference<String>(){}),

    HOME_OFFICE_DECISION_DATE(
            "homeOfficeDecisionDate", new TypeReference<String>(){}),

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
            "listCaseRequirementsOther", new TypeReference<String>(){});

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
