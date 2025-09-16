package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.DC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.RP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.EA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.EU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.HU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.NationalityGovUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.NationalityFieldValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;


public class AsylumCaseUtils {

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

    public static boolean isDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)
                .orElse(NO)
                .equals(YES);
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static boolean hasBeenSubmittedByAppellantInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
                .map(yesOrNo -> YES == yesOrNo).orElse(false);
    }

    public static boolean isInternalNonDetainedCase(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && !isAppellantInDetention(asylumCase);
    }

    public static boolean hasAppellantAddressInCountryOrOoc(AsylumCase asylumCase) {
        boolean appellantHasFixedUkAddress = asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)
            .map(flag -> flag.equals(YES))
            .orElse(false);

        boolean appellantHasFixedOutOfCountryAddress = asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)
            .map(flag -> flag.equals(YES))
            .orElse(false);

        return appellantHasFixedUkAddress || appellantHasFixedOutOfCountryAddress || isDetainedInFacilityType(asylumCase, OTHER);
    }

    public static List<IdValue<Direction>> getCaseDirections(AsylumCase asylumCase) {
        final Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);
        final List<IdValue<Direction>> existingDirections = maybeDirections
                .orElse(Collections.emptyList());
        return existingDirections;
    }

    public static List<Direction> getCaseDirectionsBasedOnTag(AsylumCase asylumCase, DirectionTag directionTag) {
        List<IdValue<Direction>> directions = getCaseDirections(asylumCase);

        return directions
                .stream()
                .map(IdValue::getValue)
                .filter(direction -> direction.getTag() == directionTag)
                .collect(Collectors.toList());
    }

    public static String getDirectionDueDate(AsylumCase asylumCase, DirectionTag tag) {
        Direction direction = getCaseDirectionsBasedOnTag(asylumCase, tag).get(0);
        return direction.getDateDue();
    }

    public static Map<String, String> getAppellantPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("hmcts", "[userImage:hmcts.png]")
                .putAll(getAppellantPersonalisationWithoutUserImage(asylumCase))
                .build();
    }

    public static Map<String, String> getAppellantPersonalisationWithoutUserImage(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(""))
                .build();
    }

    public static String dueDatePlusNumberOfWeeks(AsylumCase asylumCase, int numberOfWeeks) {
        LocalDate appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)
                .map(LocalDate::parse)
                .orElseThrow(() -> new IllegalStateException("appealSubmissionDate is missing"));

        return formatDateForNotificationAttachmentDocument(appealSubmissionDate.plusWeeks(numberOfWeeks));
    }

    public static String dueDatePlusNumberOfDays(AsylumCase asylumCase, int numberOfDays) {
        LocalDate appealSubmissionDate = asylumCase.read(APPEAL_SUBMISSION_DATE, String.class)
                .map(LocalDate::parse)
                .orElseThrow(() -> new IllegalStateException("Appeal submission date is missing"));

        return formatDateForNotificationAttachmentDocument(appealSubmissionDate.plusDays(numberOfDays));
    }

    public static String formatDateForRendering(String date, DateTimeFormatter formatter) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(formatter);
        }
        return "";
    }

    public static String formatDateTimeForRendering(String date, DateTimeFormatter formatter) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(formatter);
        }
        return "";
    }

    public static boolean isEaHuEuAppeal(AsylumCase asylumCase) {
        return asylumCase
                .read(APPEAL_TYPE, AsylumAppealType.class)
                .map(type -> type == EA || type == HU || type == EU).orElse(false);
    }

    public static double getFeeBeforeRemission(AsylumCase asylumCase) {
        double feeAmountInPence = Double.parseDouble(asylumCase.read(FEE_AMOUNT_GBP, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Fee amount not found")));
        return feeAmountInPence / 100;
    }

    private static double getAmountRemitted(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        if (remissionDecision.isPresent() && remissionDecision.get().equals(RemissionDecision.REJECTED)) {
            return 0;
        } else {
            double feeAmountInPence = Double.parseDouble(asylumCase.read(AMOUNT_REMITTED, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Amount remitted not found")));
            return feeAmountInPence / 100;
        }
    }

    public static double getFeeRemission(AsylumCase asylumCase) {
        RemissionType remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Remission type not found"));

        if (remissionType.equals(RemissionType.NO_REMISSION)) {
            return 0;
        } else {
            return getAmountRemitted(asylumCase);
        }
    }

    public static boolean isValidUserDirection(
            DirectionFinder directionFinder, AsylumCase asylumCase,
            DirectionTag directionTag, Parties parties
    ) {
        return directionFinder
                .findFirst(asylumCase, directionTag)
                .map(direction -> direction.getParties().equals(parties))
                .orElse(false);
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

    public static boolean isDirectionPartyRespondent(AsylumCase asylumCase) {
        return asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)
                .map(parties -> parties.equals(Parties.RESPONDENT))
                .orElse(false);
    }

    public static Map<String, String> getDirectionDueDateAndExplanation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        String directionEditDueDate = asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)
                .orElseThrow(() -> new IllegalStateException("Direction edit date due is not present"));

        String directionEditExplanation = asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)
                .orElseThrow(() -> new IllegalStateException("Direction edit explanation is not present"));

        return ImmutableMap
                .<String, String>builder()
                .put("dueDate", formatDateForNotificationAttachmentDocument(LocalDate.parse(directionEditDueDate)))
                .put("directionExplaination", directionEditExplanation)
                .build();
    }

    public static boolean isDecisionWithoutHearingAppeal(AsylumCase asylumCase) {
        return asylumCase.read(IS_DECISION_WITHOUT_HEARING, YesOrNo.class)
            .map(yesOrNo -> YesOrNo.YES == yesOrNo).orElse(false);
    }

    public static boolean isRemoteHearing(AsylumCase asylumCase) {
        return asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);
    }

    public static boolean isVirtualHearing(AsylumCase asylumCase) {
        return asylumCase.read(IS_VIRTUAL_HEARING, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES)
            || asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .map(HearingCentre.IAC_NATIONAL_VIRTUAL::equals).orElse(false);
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

    public static List<DocumentWithMetadata> getMaybeLetterNotificationDocuments(AsylumCase asylumCase, DocumentTag documentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeLetterNotificationDocuments = asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS);

        return maybeLetterNotificationDocuments
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(document -> document.getTag() == documentTag)
            .collect(Collectors.toList());
    }

    public static boolean isAppellantInUk(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)
            .map(inUk -> YesOrNo.YES == inUk).orElse(true);
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

    public static String convertAsylumCaseFeeValue(String amountFromAsylumCase) {
        return StringUtils.isNotBlank(amountFromAsylumCase)
                ? new BigDecimal(String.valueOf(Double.parseDouble(amountFromAsylumCase) / 100))
                .setScale(2, RoundingMode.DOWN).toString()
                : "";
    }

    public static boolean isDetainedInOneOfFacilityTypes(AsylumCase asylumCase, DetentionFacility... facilityTypes) {
        for (DetentionFacility facilityType : facilityTypes) {
            if (isDetainedInFacilityType(asylumCase, facilityType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBeenSubmittedAsLegalRepresentedInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
                .map(yesOrNo -> Objects.equals(NO, yesOrNo)).orElse(false);
    }

    public static boolean isDetainedInFacilityType(AsylumCase asylumCase, DetentionFacility facilityType) {
        if (!isAppellantInDetention(asylumCase)) {
            return false;
        }
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class).orElse("none");

        return detentionFacility.equals(facilityType.getValue());
    }

    public static boolean isSubmissionOutOfTime(AsylumCase asylumCase) {
        return asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class).orElse(NO).equals(YES);
    }

    public static Boolean isFeeExemptAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AsylumAppealType.class)
            .map(type -> type == RP || type == DC)
            .orElse(false);
    }

}
