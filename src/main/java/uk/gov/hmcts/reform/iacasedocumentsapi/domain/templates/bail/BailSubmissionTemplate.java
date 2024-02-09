package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.bail;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCaseFieldDefinition.*;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.InterpreterLanguage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.ImaFeatureTogglerHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates.DocumentTemplate;

@Component
public class BailSubmissionTemplate implements DocumentTemplate<BailCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final String SENT_BY_LR = "Legal Representative";
    private static final String SENT_BY_HO = "Home Office";
    private static final String NATIONALITY = "nationality";
    private static final String DONT_KNOW_SELECTED_VALUE = "Don't Know";
    private final ImaFeatureTogglerHandler imaFeatureTogglerHandler;

    private final String templateName;
    private final String templateNameWithoutUt;

    public BailSubmissionTemplate(
        @Value("${bailSubmissionDocument.templateName}") String templateName,
        @Value("${bailSubmissionDocumentWithoutUt.templateName}") String templateNameWithoutUt,
        ImaFeatureTogglerHandler imaFeatureTogglerHandler
    ) {
        this.templateName = templateName;
        this.templateNameWithoutUt = templateNameWithoutUt;
        this.imaFeatureTogglerHandler = imaFeatureTogglerHandler;
    }

    public String getName() {
        return imaFeatureTogglerHandler.isImaEnabled() ? templateName : templateNameWithoutUt;
    }

    @Override
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

        if (imaFeatureTogglerHandler.isImaEnabled()) {
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

        setSupporterDetails(bailCase, fieldValues);
        setSupporter2Details(bailCase, fieldValues);
        setSupporter3Details(bailCase, fieldValues);
        setSupporter4Details(bailCase, fieldValues);

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
            fieldValues.put("legalRepName", bailCase.read(LEGAL_REP_NAME, String.class).orElse(""));
            fieldValues.put("legalRepEmail", bailCase.read(LEGAL_REP_EMAIL, String.class).orElse(""));
            fieldValues.put("legalRepPhone", bailCase.read(LEGAL_REP_PHONE, String.class).orElse(""));
            fieldValues.put("legalRepReference", bailCase.read(LEGAL_REP_REFERENCE, String.class).orElse(""));
        }

        if (bailCase.read(PRIOR_APPLICATIONS, PriorApplication.class).orElse(null) == null) {
            fieldValues.put("showPreviousApplicationSection", YesOrNo.YES);
        }

        return fieldValues;
    }

    private void setSupporterDetails(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("hasFinancialCondSupporter", bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporterGivenNames", bailCase.read(SUPPORTER_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporterFamilyNames", bailCase.read(SUPPORTER_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();

                fieldValues.put(
                    "supporterAddressDetails",
                    ImmutableMap
                        .builder()
                        .put("supporterAddressLine1", supporterAddress.getAddressLine1().orElse(""))
                        .put("supporterAddressLine2", supporterAddress.getAddressLine2().orElse(""))
                        .put("supporterAddressLine3", supporterAddress.getAddressLine3().orElse(""))
                        .put("supporterAddressPostTown", supporterAddress.getPostTown().orElse(""))
                        .put("supporterAddressCounty", supporterAddress.getCounty().orElse(""))
                        .put("supporterAddressPostCode", supporterAddress.getPostCode().orElse(""))
                        .put("supporterAddressCountry", supporterAddress.getCountry().orElse(""))
                        .build()
                );
            }
            fieldValues.put("supporterTelephoneNumber1", bailCase.read(SUPPORTER_TELEPHONE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporterMobileNumber1", bailCase.read(SUPPORTER_MOBILE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporterEmailAddress1", bailCase.read(SUPPORTER_EMAIL_ADDRESS_1, String.class).orElse(""));
            fieldValues.put("supporterDOB", formatDateForRendering(bailCase.read(SUPPORTER_DOB, String.class).orElse("")));
            fieldValues.put("supporterRelation", bailCase.read(SUPPORTER_RELATION, String.class).orElse(""));
            fieldValues.put("supporterOccupation", bailCase.read(SUPPORTER_OCCUPATION, String.class).orElse(""));
            fieldValues.put("supporterImmigration", bailCase.read(SUPPORTER_IMMIGRATION, String.class).orElse(""));
            Optional<List<IdValue<NationalityFieldValue>>> supporterNationalities = bailCase
                .read(SUPPORTER_NATIONALITY);
            fieldValues.put(
                "supporterNationalities",
                supporterNationalities
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(idValue -> idValue.getValue().getCode())
                    .map(nationality -> ImmutableMap.of(NATIONALITY, nationality))
                    .collect(Collectors.toList())
            );
            fieldValues.put("supporterHasPassport", bailCase.read(SUPPORTER_HAS_PASSPORT, YesOrNo.class).orElse(YesOrNo.NO));
            if (!bailCase.read(SUPPORTER_PASSPORT, String.class).orElse("").isEmpty()) {
                fieldValues.put("supporterPassport", bailCase.read(SUPPORTER_PASSPORT, String.class).orElse(""));
            }
            fieldValues.put("financialAmountSupporterUndertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_UNDERTAKES_1, String.class).orElse(""));
        }
    }

    private void setSupporter2Details(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("hasFinancialCondSupporter2", bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_2, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporter2GivenNames", bailCase.read(SUPPORTER_2_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporter2FamilyNames", bailCase.read(SUPPORTER_2_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_2_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();

                fieldValues.put(
                    "supporter2AddressDetails",
                    ImmutableMap
                        .builder()
                        .put("supporter2AddressLine1", supporterAddress.getAddressLine1().orElse(""))
                        .put("supporter2AddressLine2", supporterAddress.getAddressLine2().orElse(""))
                        .put("supporter2AddressLine3", supporterAddress.getAddressLine3().orElse(""))
                        .put("supporter2AddressPostTown", supporterAddress.getPostTown().orElse(""))
                        .put("supporter2AddressCounty", supporterAddress.getCounty().orElse(""))
                        .put("supporter2AddressPostCode", supporterAddress.getPostCode().orElse(""))
                        .put("supporter2AddressCountry", supporterAddress.getCountry().orElse(""))
                        .build()
                );
            }

            fieldValues.put("supporter2TelephoneNumber1", bailCase.read(SUPPORTER_2_TELEPHONE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporter2MobileNumber1", bailCase.read(SUPPORTER_2_MOBILE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporter2EmailAddress1", bailCase.read(SUPPORTER_2_EMAIL_ADDRESS_1, String.class).orElse(""));
            fieldValues.put("supporter2DOB", formatDateForRendering(bailCase.read(SUPPORTER_2_DOB, String.class).orElse("")));
            fieldValues.put("supporter2Relation", bailCase.read(SUPPORTER_2_RELATION, String.class).orElse(""));
            fieldValues.put("supporter2Occupation", bailCase.read(SUPPORTER_2_OCCUPATION, String.class).orElse(""));
            fieldValues.put("supporter2Immigration", bailCase.read(SUPPORTER_2_IMMIGRATION, String.class).orElse(""));
            Optional<List<IdValue<NationalityFieldValue>>> supporter2Nationalities = bailCase
                .read(SUPPORTER_2_NATIONALITY);
            fieldValues.put(
                "supporter2Nationalities",
                supporter2Nationalities
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(idValue -> idValue.getValue().getCode())
                    .map(nationality -> ImmutableMap.of(NATIONALITY, nationality))
                    .collect(Collectors.toList())
            );
            fieldValues.put("supporter2HasPassport", bailCase.read(SUPPORTER_2_HAS_PASSPORT, YesOrNo.class).orElse(YesOrNo.NO));
            if (!bailCase.read(SUPPORTER_2_PASSPORT, String.class).orElse("").isEmpty()) {
                fieldValues.put("supporter2Passport", bailCase.read(SUPPORTER_2_PASSPORT, String.class).orElse(""));
            }
            fieldValues.put("financialAmountSupporter2Undertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_2_UNDERTAKES_1, String.class).orElse(""));
        }
    }

    private void setSupporter3Details(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("hasFinancialCondSupporter3", bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_3, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporter3GivenNames", bailCase.read(SUPPORTER_3_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporter3FamilyNames", bailCase.read(SUPPORTER_3_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_3_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();

                fieldValues.put(
                    "supporter3AddressDetails",
                    ImmutableMap
                        .builder()
                        .put("supporter3AddressLine1", supporterAddress.getAddressLine1().orElse(""))
                        .put("supporter3AddressLine2", supporterAddress.getAddressLine2().orElse(""))
                        .put("supporter3AddressLine3", supporterAddress.getAddressLine3().orElse(""))
                        .put("supporter3AddressPostTown", supporterAddress.getPostTown().orElse(""))
                        .put("supporter3AddressCounty", supporterAddress.getCounty().orElse(""))
                        .put("supporter3AddressPostCode", supporterAddress.getPostCode().orElse(""))
                        .put("supporter3AddressCountry", supporterAddress.getCountry().orElse(""))
                        .build()
                );
            }
            fieldValues.put("supporter3TelephoneNumber1", bailCase.read(SUPPORTER_3_TELEPHONE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporter3MobileNumber1", bailCase.read(SUPPORTER_3_MOBILE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporter3EmailAddress1", bailCase.read(SUPPORTER_3_EMAIL_ADDRESS_1, String.class).orElse(""));
            fieldValues.put("supporter3DOB", formatDateForRendering(bailCase.read(SUPPORTER_3_DOB, String.class).orElse("")));
            fieldValues.put("supporter3Relation", bailCase.read(SUPPORTER_3_RELATION, String.class).orElse(""));
            fieldValues.put("supporter3Occupation", bailCase.read(SUPPORTER_3_OCCUPATION, String.class).orElse(""));
            fieldValues.put("supporter3Immigration", bailCase.read(SUPPORTER_3_IMMIGRATION, String.class).orElse(""));
            Optional<List<IdValue<NationalityFieldValue>>> supporter3Nationalities = bailCase
                .read(SUPPORTER_3_NATIONALITY);
            fieldValues.put(
                "supporter3Nationalities",
                supporter3Nationalities
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(idValue -> idValue.getValue().getCode())
                    .map(nationality -> ImmutableMap.of(NATIONALITY, nationality))
                    .collect(Collectors.toList())
            );
            fieldValues.put("supporter3HasPassport", bailCase.read(SUPPORTER_3_HAS_PASSPORT, YesOrNo.class).orElse(YesOrNo.NO));
            if (!bailCase.read(SUPPORTER_3_PASSPORT, String.class).orElse("").isEmpty()) {
                fieldValues.put("supporter3Passport", bailCase.read(SUPPORTER_3_PASSPORT, String.class).orElse(""));
            }
            fieldValues.put("financialAmountSupporter3Undertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_3_UNDERTAKES_1, String.class).orElse(""));
        }
    }

    private void setSupporter4Details(BailCase bailCase, Map<String, Object> fieldValues) {
        fieldValues.put("hasFinancialCondSupporter4", bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class).orElse(YesOrNo.NO));
        if (bailCase.read(HAS_FINANCIAL_COND_SUPPORTER_4, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            fieldValues.put("supporter4GivenNames", bailCase.read(SUPPORTER_4_GIVEN_NAMES, String.class).orElse(""));
            fieldValues.put("supporter4FamilyNames", bailCase.read(SUPPORTER_4_FAMILY_NAMES, String.class).orElse(""));
            Optional<AddressUk> optionalSupporterAddress = bailCase.read(SUPPORTER_4_ADDRESS_DETAILS, AddressUk.class);
            if (optionalSupporterAddress.isPresent()) {
                AddressUk supporterAddress = optionalSupporterAddress.get();
                fieldValues.put(
                    "supporter4AddressDetails",
                    ImmutableMap
                        .builder()
                        .put("supporter4AddressLine1", supporterAddress.getAddressLine1().orElse(""))
                        .put("supporter4AddressLine2", supporterAddress.getAddressLine2().orElse(""))
                        .put("supporter4AddressLine3", supporterAddress.getAddressLine3().orElse(""))
                        .put("supporter4AddressPostTown", supporterAddress.getPostTown().orElse(""))
                        .put("supporter4AddressCounty", supporterAddress.getCounty().orElse(""))
                        .put("supporter4AddressPostCode", supporterAddress.getPostCode().orElse(""))
                        .put("supporter4AddressCountry", supporterAddress.getCountry().orElse(""))
                        .build()
                );
            }
            fieldValues.put("supporter4TelephoneNumber1", bailCase.read(SUPPORTER_4_TELEPHONE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporter4MobileNumber1", bailCase.read(SUPPORTER_4_MOBILE_NUMBER_1, String.class).orElse(""));
            fieldValues.put("supporter4EmailAddress1", bailCase.read(SUPPORTER_4_EMAIL_ADDRESS_1, String.class).orElse(""));
            fieldValues.put("supporter4DOB", formatDateForRendering(bailCase.read(SUPPORTER_4_DOB, String.class).orElse("")));
            fieldValues.put("supporter4Relation", bailCase.read(SUPPORTER_4_RELATION, String.class).orElse(""));
            fieldValues.put("supporter4Occupation", bailCase.read(SUPPORTER_4_OCCUPATION, String.class).orElse(""));
            fieldValues.put("supporter4Immigration", bailCase.read(SUPPORTER_4_IMMIGRATION, String.class).orElse(""));
            Optional<List<IdValue<NationalityFieldValue>>> supporter4Nationalities = bailCase
                .read(SUPPORTER_4_NATIONALITY);
            fieldValues.put(
                "supporter4Nationalities",
                supporter4Nationalities
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(idValue -> idValue.getValue().getCode())
                    .map(nationality -> ImmutableMap.of(NATIONALITY, nationality))
                    .collect(Collectors.toList())
            );
            fieldValues.put("supporter4HasPassport", bailCase.read(SUPPORTER_4_HAS_PASSPORT, YesOrNo.class).orElse(YesOrNo.NO));
            if (!bailCase.read(SUPPORTER_4_PASSPORT, String.class).orElse("").isEmpty()) {
                fieldValues.put("supporter4Passport", bailCase.read(SUPPORTER_4_PASSPORT, String.class).orElse(""));
            }
            fieldValues.put("financialAmountSupporter4Undertakes1", bailCase.read(FINANCIAL_AMOUNT_SUPPORTER_4_UNDERTAKES_1, String.class).orElse(""));
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

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(DOCUMENT_DATE_FORMAT);
        }
        return "";
    }
}
