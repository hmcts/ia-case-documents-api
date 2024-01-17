package uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;

public enum BailCaseFieldDefinition {
    SENT_BY_CHECKLIST(
        "sentByChecklist", new TypeReference<String>() {}),
    IS_ADMIN(
        "isAdmin", new TypeReference<YesOrNo>() {}),
    IS_LEGAL_REP(
        "isLegalRep", new TypeReference<YesOrNo>() {}),
    IS_HOME_OFFICE(
        "isHomeOffice", new TypeReference<YesOrNo>() {}),
    APPLICANT_GIVEN_NAMES(
        "applicantGivenNames", new TypeReference<String>() {}),
    APPLICANT_FAMILY_NAME(
        "applicantFamilyName", new TypeReference<String>() {}),
    APPLICANT_DATE_OF_BIRTH(
        "applicantDateOfBirth", new TypeReference<String>() {}),
    APPLICANT_GENDER(
        "applicantGender", new TypeReference<String>() {}),
    APPLICANT_GENDER_ENTER_DETAILS(
        "applicantGenderEnterDetails", new TypeReference<String>() {}),
    APPLICANT_NATIONALITY(
        "applicantNationality", new TypeReference<String>() {}),
    APPLICANT_NATIONALITIES(
        "applicantNationalities", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    HOME_OFFICE_REFERENCE_NUMBER(
        "homeOfficeReferenceNumber", new TypeReference<String>(){}),
    APPLICANT_DETAINED_LOC(
        "applicantDetainedLoc", new TypeReference<String>(){}),
    APPLICANT_PRISON_DETAILS(
        "applicantPrisonDetails", new TypeReference<String>(){}),
    IRC_NAME(
        "ircName", new TypeReference<String>(){}),
    PRISON_NAME(
        "prisonName", new TypeReference<String>(){}),
    APPLICANT_ARRIVAL_IN_UK(
        "applicantArrivalInUKDate", new TypeReference<String>(){}),
    APPLICANT_HAS_MOBILE(
        "applicantHasMobile", new TypeReference<YesOrNo>(){}),
    APPLICANT_MOBILE_NUMBER_1(
        "applicantMobileNumber1", new TypeReference<String>(){}),
    HAS_APPEAL_HEARING_PENDING(
        "hasAppealHearingPending", new TypeReference<String>(){}),
    APPEAL_REFERENCE_NUMBER(
        "appealReferenceNumber", new TypeReference<String>(){}),
    HAS_PREVIOUS_BAIL_APPLICATION(
        "hasPreviousBailApplication", new TypeReference<String>(){}),
    PREVIOUS_BAIL_APPLICATION_NUMBER(
        "previousBailApplicationNumber", new TypeReference<String>(){}),
    APPLICANT_BEEN_REFUSED_BAIL(
        "applicantBeenRefusedBail", new TypeReference<YesOrNo>(){}),
    BAIL_HEARING_DATE(
        "bailHearingDate", new TypeReference<String>(){}),
    APPLICANT_HAS_ADDRESS(
        "applicantHasAddress", new TypeReference<YesOrNo>(){}),
    APPLICANT_ADDRESS(
        "applicantAddress", new TypeReference<AddressUk>(){}),
    AGREES_TO_BOUND_BY_FINANCIAL_COND(
        "agreesToBoundByFinancialCond", new TypeReference<YesOrNo>(){}),
    FINANCIAL_COND_AMOUNT_1(
        "financialCondAmount1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER(
        "hasFinancialCondSupporter", new TypeReference<YesOrNo>(){}),
    SUPPORTER_GIVEN_NAMES(
        "supporterGivenNames", new TypeReference<String>(){}),
    SUPPORTER_FAMILY_NAMES(
        "supporterFamilyNames", new TypeReference<String>(){}),
    SUPPORTER_ADDRESS_DETAILS(
        "supporterAddressDetails", new TypeReference<AddressUk>(){}),
    SUPPORTER_CONTACT_DETAILS(
        "supporterContactDetails", new TypeReference<String>(){}),
    SUPPORTER_TELEPHONE_NUMBER_1(
        "supporterTelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_MOBILE_NUMBER_1(
        "supporterMobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_EMAIL_ADDRESS_1(
        "supporterEmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_DOB(
        "supporterDOB", new TypeReference<String>(){}),
    SUPPORTER_RELATION(
        "supporterRelation", new TypeReference<String>(){}),
    SUPPORTER_OCCUPATION(
        "supporterOccupation", new TypeReference<String>(){}),
    SUPPORTER_IMMIGRATION(
        "supporterImmigration", new TypeReference<String>(){}),
    SUPPORTER_NATIONALITY(
        "supporterNationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_HAS_PASSPORT(
        "supporterHasPassport", new TypeReference<String>(){}),
    SUPPORTER_PASSPORT(
        "supporterPassport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES_1(
        "financialAmountSupporterUndertakes1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_2(
        "hasFinancialCondSupporter2", new TypeReference<YesOrNo>(){}),
    SUPPORTER_2_GIVEN_NAMES(
        "supporter2GivenNames", new TypeReference<String>(){}),
    SUPPORTER_2_FAMILY_NAMES(
        "supporter2FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_2_ADDRESS_DETAILS(
        "supporter2AddressDetails", new TypeReference<AddressUk>(){}),
    SUPPORTER_2_CONTACT_DETAILS(
        "supporter2ContactDetails", new TypeReference<String>(){}),
    SUPPORTER_2_TELEPHONE_NUMBER_1(
        "supporter2TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_2_MOBILE_NUMBER_1(
        "supporter2MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_2_EMAIL_ADDRESS_1(
        "supporter2EmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_2_DOB(
        "supporter2DOB", new TypeReference<String>(){}),
    SUPPORTER_2_RELATION(
        "supporter2Relation", new TypeReference<String>(){}),
    SUPPORTER_2_OCCUPATION(
        "supporter2Occupation", new TypeReference<String>(){}),
    SUPPORTER_2_IMMIGRATION(
        "supporter2Immigration", new TypeReference<String>(){}),
    SUPPORTER_2_NATIONALITY(
        "supporter2Nationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_2_HAS_PASSPORT(
        "supporter2HasPassport", new TypeReference<String>(){}),
    SUPPORTER_2_PASSPORT(
        "supporter2Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES_1(
        "financialAmountSupporter2Undertakes1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_3(
        "hasFinancialCondSupporter3", new TypeReference<YesOrNo>(){}),
    SUPPORTER_3_GIVEN_NAMES(
        "supporter3GivenNames", new TypeReference<String>(){}),
    SUPPORTER_3_FAMILY_NAMES(
        "supporter3FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_3_ADDRESS_DETAILS(
        "supporter3AddressDetails", new TypeReference<AddressUk>(){}),
    SUPPORTER_3_CONTACT_DETAILS(
        "supporter3ContactDetails", new TypeReference<String>(){}),
    SUPPORTER_3_TELEPHONE_NUMBER_1(
        "supporter3TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_3_MOBILE_NUMBER_1(
        "supporter3MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_3_EMAIL_ADDRESS_1(
        "supporter3EmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_3_DOB(
        "supporter3DOB", new TypeReference<String>(){}),
    SUPPORTER_3_RELATION(
        "supporter3Relation", new TypeReference<String>(){}),
    SUPPORTER_3_OCCUPATION(
        "supporter3Occupation", new TypeReference<String>(){}),
    SUPPORTER_3_IMMIGRATION(
        "supporter3Immigration", new TypeReference<String>(){}),
    SUPPORTER_3_NATIONALITY(
        "supporter3Nationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_3_HAS_PASSPORT(
        "supporter3HasPassport", new TypeReference<String>(){}),
    SUPPORTER_3_PASSPORT(
        "supporter3Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES_1(
        "financialAmountSupporter3Undertakes1", new TypeReference<String>(){}),
    HAS_FINANCIAL_COND_SUPPORTER_4(
        "hasFinancialCondSupporter4", new TypeReference<YesOrNo>(){}),
    SUPPORTER_4_GIVEN_NAMES(
        "supporter4GivenNames", new TypeReference<String>(){}),
    SUPPORTER_4_FAMILY_NAMES(
        "supporter4FamilyNames", new TypeReference<String>(){}),
    SUPPORTER_4_ADDRESS_DETAILS(
        "supporter4AddressDetails", new TypeReference<AddressUk>(){}),
    SUPPORTER_4_CONTACT_DETAILS(
        "supporter4ContactDetails", new TypeReference<String>(){}),
    SUPPORTER_4_TELEPHONE_NUMBER_1(
        "supporter4TelephoneNumber1", new TypeReference<String>(){}),
    SUPPORTER_4_MOBILE_NUMBER_1(
        "supporter4MobileNumber1", new TypeReference<String>(){}),
    SUPPORTER_4_EMAIL_ADDRESS_1(
        "supporter4EmailAddress1", new TypeReference<String>(){}),
    SUPPORTER_4_DOB(
        "supporter4DOB", new TypeReference<String>(){}),
    SUPPORTER_4_RELATION(
        "supporter4Relation", new TypeReference<String>(){}),
    SUPPORTER_4_OCCUPATION(
        "supporter4Occupation", new TypeReference<String>(){}),
    SUPPORTER_4_IMMIGRATION(
        "supporter4Immigration", new TypeReference<String>(){}),
    SUPPORTER_4_NATIONALITY(
        "supporter4Nationality", new TypeReference<List<IdValue<NationalityFieldValue>>>(){}),
    SUPPORTER_4_HAS_PASSPORT(
        "supporter4HasPassport", new TypeReference<String>(){}),
    SUPPORTER_4_PASSPORT(
        "supporter4Passport", new TypeReference<String>(){}),
    FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES_1(
        "financialAmountSupporter4Undertakes1", new TypeReference<String>(){}),
    INTERPRETER_YES_NO(
        "interpreterYesNo", new TypeReference<YesOrNo>(){}),
    INTERPRETER_LANGUAGES(
        "interpreterLanguages", new TypeReference<List<IdValue<InterpreterLanguage>>>(){}),
    APPLICANT_DISABILITY1(
        "applicantDisability1", new TypeReference<YesOrNo>(){}),
    APPLICANT_DISABILITY_DETAILS(
        "applicantDisabilityDetails", new TypeReference<String>(){}),
    VIDEO_HEARING1(
        "videoHearing1", new TypeReference<YesOrNo>(){}),
    VIDEO_HEARING_DETAILS(
        "videoHearingDetails", new TypeReference<String>(){}),
    LEGAL_REP_COMPANY(
        "legalRepCompany", new TypeReference<String>(){}),
    LEGAL_REP_EMAIL(
        "legalRepEmail", new TypeReference<String>(){}),
    LEGAL_REP_NAME(
        "legalRepName", new TypeReference<String>(){}),
    LEGAL_REP_PHONE(
        "legalRepPhone", new TypeReference<String>(){}),
    LEGAL_REP_REFERENCE(
        "legalRepReference", new TypeReference<String>(){}),
    GROUNDS_FOR_BAIL_REASONS(
        "groundsForBailReasons", new TypeReference<String>(){}),
    GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION(
        "groundsForBailProvideEvidenceOption", new TypeReference<YesOrNo>(){}),
    UPLOAD_THE_BAIL_EVIDENCE_DOCS(
        "uploadTheBailEvidenceDocs", new TypeReference<List<IdValue<DocumentWithDescription>>>(){}),
    APPLICANT_DOCUMENTS_WITH_METADATA(
        "applicantDocumentsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    TRANSFER_BAIL_MANAGEMENT_YES_OR_NO(
        "transferBailManagementYesOrNo", new TypeReference<YesOrNo>(){}),
    NO_TRANSFER_BAIL_MANAGEMENT_REASONS(
        "noTransferBailManagementReasons", new TypeReference<String>(){}),
    APPLICATION_SUBMITTED_BY(
        "applicationSubmittedBy", new TypeReference<String>(){}),
    BAIL_REFERENCE_NUMBER(
        "bailReferenceNumber", new TypeReference<String>(){}),
    APPLICANT_FULL_NAME(
        "applicantFullName", new TypeReference<String>(){}),
    IS_LEGALLY_REPRESENTED_FOR_FLAG(
        "isLegallyRepresentedForFlag", new TypeReference<YesOrNo>() {}),
    HAS_LEGAL_REP(
        "hasLegalRep", new TypeReference<YesOrNo>(){}),
    DECISION_UNSIGNED_DOCUMENT(
        "decisionUnsignedDocument", new TypeReference<Document>(){}),
    TRIBUNAL_DOCUMENTS_WITH_METADATA(
        "tribunalDocumentsWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    REASONS_JUDGE_IS_MINDED_DETAILS(
        "reasonsJudgeIsMindedDetails", new TypeReference<String>(){}),
    JUDGE_DETAILS_NAME(
        "judgeDetailsName", new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL_OTHER(
        "conditionsForBailOther", new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL_APPEARANCE(
        "conditionsForBailAppearance", new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL_ACTIVITIES(
        "conditionsForBailActivities", new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL_RESIDENCE(
        "conditionsForBailResidence", new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL_ELECTRONIC_MONITORING(
        "conditionsForBailElectronicMonitoring",  new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL_REPORTING(
        "conditionsForBailReporting", new TypeReference<String>(){}),
    SECRETARY_OF_STATE_REFUSAL_REASONS(
        "secretaryOfStateRefusalReasons", new TypeReference<String>(){}),
    BAIL_TRANSFER_YES_OR_NO(
        "bailTransferYesOrNo", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER1(
        "judgeHasAgreedToSupporter1", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER2(
        "judgeHasAgreedToSupporter2", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER3(
        "judgeHasAgreedToSupporter3", new TypeReference<YesOrNo>(){}),
    JUDGE_HAS_AGREED_TO_SUPPORTER4(
        "judgeHasAgreedToSupporter4", new TypeReference<YesOrNo>(){}),
    RECORD_FINANCIAL_CONDITION_YES_OR_NO(
        "recordFinancialConditionYesOrNo", new TypeReference<YesOrNo>(){}),
    RECORD_DECISION_TYPE(
        "recordDecisionType", new TypeReference<String>(){}),
    RECORD_THE_DECISION_LIST(
        "recordTheDecisionList", new TypeReference<String>(){}),
    BAIL_TRANSFER_DIRECTIONS(
        "bailTransferDirections", new TypeReference<String>(){}),
    REASON_FOR_REFUSAL_DETAILS(
        "reasonForRefusalDetails", new TypeReference<String>(){}),
    TRIBUNAL_REFUSAL_REASON(
        "tribunalRefusalReason", new TypeReference<String>(){}),
    CONDITIONS_FOR_BAIL(
            "conditionsForBail", new TypeReference<List<String>>(){}),
    END_APPLICATION_DATE(
        "endApplicationDate", new TypeReference<String>(){}),
    END_APPLICATION_OUTCOME(
        "endApplicationOutcome", new TypeReference<String>(){}),
    END_APPLICATION_REASONS(
        "endApplicationReasons", new TypeReference<String>(){}),
    PRIOR_APPLICATIONS(
            "priorApplications1", new TypeReference<List<IdValue<PriorApplication>>>(){}),
    UNSIGNED_DECISION_DOCUMENTS_WITH_METADATA(
        "unsgnDecisionDocumentWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    UPLOAD_SIGNED_DECISION_NOTICE_DOCUMENT(
        "uploadSignedDecisionNoticeDocument", new TypeReference<Document>(){}),
    SIGNED_DECISION_DOCUMENT_WITH_METADATA(
        "signDecisionDocumentWithMetadata", new TypeReference<List<IdValue<DocumentWithMetadata>>>(){}),
    ;

    private final String value;
    private final TypeReference typeReference;

    BailCaseFieldDefinition(String value, TypeReference typeReference) {
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
