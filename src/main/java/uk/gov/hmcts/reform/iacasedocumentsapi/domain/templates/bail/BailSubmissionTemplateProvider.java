package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.bail.BailInterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.InterpreterLanguage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.BailCaseUtils;

@Component
public class BailSubmissionTemplateProvider {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final String SENT_BY_LR = "Legal Representative";
    private static final String SENT_BY_HO = "Home Office";
    private static final String NATIONALITY = "nationality";
    private static final String SPOKEN_INTERPRETER_LABEL = "Spoken language interpreter";
    private static final String SIGN_INTERPRETER_LABEL = "Sign language interpreter";
    private static final String DONT_KNOW_SELECTED_VALUE = "Don't Know";
    private static final String IS_MANUAL_ENTRY = "Yes";
    private static final String IS_NOT_MANUAL_ENTRY = "No";

    public BailSubmissionTemplateProvider() {

    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<BailCase> caseDetails
    ) {
        final BailCase bailCase = caseDetails.getCaseData();

        final Map<String, Object> fieldValues = new HashMap<>();
        boolean isLegalRep = false;
        final boolean hasLegalRep = bailCase.read(HAS_LEGAL_REP, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);

        fieldValues.put("hmcts", "[userImage:hmcts.png]");
        fieldValues.put("CREATED_DATE", caseDetails.getCreatedDate().format(DOCUMENT_DATE_FORMAT));
        fieldValues.put("applicantGivenNames", bailCase.read(APPLICANT_GIVEN_NAMES, String.class));
        fieldValues.put("applicantFamilyName", bailCase.read(APPLICANT_FAMILY_NAME, String.class));

        if (bailCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            isLegalRep = (bailCase.read(SENT_BY_CHECKLIST, String.class).orElse("").equalsIgnoreCase("legal representative"));
            fieldValues.put("applicationSubmittedBy", bailCase.read(SENT_BY_CHECKLIST, String.class));
        } else if (bailCase.read(IS_LEGAL_REP, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            isLegalRep = true;
            fieldValues.put("applicationSubmittedBy", SENT_BY_LR);
        } else if (bailCase.read(IS_HOME_OFFICE, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("applicationSubmittedBy", SENT_BY_HO);
        }
        final boolean isLegallyRepresentedCase = isLegalRep || hasLegalRep;

        fieldValues.put("applicantDateOfBirth", formatDateForRendering(bailCase.read(APPLICANT_DATE_OF_BIRTH, String.class).orElse("")));
        String gender = bailCase.read(APPLICANT_GENDER, String.class).orElse("");
        fieldValues.put("applicantGender", gender);
        if (gender.equals("Other")) {
            fieldValues.put("applicantOtherGenderDetails", bailCase.read(APPLICANT_GENDER_ENTER_DETAILS, String.class).orElse(""));
        }

        Optional<List<IdValue<NationalityFieldValue>>> applicantNationalities = bailCase
            .read(APPLICANT_NATIONALITIES);
        fieldValues.put(
            "applicantNationalities",
            applicantNationalities
                .orElse(Collections.emptyList())
                .stream()
                .map(idValue -> idValue.getValue().getCode())
                .map(nationality -> ImmutableMap.of(NATIONALITY, nationality))
                .collect(Collectors.toList())
        );

        fieldValues.put("applicantHasMobile", bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(APPLICANT_HAS_MOBILE, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("applicantMobileNumber1", bailCase.read(APPLICANT_MOBILE_NUMBER_1, String.class).orElse(""));
        }

        fieldValues.put("homeOfficeReferenceNumber", bailCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("applicantDetainedLoc", bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse(""));
        if (bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse("").equals("prison")) {
            fieldValues.put("applicantDetainedLoc", "Prison");
            fieldValues.put("applicantPrisonDetails", bailCase.read(APPLICANT_PRISON_DETAILS, String.class).orElse(""));
            fieldValues.put("prisonName", bailCase.read(PRISON_NAME, String.class).orElse(""));
        }

        if (bailCase.read(APPLICANT_DETAINED_LOC, String.class).orElse("").equals("immigrationRemovalCentre")) {
            fieldValues.put("applicantDetainedLoc", "Immigration Removal Centre");
            fieldValues.put("ircName", bailCase.read(IRC_NAME, String.class).orElse(""));
        }

        fieldValues.put("applicantArrivalInUKDate", formatDateForRendering(bailCase.read(APPLICANT_ARRIVAL_IN_UK, String.class).orElse("")));

        fieldValues.put("hasAppealHearingPending", bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class).orElse(""));
        switch (bailCase.read(HAS_APPEAL_HEARING_PENDING, String.class).orElse("")) {
            case "YesWithoutAppealNumber":
                fieldValues.put("hasAppealHearingPending", "Yes Without Appeal Number");
                break;
            case "DontKnow":
                fieldValues.put("hasAppealHearingPending", DONT_KNOW_SELECTED_VALUE);
                break;
            case "Yes" :
                fieldValues.put("appealReferenceNumber", bailCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
                break;
            default:
                break;
        }

        if (BailCaseUtils.isImaEnabled(bailCase)) {
            fieldValues.put("hasAppealHearingPendingUT", bailCase.read(HAS_APPEAL_HEARING_PENDING_UT, String.class).orElse(""));
            switch (bailCase.read(HAS_APPEAL_HEARING_PENDING_UT, String.class).orElse("")) {
                case "YesWithoutAppealNumber":
                    fieldValues.put("hasAppealHearingPendingUT", "Yes Without Appeal Number");
                    break;
                case "DontKnow":
                    fieldValues.put("hasAppealHearingPendingUT", DONT_KNOW_SELECTED_VALUE);
                    break;
                case "Yes":
                    fieldValues.put("appealReferenceNumberUT", bailCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
                    break;
                default:
                    break;
            }
        }

        fieldValues.put("applicantHasAddress", bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(APPLICANT_HAS_ADDRESS, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            Optional<AddressUk> optionalApplicantAddress = bailCase.read(APPLICANT_ADDRESS, AddressUk.class);
            if (optionalApplicantAddress.isPresent()) {

                AddressUk applicantAddress = optionalApplicantAddress.get();

                fieldValues.put(
                    "applicantAddress",
                    ImmutableMap
                        .builder()
                        .put("applicantAddressLine1", applicantAddress.getAddressLine1().orElse(""))
                        .put("applicantAddressLine2", applicantAddress.getAddressLine2().orElse(""))
                        .put("applicantAddressLine3", applicantAddress.getAddressLine3().orElse(""))
                        .put("applicantAddressPostTown", applicantAddress.getPostTown().orElse(""))
                        .put("applicantAddressCounty", applicantAddress.getCounty().orElse(""))
                        .put("applicantAddressPostCode", applicantAddress.getPostCode().orElse(""))
                        .put("applicantAddressCountry", applicantAddress.getCountry().orElse(""))
                        .build()
                );
            }
        }

        fieldValues.put("hasPreviousBailApplication", bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class).orElse(""));
        switch (bailCase.read(HAS_PREVIOUS_BAIL_APPLICATION, String.class).orElse("")) {
            case "Yes":
                fieldValues.put("previousBailApplicationNumber", bailCase.read(PREVIOUS_BAIL_APPLICATION_NUMBER, String.class).orElse(""));
                break;
            case "YesWithoutApplicationNumber":
                fieldValues.put("hasPreviousBailApplication", "Yes Without Application Number");
                break;
            case "DontKnow":
                fieldValues.put("hasPreviousBailApplication", DONT_KNOW_SELECTED_VALUE);
                break;
            default:
                break;
        }

        fieldValues.put("applicantBeenRefusedBail", bailCase.read(APPLICANT_BEEN_REFUSED_BAIL, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(APPLICANT_BEEN_REFUSED_BAIL, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("bailHearingDate", formatDateForRendering(bailCase.read(BAIL_HEARING_DATE, String.class).orElse("")));
        }

        fieldValues.put("agreesToBoundByFinancialCond", bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(AGREES_TO_BOUND_BY_FINANCIAL_COND, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("financialCondAmount1", bailCase.read(FINANCIAL_COND_AMOUNT_1, String.class).orElse(""));
        }

        setSupporter1Details(bailCase, fieldValues);
        setSupporter2Details(bailCase, fieldValues);
        setSupporter3Details(bailCase, fieldValues);
        setSupporter4Details(bailCase, fieldValues);

        fieldValues.put("hasProbationOffenderManager", bailCase.read(HAS_PROBATION_OFFENDER_MANAGER, String.class).orElse(""));
        if (bailCase.read(HAS_PROBATION_OFFENDER_MANAGER, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.NO) {
            fieldValues.put("probationOffenderManagerGivenName", bailCase.read(PROBATION_OFFENDER_MANAGER_GIVEN_NAME, String.class).orElse("test"));
            fieldValues.put("probationOffenderManagerFamilyName", bailCase.read(PROBATION_OFFENDER_MANAGER_FAMILY_NAME, String.class).orElse(""));
            fieldValues.put("probationOffenderManagerTelephoneNumber", bailCase.read(PROBATION_OFFENDER_MANAGER_TELEPHONE_NUMBER, String.class).orElse(""));
            fieldValues.put("probationOffenderManagerMobileNumber", bailCase.read(PROBATION_OFFENDER_MANAGER_MOBILE_NUMBER, String.class).orElse(""));
            fieldValues.put("probationOffenderManagerEmailAddress", bailCase.read(PROBATION_OFFENDER_MANAGER_EMAIL_ADDRESS, String.class).orElse(""));
        }

        fieldValues.put("groundsForBailReasons", bailCase.read(GROUNDS_FOR_BAIL_REASONS, String.class).orElse(""));
        fieldValues.put("transferBailManagementYesOrNo", bailCase.read(TRANSFER_BAIL_MANAGEMENT_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(TRANSFER_BAIL_MANAGEMENT_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.NO) {
            fieldValues.put("noTransferBailManagementReasons", bailCase.read(NO_TRANSFER_BAIL_MANAGEMENT_REASONS, String.class).orElse(""));
        }
        fieldValues.put("groundsForBailProvideEvidenceOption", bailCase.read(GROUNDS_FOR_BAIL_PROVIDE_EVIDENCE_OPTION, YesOrNo.class).orElse(YesOrNo.NO));

        updateHearingDetails(bailCase, fieldValues);

        fieldValues.put("isLegallyRepresentedForFlag", isLegallyRepresentedCase ? YesOrNo.YES : YesOrNo.NO);
        if (isLegallyRepresentedCase) {
            fieldValues.put("legalRepCompany", bailCase.read(LEGAL_REP_COMPANY, String.class).orElse(""));
            fieldValues.put("legalRepName", formatLegalRepName(
                bailCase.read(LEGAL_REP_NAME, String.class).orElse(""),
                bailCase.read(LEGAL_REP_FAMILY_NAME, String.class).orElse("")
            ));
            fieldValues.put("legalRepEmail", bailCase.read(LEGAL_REP_EMAIL, String.class).orElse(""));
            fieldValues.put("legalRepPhone", bailCase.read(LEGAL_REP_PHONE, String.class).orElse(""));
            fieldValues.put("legalRepReference", bailCase.read(LEGAL_REP_REFERENCE, String.class).orElse(""));
        }

        if (bailCase.read(PRIOR_APPLICATIONS, PriorApplication.class).orElse(null) == null) {
            fieldValues.put("showPreviousApplicationSection", YesOrNo.YES);
        }

        setApplicantInterpreterLanguageDetails(bailCase, fieldValues);

        fieldValues.put("fcsInterpreterYesNo", bailCase.read(FCS_INTERPRETER_YES_NO, YesOrNo.class).orElse(YesOrNo.NO));

        if (bailCase.read(FCS_INTERPRETER_YES_NO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            setFcs1InterpreterLanguageDetails(bailCase, fieldValues);
            setFcs2InterpreterLanguageDetails(bailCase, fieldValues);
            setFcs3InterpreterLanguageDetails(bailCase, fieldValues);
            setFcs4InterpreterLanguageDetails(bailCase, fieldValues);
        }

        return fieldValues;
    }

    private void setSupporter1Details(BailCase bailCase, Map<String, Object> fieldValues) {
        setSupporterDetails(bailCase, fieldValues, HAS_FINANCIAL_COND_SUPPORTER, SUPPORTER_GIVEN_NAMES,
            SUPPORTER_FAMILY_NAMES, SUPPORTER_ADDRESS_DETAILS, "supporterAddressLine1",
            "supporterAddressLine2", "supporterAddressLine3",
            "supporterAddressPostTown", "supporterAddressCounty",
            "supporterAddressPostCode", "supporterAddressCountry",
            SUPPORTER_TELEPHONE_NUMBER_1, SUPPORTER_MOBILE_NUMBER_1, SUPPORTER_EMAIL_ADDRESS_1,
            SUPPORTER_DOB, SUPPORTER_RELATION, SUPPORTER_OCCUPATION, SUPPORTER_IMMIGRATION,
            SUPPORTER_NATIONALITY, "supporterNationalities", SUPPORTER_HAS_PASSPORT,
            SUPPORTER_PASSPORT, FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES_1);

    }

    private void setSupporter2Details(BailCase bailCase, Map<String, Object> fieldValues) {
        setSupporterDetails(bailCase, fieldValues, HAS_FINANCIAL_COND_SUPPORTER_2, SUPPORTER_2_GIVEN_NAMES,
            SUPPORTER_2_FAMILY_NAMES, SUPPORTER_2_ADDRESS_DETAILS, "supporter2AddressLine1",
            "supporter2AddressLine2", "supporter2AddressLine3",
            "supporter2AddressPostTown", "supporter2AddressCounty",
            "supporter2AddressPostCode", "supporter2AddressCountry",
            SUPPORTER_2_TELEPHONE_NUMBER_1, SUPPORTER_2_MOBILE_NUMBER_1, SUPPORTER_2_EMAIL_ADDRESS_1,
            SUPPORTER_2_DOB, SUPPORTER_2_RELATION, SUPPORTER_2_OCCUPATION, SUPPORTER_2_IMMIGRATION,
            SUPPORTER_2_NATIONALITY, "supporter2Nationalities", SUPPORTER_2_HAS_PASSPORT,
            SUPPORTER_2_PASSPORT, FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES_1);

    }

    private void setSupporter3Details(BailCase bailCase, Map<String, Object> fieldValues) {
        setSupporterDetails(bailCase, fieldValues, HAS_FINANCIAL_COND_SUPPORTER_3, SUPPORTER_3_GIVEN_NAMES,
            SUPPORTER_3_FAMILY_NAMES, SUPPORTER_3_ADDRESS_DETAILS, "supporter3AddressLine1",
            "supporter3AddressLine2", "supporter3AddressLine3",
            "supporter3AddressPostTown", "supporter3AddressCounty",
            "supporter3AddressPostCode", "supporter3AddressCountry",
            SUPPORTER_3_TELEPHONE_NUMBER_1, SUPPORTER_3_MOBILE_NUMBER_1, SUPPORTER_3_EMAIL_ADDRESS_1,
            SUPPORTER_3_DOB, SUPPORTER_3_RELATION, SUPPORTER_3_OCCUPATION, SUPPORTER_3_IMMIGRATION,
            SUPPORTER_3_NATIONALITY, "supporter3Nationalities", SUPPORTER_3_HAS_PASSPORT,
            SUPPORTER_3_PASSPORT, FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES_1);

    }

    private void setSupporter4Details(BailCase bailCase, Map<String, Object> fieldValues) {
        setSupporterDetails(bailCase, fieldValues, HAS_FINANCIAL_COND_SUPPORTER_4, SUPPORTER_4_GIVEN_NAMES,
            SUPPORTER_4_FAMILY_NAMES, SUPPORTER_4_ADDRESS_DETAILS, "supporter4AddressLine1",
            "supporter4AddressLine2", "supporter4AddressLine3",
            "supporter4AddressPostTown", "supporter4AddressCounty",
            "supporter4AddressPostCode", "supporter4AddressCountry",
            SUPPORTER_4_TELEPHONE_NUMBER_1, SUPPORTER_4_MOBILE_NUMBER_1, SUPPORTER_4_EMAIL_ADDRESS_1,
            SUPPORTER_4_DOB, SUPPORTER_4_RELATION, SUPPORTER_4_OCCUPATION, SUPPORTER_4_IMMIGRATION,
            SUPPORTER_4_NATIONALITY, "supporter4Nationalities", SUPPORTER_4_HAS_PASSPORT,
            SUPPORTER_4_PASSPORT, FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES_1);
    }

    private void setSupporterDetails(BailCase bailCase, Map<String, Object> fieldValues,
                                     BailCaseFieldDefinition hasFinancialSupporter, BailCaseFieldDefinition supporterGivenName,
                                     BailCaseFieldDefinition supporterFamilyName, BailCaseFieldDefinition supporterAddress,
                                     String supporterAddressLine1, String supporterAddressLine2,
                                     String supporterAddressLine3, String supporterAddressPostTown,
                                     String supporterAddressCounty, String supporterAddressPostCode,
                                     String supporterAddressCountry, BailCaseFieldDefinition supporterTelephoneNumber,
                                     BailCaseFieldDefinition supporterMobileNumber, BailCaseFieldDefinition supporterEmailAddress,
                                     BailCaseFieldDefinition supporterDob, BailCaseFieldDefinition supporterRelation,
                                     BailCaseFieldDefinition supporterOccupation, BailCaseFieldDefinition supporterImmigration,
                                     BailCaseFieldDefinition supporterNationality, String supporterNationalities, BailCaseFieldDefinition supporterHasPassport,
                                     BailCaseFieldDefinition supporterPassport, BailCaseFieldDefinition supporterAmountUndertakes) {
        fieldValues.put(hasFinancialSupporter.value(), bailCase.read(hasFinancialSupporter, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(hasFinancialSupporter, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put(supporterGivenName.value(), bailCase.read(supporterGivenName, String.class).orElse(""));
            fieldValues.put(supporterFamilyName.value(), bailCase.read(supporterFamilyName, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(supporterAddress, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk suppAddress = optionalSupporterAddress.get();
                fieldValues.put(
                    supporterAddress.value(),
                    ImmutableMap
                        .builder()
                        .put(supporterAddressLine1, suppAddress.getAddressLine1().orElse(""))
                        .put(supporterAddressLine2, suppAddress.getAddressLine2().orElse(""))
                        .put(supporterAddressLine3, suppAddress.getAddressLine3().orElse(""))
                        .put(supporterAddressPostTown, suppAddress.getPostTown().orElse(""))
                        .put(supporterAddressCounty, suppAddress.getCounty().orElse(""))
                        .put(supporterAddressPostCode, suppAddress.getPostCode().orElse(""))
                        .put(supporterAddressCountry, suppAddress.getCountry().orElse(""))
                        .build()
                );
            }
            fieldValues.put(supporterTelephoneNumber.value(), bailCase.read(supporterTelephoneNumber, String.class).orElse(""));
            fieldValues.put(supporterMobileNumber.value(), bailCase.read(supporterMobileNumber, String.class).orElse(""));
            fieldValues.put(supporterEmailAddress.value(), bailCase.read(supporterEmailAddress, String.class).orElse(""));
            fieldValues.put(supporterDob.value(), formatDateForRendering(bailCase.read(supporterDob, String.class).orElse("")));
            fieldValues.put(supporterRelation.value(), bailCase.read(supporterRelation, String.class).orElse(""));
            fieldValues.put(supporterOccupation.value(), bailCase.read(supporterOccupation, String.class).orElse(""));
            fieldValues.put(supporterImmigration.value(), bailCase.read(supporterImmigration, String.class).orElse(""));
            Optional<List<IdValue<NationalityFieldValue>>> supporter4Nationalities = bailCase
                .read(supporterNationality);
            fieldValues.put(
                supporterNationalities,
                supporter4Nationalities
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(idValue -> idValue.getValue().getCode())
                    .map(nationality -> ImmutableMap.of(NATIONALITY, nationality))
                    .collect(Collectors.toList())
            );
            fieldValues.put(supporterHasPassport.value(), bailCase.read(supporterHasPassport, YesOrNo.class).orElse(YesOrNo.NO));
            if (!bailCase.read(supporterPassport, String.class).orElse("").isEmpty()) {
                fieldValues.put(supporterPassport.value(), bailCase.read(supporterPassport, String.class).orElse(""));
            }
            fieldValues.put(supporterAmountUndertakes.value(), bailCase.read(supporterAmountUndertakes, String.class).orElse(""));
        }
    }

    private void updateHearingDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("interpreterYesNo", bailCase.read(INTERPRETER_YES_NO, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(INTERPRETER_YES_NO, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            Optional<List<IdValue<InterpreterLanguage>>> interpreterLanguages = bailCase.read(INTERPRETER_LANGUAGES);
            fieldValues.put(
                "interpreterLanguages",
                interpreterLanguages
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(language -> new InterpreterLanguage(language.getValue().getLanguage(), language.getValue().getLanguageDialect()))
                    .collect(Collectors.toList())
            );
        }
        fieldValues.put("applicantDisability1", bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(APPLICANT_DISABILITY1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("applicantDisabilityDetails", bailCase.read(APPLICANT_DISABILITY_DETAILS, String.class).orElse(""));
        }

        fieldValues.put("videoHearing1", bailCase.read(VIDEO_HEARING1, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(VIDEO_HEARING1, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.NO) {
            fieldValues.put("videoHearingDetails", bailCase.read(VIDEO_HEARING_DETAILS, String.class).orElse(""));
        }
    }

    private void setApplicantInterpreterLanguageDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        setInterpreterLanguageDetails(bailCase, fieldValues, APPLICANT_INTERPRETER_LANGUAGE_CATEGORY,
            APPLICANT_INTERPRETER_SPOKEN_LANGUAGE, APPLICANT_INTERPRETER_SIGN_LANGUAGE);

    }

    private void setFcs1InterpreterLanguageDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        setInterpreterLanguageDetails(bailCase, fieldValues, FCS1_INTERPRETER_LANGUAGE_CATEGORY,
            FCS1_INTERPRETER_SPOKEN_LANGUAGE, FCS1_INTERPRETER_SIGN_LANGUAGE);
    }

    private void setFcs2InterpreterLanguageDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        setInterpreterLanguageDetails(bailCase, fieldValues, FCS2_INTERPRETER_LANGUAGE_CATEGORY,
            FCS2_INTERPRETER_SPOKEN_LANGUAGE, FCS2_INTERPRETER_SIGN_LANGUAGE);
    }

    private void setFcs3InterpreterLanguageDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        setInterpreterLanguageDetails(bailCase, fieldValues, FCS3_INTERPRETER_LANGUAGE_CATEGORY,
            FCS3_INTERPRETER_SPOKEN_LANGUAGE, FCS3_INTERPRETER_SIGN_LANGUAGE);
    }

    private void setFcs4InterpreterLanguageDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        setInterpreterLanguageDetails(bailCase, fieldValues, FCS4_INTERPRETER_LANGUAGE_CATEGORY,
            FCS4_INTERPRETER_SPOKEN_LANGUAGE, FCS4_INTERPRETER_SIGN_LANGUAGE);
    }


    private void setInterpreterLanguageDetails(BailCase bailCase, Map<String, Object> fieldValues, BailCaseFieldDefinition langCategoryField,
                                               BailCaseFieldDefinition spokenLanguageField, BailCaseFieldDefinition signLanguageField) {

        Optional<List<String>> languageCategoriesOptional = bailCase.read(langCategoryField);

        if (languageCategoriesOptional.isPresent()) {
            List<String> languageCategories = languageCategoriesOptional.get();

            if (languageCategories.contains(SPOKEN_LANGUAGE_INTERPRETER.getValue())
                && languageCategories.contains(SIGN_LANGUAGE_INTERPRETER.getValue())) {
                fieldValues.put(langCategoryField.value(), SPOKEN_INTERPRETER_LABEL + "\n" + SIGN_INTERPRETER_LABEL);
            } else if (languageCategories.contains(SPOKEN_LANGUAGE_INTERPRETER.getValue())) {
                fieldValues.put(langCategoryField.value(), SPOKEN_INTERPRETER_LABEL);
            } else if (languageCategories.contains(SIGN_LANGUAGE_INTERPRETER.getValue())) {
                fieldValues.put(langCategoryField.value(), SIGN_INTERPRETER_LABEL);
            }

            Optional<BailInterpreterLanguageRefData> fcs4SpokenInterpreterLanguage = bailCase.read(spokenLanguageField, BailInterpreterLanguageRefData.class)
                .filter(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null);
            Optional<BailInterpreterLanguageRefData> fcs4SignInterpreterLanguage = bailCase.read(signLanguageField, BailInterpreterLanguageRefData.class)
                .filter(language -> language.getLanguageRefData() != null || language.getLanguageManualEntryDescription() != null);

            fcs4SpokenInterpreterLanguage.ifPresent(language -> {
                if (language.getLanguageRefData() != null && language.getLanguageManualEntry().equals(IS_NOT_MANUAL_ENTRY)) {
                    fieldValues.put(spokenLanguageField.value(), language.getLanguageRefData().getValue().getLabel());
                } else if (language.getLanguageManualEntry() != null && language.getLanguageManualEntry().equals(IS_MANUAL_ENTRY)) {
                    fieldValues.put(spokenLanguageField.value(), language.getLanguageManualEntryDescription());
                }
            });

            fcs4SignInterpreterLanguage.ifPresent(language -> {
                if (language.getLanguageRefData() != null && language.getLanguageManualEntry().equals(IS_NOT_MANUAL_ENTRY)) {
                    fieldValues.put(signLanguageField.value(), language.getLanguageRefData().getValue().getLabel());
                } else if (language.getLanguageManualEntry() != null && language.getLanguageManualEntry().equals(IS_MANUAL_ENTRY)) {
                    fieldValues.put(signLanguageField.value(), language.getLanguageManualEntryDescription());
                }
            });
        }
    }

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }
        return "";
    }

    private String formatLegalRepName(@NonNull String firstName, @NonNull String lastName) {
        if (!(lastName.isEmpty() || firstName.isEmpty())) {
            return firstName + " " + lastName;
        }

        return firstName;
    }
}
