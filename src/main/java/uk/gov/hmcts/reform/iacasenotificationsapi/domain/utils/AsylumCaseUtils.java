package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.NationalityGovUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AccessCodeGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

public class AsylumCaseUtils {

    public static final String HOME_OFFICE = "Home office";
    public static final String LEGAL_REPRESENTATIVE = "Legal representative";
    public static final String JUDGE = "Tribunal";
    private static final String INCORRECT_APPLICANT_TYPE_ERROR_MESSAGE = "Correct applicant type is not present";
    private static final String INCORRECT_RESPONDENT_TYPE_ERROR_MESSAGE = "Correct respondent type is not present";


    private AsylumCaseUtils() {
        // prevent public constructor for Sonar
    }

    public static boolean isAppellantInDetention(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(NO)
            .equals(YES);
    }

    public static boolean isLegalRepEjp(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_REFERENCE_EJP, String.class).isPresent();
    }

    public static boolean isAgeAssessmentAppeal(AsylumCase asylumCase) {
        return (asylumCase.read(APPEAL_TYPE, AppealType.class)).orElse(null) == AppealType.AG;
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static boolean isNotInternalOrIsInternalWithLegalRepresentation(AsylumCase asylumCase) {
        return (!isInternalCase(asylumCase) ||
            isInternalCase(asylumCase) && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase));
    }

    public static boolean inCountryAppeal(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(false);
    }

    public static boolean legalRepInCountryAppeal(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(false);
    }

    public static boolean isAriaMigrated(AsylumCase asylumCase) {
        return asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static Optional<FtpaDecisionOutcomeType> getFtpaDecisionOutcomeType(AsylumCase asylumCase) {
        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);
        if (ftpaDecisionOutcomeType.isPresent()) {
            return ftpaDecisionOutcomeType;
        }
        return asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);
    }

    public static boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }

    public static String getDetentionFacilityName(AsylumCase asylumCase) {
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
            .orElse("");
        switch (detentionFacility) {
            case "immigrationRemovalCentre":
                return getFacilityName(IRC_NAME, asylumCase);
            case "prison":
                return getFacilityName(PRISON_NAME, asylumCase);
            case "other":
                return asylumCase.read(OTHER_DETENTION_FACILITY_NAME, OtherDetentionFacilityName.class)
                    .orElseThrow(() -> new RequiredFieldMissingException("Other detention facility name is missing")).getOther();
            default:
                throw new RequiredFieldMissingException("Detention Facility is missing");
        }
    }

    public static DocumentWithMetadata getLetterForNotification(AsylumCase asylumCase, DocumentTag documentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalNotificationLetters = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);
        return optionalNotificationLetters
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == documentTag)
            .findFirst().orElseThrow(() -> new IllegalStateException(documentTag + " document not available"));
    }

    private static String getFacilityName(AsylumCaseDefinition field, AsylumCase asylumCase) {
        return asylumCase.read(field, String.class)
            .orElseThrow(() -> new RequiredFieldMissingException(field.name() + " is missing"));
    }

    public static boolean isAipJourney(AsylumCase asylumCase) {

        return asylumCase
            .read(JOURNEY_TYPE, JourneyType.class)
            .map(type -> type == AIP).orElse(false);
    }

    public static List<IdValue<DocumentWithMetadata>> getAddendumEvidenceDocuments(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceDocuments =
            asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS);
        if (maybeExistingAdditionalEvidenceDocuments.isEmpty()) {
            return Collections.emptyList();
        }

        return maybeExistingAdditionalEvidenceDocuments.get();
    }

    public static Optional<IdValue<DocumentWithMetadata>> getLatestAddendumEvidenceDocument(AsylumCase asylumCase) {
        List<IdValue<DocumentWithMetadata>> addendums = getAddendumEvidenceDocuments(asylumCase);

        if (addendums.isEmpty()) {
            return Optional.empty();
        }

        Optional<IdValue<DocumentWithMetadata>> optionalLatestAddendum = addendums.stream().findFirst();

        return optionalLatestAddendum.isEmpty() ? Optional.empty() : Optional.of(optionalLatestAddendum.get());
    }

    // This method uses the isEjp field which is set yes for EJP when a case is saved or no if paper form
    public static boolean isEjpCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_EJP, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;
    }

    public static ImmutablePair<String, String> getApplicantAndRespondent(AsylumCase asylumCase, Function<AsylumCase, ApplyForCosts> retrieveApplyForCosts) {
        List<String> availableRoles = List.of(HOME_OFFICE, LEGAL_REPRESENTATIVE, JUDGE);

        ApplyForCosts applyForCosts = retrieveApplyForCosts.apply(asylumCase);

        final String applicantType = applyForCosts.getApplyForCostsApplicantType();
        final String respondentType = applyForCosts.getApplyForCostsRespondentRole();

        if (!availableRoles.contains(applicantType)) {
            throw new IllegalStateException(INCORRECT_APPLICANT_TYPE_ERROR_MESSAGE);
        }
        if (!availableRoles.contains(respondentType)) {
            throw new IllegalStateException(INCORRECT_RESPONDENT_TYPE_ERROR_MESSAGE);
        }

        return new ImmutablePair<>(applicantType, respondentType);
    }

    public static ApplyForCosts retrieveLatestApplyForCosts(AsylumCase asylumCase) {
        Optional<List<IdValue<ApplyForCosts>>> applyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        if (applyForCosts.isPresent()) {
            List<IdValue<ApplyForCosts>> applyForCostsList = applyForCosts.get();
            return applyForCostsList.get(0).getValue();
        } else {
            throw new IllegalStateException("Applies for costs are not present");
        }
    }

    public static ApplyForCosts getApplicationById(AsylumCase asylumCase, AsylumCaseDefinition definition) {
        DynamicList applyForCostsDynamicList = asylumCase.read(definition, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException(definition.value() + " is not present"));

        String applicationId = applyForCostsDynamicList.getValue().getCode();

        Optional<List<IdValue<ApplyForCosts>>> maybeApplyForCosts = asylumCase.read(APPLIES_FOR_COSTS);

        return maybeApplyForCosts
            .orElseThrow(() -> new IllegalStateException("appliesForCost are not present"))
            .stream()
            .filter(applyForCosts -> applyForCosts.getId().equals(applicationId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Apply for costs with id " + applicationId + " not found"))
            .getValue();
    }

    public static boolean isLoggedUserIsHomeOffice(AsylumCase asylumCase, Function<AsylumCase, ApplyForCosts> retrieveApplyForCosts) {
        ApplyForCosts selectedApplication = retrieveApplyForCosts.apply(asylumCase);

        if (selectedApplication.getLoggedUserRole().equals(HOME_OFFICE)) {
            return true;
        } else if (selectedApplication.getLoggedUserRole().equals(LEGAL_REPRESENTATIVE)) {
            return false;
        }
        throw new IllegalStateException(INCORRECT_APPLICANT_TYPE_ERROR_MESSAGE);
    }

    public static PinInPostDetails generateAppellantPinIfNotPresent(AsylumCase asylumCase) {
        if (!asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class).isPresent()) {
            asylumCase.write(APPELLANT_PIN_IN_POST, PinInPostDetails.builder()
                .accessCode(AccessCodeGenerator.generateAccessCode())
                .expiryDate(LocalDate.now().plusDays(30).toString())
                .pinUsed(YesOrNo.NO)
                .build());
        }

        return asylumCase.read(APPELLANT_PIN_IN_POST, PinInPostDetails.class)
            .orElseThrow(() -> new IllegalStateException("Failed to generate appellantPinInPost."));
    }

    public static boolean isSubmissionOutOfTime(AsylumCase asylumCase) {
        return asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static YesOrNo isAppellantInUK(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.APPELLANT_IN_UK, YesOrNo.class).orElse(YesOrNo.NO);
    }

    public static List<String> getAppellantAddressAsList(final AsylumCase asylumCase) {
        AddressUk address = asylumCase
            .read(AsylumCaseDefinition.APPELLANT_ADDRESS, AddressUk.class)
            .orElseThrow(() -> new IllegalStateException("appellantAddress is not present"));

        List<String> appellantAddressAsList = new ArrayList<>();

        appellantAddressAsList.add(address.getAddressLine1().orElseThrow(() -> new IllegalStateException("appellantAddress line 1 is not present")));
        String addressLine2 = address.getAddressLine2().orElse(null);
        String addressLine3 = address.getAddressLine3().orElse(null);

        if (addressLine2 != null) {
            appellantAddressAsList.add(addressLine2);
        }
        if (addressLine3 != null) {
            appellantAddressAsList.add(addressLine3);
        }
        appellantAddressAsList.add(address.getPostTown().orElseThrow(() -> new IllegalStateException("appellantAddress postTown is not present")));
        appellantAddressAsList.add(address.getPostCode().orElseThrow(() -> new IllegalStateException("appellantAddress postCode is not present")));

        return appellantAddressAsList;
    }

    public static List<String> getAppellantAddressAsListOoc(final AsylumCase asylumCase) {

        String oocAddressLine1 = asylumCase
            .read(ADDRESS_LINE_1_ADMIN_J, String.class)
            .orElseThrow(() -> new IllegalStateException("OOC Address line 1 is not present"));

        String oocAddressLine2 = asylumCase
            .read(ADDRESS_LINE_2_ADMIN_J, String.class)
            .orElseThrow(() -> new IllegalStateException("OOC Address line 2 is not present"));

        List<String> appellantAddressAsList = new ArrayList<>();

        appellantAddressAsList.add(oocAddressLine1);
        appellantAddressAsList.add(oocAddressLine2);

        String oocAddressLine3 = asylumCase
            .read(ADDRESS_LINE_3_ADMIN_J, String.class)
            .orElse(null);

        String oocAddressLine4 = asylumCase
            .read(ADDRESS_LINE_4_ADMIN_J, String.class)
            .orElse(null);

        NationalityGovUk oocAddressCountry = NationalityGovUk.valueOf(asylumCase
            .read(COUNTRY_GOV_UK_OOC_ADMIN_J, NationalityFieldValue.class)
            .orElseThrow(() -> new IllegalStateException("OOC Address country is not present")).getCode());

        if (oocAddressLine3 != null) {
            appellantAddressAsList.add(oocAddressLine3);
        }
        if (oocAddressLine4 != null) {
            appellantAddressAsList.add(oocAddressLine4);
        }
        appellantAddressAsList.add(oocAddressCountry.toString());

        return appellantAddressAsList;
    }

    public static List<String> getLegalRepresentativeAddressAsList(final AsylumCase asylumCase) {
        AddressUk address = asylumCase
            .read(LEGAL_REP_ADDRESS_U_K, AddressUk.class)
            .orElseThrow(() -> new IllegalStateException("legalRepAddressUK is not present"));

        List<String> legalRepAddressAsList = new ArrayList<>();

        legalRepAddressAsList.add(address.getAddressLine1().orElseThrow(() -> new IllegalStateException("legalRepAddress line 1 is not present")));
        String addressLine2 = address.getAddressLine2().orElse(null);
        String addressLine3 = address.getAddressLine3().orElse(null);

        if (addressLine2 != null) {
            legalRepAddressAsList.add(addressLine2);
        }
        if (addressLine3 != null) {
            legalRepAddressAsList.add(addressLine3);
        }
        legalRepAddressAsList.add(address.getPostTown().orElseThrow(() -> new IllegalStateException("legalRepAddress postTown is not present")));
        legalRepAddressAsList.add(address.getPostCode().orElseThrow(() -> new IllegalStateException("legalRepAddress postCode is not present")));

        return legalRepAddressAsList;
    }

    public static List<String> getLegalRepresentativeAddressOocAsList(final AsylumCase asylumCase) {

        String addressLine1 = asylumCase
            .read(OOC_ADDRESS_LINE_1, String.class)
            .orElseThrow(() -> new IllegalStateException("Ooc Legal Rep Address line 1 is not present"));

        String addressLine2 = asylumCase
            .read(OOC_ADDRESS_LINE_2, String.class)
            .orElseThrow(() -> new IllegalStateException("Ooc Legal Rep Address line 2 is not present"));

        List<String> legalRepAddressAsList = new ArrayList<>();

        legalRepAddressAsList.add(addressLine1);
        legalRepAddressAsList.add(addressLine2);

        String addressLine3 = asylumCase
            .read(OOC_ADDRESS_LINE_3, String.class)
            .orElse(null);

        String addressLine4 = asylumCase
            .read(OOC_ADDRESS_LINE_4, String.class)
            .orElse(null);

        NationalityGovUk oocLrCountryGovUkAdminJ = NationalityGovUk.valueOf(asylumCase
            .read(OOC_LR_COUNTRY_GOV_UK_ADMIN_J, NationalityFieldValue.class)
            .orElseThrow(() -> new IllegalStateException("oocLrCountryGovUkAdminJ is not present")).getCode());

        if (addressLine3 != null) {
            legalRepAddressAsList.add(addressLine3);
        }
        if (addressLine4 != null) {
            legalRepAddressAsList.add(addressLine4);
        }
        legalRepAddressAsList.add(oocLrCountryGovUkAdminJ.toString());

        return legalRepAddressAsList;
    }

    public static String convertAsylumCaseFeeValue(String amountFromAsylumCase) {
        return StringUtils.isNotBlank(amountFromAsylumCase)
            ? new BigDecimal(String.valueOf(Double.parseDouble(amountFromAsylumCase) / 100))
            .setScale(2, RoundingMode.DOWN).toString()
            : "";
    }

    public static String calculateFeeDifference(String originalFeeTotal, String newFeeTotal) {
        try {

            BigDecimal originalFee = new BigDecimal(String.valueOf(Double.parseDouble(originalFeeTotal) / 100));
            BigDecimal newFee = new BigDecimal(String.valueOf(Double.parseDouble(newFeeTotal) / 100));
            BigDecimal difference = originalFee.subtract(newFee).abs();
            return difference.setScale(2, RoundingMode.DOWN).toString();

        } catch (NumberFormatException e) {

            return "0.00";
        }
    }

    public static boolean hasAppellantAddressInCountryOrOutOfCountry(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)
                   .map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
               || asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)
                   .map(flag -> flag.equals(YesOrNo.YES)).orElse(false);
    }

    public static Set<String> getAppellantAddressInCountryOrOoc(final AsylumCase asylumCase) {
        return inCountryAppeal(asylumCase) ? Collections.singleton(getAppellantAddressAsList(asylumCase).stream()
            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_"))) :
            Collections.singleton(getAppellantAddressAsListOoc(asylumCase).stream()
                .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
    }

    public static Set<String> getLegalRepAddressInCountryOrOoc(final AsylumCase asylumCase) {
        return legalRepInCountryAppeal(asylumCase) ? Collections.singleton(getLegalRepresentativeAddressAsList(asylumCase).stream()
            .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_"))) :
            Collections.singleton(getLegalRepresentativeAddressOocAsList(asylumCase).stream()
                .map(item -> item.replaceAll("\\s", "")).collect(Collectors.joining("_")));
    }

    public static String getLegalRepEmailInternalOrLegalRepJourney(final AsylumCase asylumCase) {
        if (isInternalCase(asylumCase) && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)) {
            return asylumCase.read(LEGAL_REP_EMAIL, String.class).orElseThrow(() -> new IllegalStateException("legalRepEmail is not present"));
        } else if (isInternalCase(asylumCase) && !hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase)) {
            return StringUtils.EMPTY;
        } else {
            return asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));
        }
    }

    public static String getLegalRepEmailInternalOrLegalRepJourneyNonMandatory(final AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase) ? asylumCase.read(LEGAL_REP_EMAIL, String.class).orElse("")
            : asylumCase.read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class).orElse("");
    }

    public static boolean isDecisionWithoutHearingAppeal(AsylumCase asylumCase) {
        return asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)
            .map(yesOrNo -> YES == yesOrNo).orElse(false);
    }

    public static boolean hasBeenSubmittedByAppellantInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
            .map(yesOrNo -> YES == yesOrNo).orElse(false);
    }

    public static boolean hasBeenSubmittedAsLegalRepresentedInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(NO, yesOrNo)).orElse(false);
    }

    public static List<String> getAppellantOrLegalRepAddressLetterPersonalisation(AsylumCase asylumCase) {
        boolean appellantRepresentation = hasBeenSubmittedByAppellantInternalCase(asylumCase);
        List<String> address;
        // Internal appellant no representation - use appellant address
        if (appellantRepresentation) {
            address = inCountryAppeal(asylumCase) ?
                getAppellantAddressAsList(asylumCase) :
                getAppellantAddressAsListOoc(asylumCase);
            // Internal appellant has representation - use legal rep address
        } else {
            address = legalRepInCountryAppeal(asylumCase) ?
                getLegalRepresentativeAddressAsList(asylumCase) :
                getLegalRepresentativeAddressOocAsList(asylumCase);
        }
        return address;
    }

    public static String normalizeDecisionHearingOptionText(String decisionHearingFeeOption) {
        if ("decisionWithHearing".equals(decisionHearingFeeOption)) {
            return "Decision with hearing";
        } else if ("decisionWithoutHearing".equals(decisionHearingFeeOption)) {
            return "Decision without hearing";
        } else {
            return "";
        }
    }

}
