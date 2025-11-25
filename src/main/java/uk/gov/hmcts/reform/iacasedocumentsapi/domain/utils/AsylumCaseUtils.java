package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType.APPELLANT;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.DC;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.RP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.EA;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.EU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.HU;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.JourneyType.REP;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplicantType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ApplyForCosts;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.MakeAnApplication;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.OtherDetentionFacilityName;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.NationalityGovUk;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.*;


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

    public static boolean isDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)
                .orElse(NO)
                .equals(YES);
    }

    public static boolean isInternalCase(AsylumCase asylumCase) {
        return asylumCase.read(IS_ADMIN, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static boolean hasAppealBeenSubmittedByAppellantInternalCase(AsylumCase asylumCase) {
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

    public static boolean isRepJourney(AsylumCase asylumCase) {
        return asylumCase
                .read(JOURNEY_TYPE, JourneyType.class)
                .map(type -> type == REP).orElse(true);
    }

    public static boolean isNotInternalOrIsInternalWithLegalRepresentation(AsylumCase asylumCase) {
        return (!isInternalCase(asylumCase) ||
            isInternalCase(asylumCase) && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase));
    }

    public static boolean inCountryAppeal(AsylumCase asylumCase) {
        return asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(false);
    }

    public static boolean legalRepInCountryAppeal(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class)
            .map(value -> value.equals(YesOrNo.YES))
            .orElse(false);
    }

    public static boolean isAriaMigrated(AsylumCase asylumCase) {
        return asylumCase.read(IS_ARIA_MIGRATED, YesOrNo.class).map(isAdmin -> YES == isAdmin).orElse(false);
    }

    public static boolean isRemissionApproved(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        return remissionDecision.isPresent() && remissionDecision.get().equals(RemissionDecision.APPROVED);
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
            .orElse(emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(d -> d.getTag() == documentTag)
            .findFirst().orElseThrow(() -> new IllegalStateException(documentTag + " document not available"));
    }

    public static DocumentWithMetadata getBundledLetter(AsylumCase asylumCase, DocumentTag documentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> optionalNotificationLetters = asylumCase.read(LETTER_BUNDLE_DOCUMENTS);
        return optionalNotificationLetters
            .orElse(emptyList())
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
            .read(JOURNEY_TYPE, uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.JourneyType.class)
            .map(type -> type == AIP).orElse(false);
    }

    public static Map<String, String> getAppellantPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("hmcts", "[userImage:hmcts.png]")
                .putAll(getAppellantPersonalisationWithoutUserImage(asylumCase))
                .build();
    }

    public static Map<String, String> getLegalRepPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("hmcts", "[userImage:hmcts.png]")
            .putAll(getLegalRepPersonalisationWithoutUserImage(asylumCase))
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

    public static Map<String, String> getLegalRepPersonalisationWithoutUserImage(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appellantGivenNames", asylumCase.read(LEGAL_REP_GIVEN_NAME, String.class).orElse(""))
                .put("appellantFamilyName", asylumCase.read(LEGAL_REP_FAMILY_NAME_PAPER_J, String.class).orElse(""))
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

    public static boolean isRemissionApproved(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        return remissionDecision.isPresent() && remissionDecision.get().equals(RemissionDecision.APPROVED);
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

    public static List<IdValue<DocumentWithMetadata>> getAddendumEvidenceDocuments(AsylumCase asylumCase) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeExistingAdditionalEvidenceDocuments =
                asylumCase.read(ADDENDUM_EVIDENCE_DOCUMENTS);
        if (maybeExistingAdditionalEvidenceDocuments.isEmpty()) {
            return Collections.emptyList();
        }

        return maybeExistingAdditionalEvidenceDocuments.get();
    }

    public static Optional<Document> getDecisionOfNoticeDocuments(AsylumCase asylumCase) {
        return asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT);
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

    public static List<DocumentWithMetadata> getMaybeLetterNotificationDocuments(AsylumCase asylumCase, DocumentTag documentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeLetterNotificationDocuments = asylumCase.read(LETTER_NOTIFICATION_DOCUMENTS);

        return maybeLetterNotificationDocuments
            .orElse(Collections.emptyList())
            .stream()
            .map(IdValue::getValue)
            .filter(document -> document.getTag() == documentTag)
            .collect(Collectors.toList());
    }

    public static List<DocumentWithMetadata> getMaybeNotificationAttachmentDocuments(AsylumCase asylumCase, DocumentTag documentTag) {
        Optional<List<IdValue<DocumentWithMetadata>>> maybeLetterNotificationDocuments = asylumCase.read(NOTIFICATION_ATTACHMENT_DOCUMENTS);

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

    public static Boolean remissionDecisionPartiallyGrantedOrRefused(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .map(decision -> PARTIALLY_APPROVED == decision || REJECTED == decision)
            .orElse(false);
    }

    public static Boolean remissionDecisionPartiallyGranted(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                .map(decision -> PARTIALLY_APPROVED == decision)
                .orElse(false);
    }

    public static Boolean remissionDecisionGranted(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
                .map(decision -> APPROVED == decision)
                .orElse(false);
    }

    public static List<String> getAppellantAddressInCountryOrOoc(final AsylumCase asylumCase) {
        return isAppellantInUk(asylumCase) ? getAppellantAddressAsList(asylumCase) :
                getAppellantAddressAsListOoc(asylumCase);
    }

    public static List<String> getLegalRepAddressInCountryOrOoc(final AsylumCase asylumCase) {
        return legalRepInCountryAppeal(asylumCase) ? getLegalRepresentativeAddressAsList(asylumCase) :
                getLegalRepresentativeAddressOocAsList(asylumCase);
    }

    public static boolean legalRepInCountryAppeal(AsylumCase asylumCase) {
        return asylumCase.read(LEGAL_REP_HAS_ADDRESS, YesOrNo.class).map(value -> value.equals(YesOrNo.YES)).orElse(false);
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
        boolean appellantHasFixedUkAddress = asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)
            .map(flag -> flag.equals(YES))
            .orElse(false);

        boolean appellantHasFixedOutOfCountryAddress = asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class)
            .map(flag -> flag.equals(YES))
            .orElse(false);

        return appellantHasFixedUkAddress || appellantHasFixedOutOfCountryAddress || isDetainedInFacilityType(asylumCase, OTHER);
    }

    public static Set<String> getAppellantAddressInCountryOrOoc(final AsylumCase asylumCase) {
        return inCountryAppeal(asylumCase) ? singleton(getAppellantAddressAsList(asylumCase)
            .stream()
            .map(item -> item.replaceAll("\\s", ""))
            .collect(joining("_"))) :
                singleton(getAppellantAddressAsListOoc(asylumCase).stream()
                        .map(item -> item.replaceAll("\\s", "")).collect(joining("_")));
    }

    public static Set<String> getLegalRepAddressInCountryOrOoc(final AsylumCase asylumCase) {
        if (legalRepInCountryAppeal(asylumCase)) {
            return singleton(getLegalRepresentativeAddressAsList(asylumCase)
                .stream()
                .map(item -> item.replaceAll("\\s", ""))
                .collect(joining("_")));
        }
        return singleton(getLegalRepresentativeAddressOocAsList(asylumCase)
            .stream()
            .map(item -> item.replaceAll("\\s", ""))
            .collect(joining("_")));
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
        Boolean isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(YES, yesOrNo))
            .orElse(false);

        Boolean isLegallyRepresented = asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
            .map(yesOrNo -> Objects.equals(NO, yesOrNo))
            .orElse(false);

        return isAdmin && isLegallyRepresented;
    }

    public static String getHearingChannel(AsylumCase asylumCase, String defaultValue) {
        Optional<DynamicList> hearingChannelDl = asylumCase.read(HEARING_CHANNEL, DynamicList.class);

        return hearingChannelDl
            .map(dynamicList -> dynamicList.getValue().getLabel())
            .orElse(defaultValue);
    }

    public static boolean isFtpaDecisionOutcomeTypeUnderRule31OrRule32(AsylumCase asylumCase) {
        List<String> rule31And32 = List.of(
            FtpaDecisionOutcomeType.FTPA_REMADE31.toString(),
            FtpaDecisionOutcomeType.FTPA_REMADE32.toString()
        );

        final ApplicantType ftpaApplicantType = retrieveApplicantType(asylumCase);

        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = retrieveFtpaDecisionApplicantType(asylumCase, ftpaApplicantType);

        return ftpaDecisionOutcomeType
            .map(decision -> rule31And32.contains(decision.toString()))
            .orElse(false);
    }

    private static ApplicantType retrieveApplicantType(AsylumCase asylumCase) {
        return asylumCase
            .read(FTPA_APPLICANT_TYPE, ApplicantType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaApplicantType is not present"));
    }

    private static Optional<FtpaDecisionOutcomeType> retrieveFtpaDecisionApplicantType(AsylumCase asylumCase, ApplicantType ftpaApplicantType) {
        return asylumCase.read(ftpaApplicantType.equals(APPELLANT)
            ? FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE
            : FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);
    }

    public static MakeAnApplication getDecidedApplication(AsylumCase asylumCase) {
        String decidedApplicationId = asylumCase.read(DECIDE_AN_APPLICATION_ID, String.class)
            .orElseThrow(() -> new IllegalStateException("decideAnApplicationId is not present"));

        Optional<List<IdValue<MakeAnApplication>>> applications = asylumCase.read(MAKE_AN_APPLICATIONS);

        IdValue<MakeAnApplication> applicationIdValue = applications
            .orElse(Collections.emptyList())
            .stream()
            .filter(app -> app.getId().equals(decidedApplicationId))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("the decided application is not present in make an applications list"));;

        return applicationIdValue.getValue();
    }

    public static String addIndefiniteArticle(String noun) {
        if (StringUtils.isBlank(noun)) {
            return "";
        }

        String trimmedNoun = noun.trim();
        if (trimmedNoun.isEmpty()) {
            return "";
        }

        char firstChar = Character.toLowerCase(trimmedNoun.charAt(0));

        boolean startsWithVowelSound = firstChar == 'a' || firstChar == 'e' || firstChar == 'i' || firstChar == 'o' || firstChar == 'u';

        // Handle special cases where 'u' sounds like 'you' (consonant sound)
        if (firstChar == 'u' && trimmedNoun.length() > 1) {
            String lowerNoun = trimmedNoun.toLowerCase();
            if (lowerNoun.startsWith("uni") || lowerNoun.startsWith("us") || lowerNoun.startsWith("ut")) {
                startsWithVowelSound = false; // "university", "user", "utility" use "A"
            }
        }

        // Handle 'h' words where 'h' is silent (vowel sound)
        if (firstChar == 'h' && trimmedNoun.length() > 1) {
            String lowerNoun = trimmedNoun.toLowerCase();
            if (lowerNoun.startsWith("hon") || lowerNoun.startsWith("hou")) {
                startsWithVowelSound = true; // "honor", "hour" use "An"
            }
        }

        return (startsWithVowelSound ? "An " : "A ") + trimmedNoun;
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

    public static boolean isDetainedInOneOfFacilityTypes(AsylumCase asylumCase, DetentionFacility... facilityTypes) {
        for (DetentionFacility facilityType : facilityTypes) {
            if (isDetainedInFacilityType(asylumCase, facilityType)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isDetainedInFacilityType(AsylumCase asylumCase, DetentionFacility facilityType) {
        if (!isAppellantInDetention(asylumCase)) {
            return false;
        }
        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class).orElse("none");

        return detentionFacility.equals(facilityType.getValue());
    }

    public static Boolean isFeeExemptAppeal(AsylumCase asylumCase) {
        return asylumCase
            .read(APPEAL_TYPE, AppealType.class)
            .map(type -> type == AppealType.RP || type == AppealType.DC).orElse(false);
    }

    public static boolean isHearingChannel(AsylumCase asylumCase, String hearingChannelCode) {
        return asylumCase.read(HEARING_CHANNEL, DynamicList.class)
            .map(hearingChannels -> hearingChannels.getValue().getCode().equals(hearingChannelCode))
            .orElse(false);
    }

    public static Boolean remissionDecisionPartiallyGrantedOrRefused(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .map(decision -> PARTIALLY_APPROVED == decision || REJECTED == decision)
            .orElse(false);
    }

    public static Boolean remissionDecisionPartiallyGranted(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .map(decision -> PARTIALLY_APPROVED == decision)
            .orElse(false);
    }

    public static Boolean remissionDecisionGranted(AsylumCase asylumCase) {
        return asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .map(decision -> APPROVED == decision)
            .orElse(false);
    }

    public static boolean isInternalNonDetainedCase(AsylumCase asylumCase) {
        return isInternalCase(asylumCase) && !isAppellantInDetention(asylumCase);
    }

    public static boolean internalNonDetainedWithAddressAvailable(AsylumCase asylumCase) {
        return hasAppellantAddressInCountryOrOutOfCountry(asylumCase) && isInternalNonDetainedCase(asylumCase);
    }

    public static boolean isHearingDetailsUpdated(AsylumCase asylumCase,
                                                  Optional<CaseDetails<AsylumCase>> caseDetailsBefore) {
        boolean result = false;
        if (caseDetailsBefore.isPresent()) {
            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();
            result = !asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .equals(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
                || !asylumCase.read(LIST_CASE_HEARING_DATE, String.class)
                .equals(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class))
                || !asylumCase.read(HEARING_CHANNEL, DynamicList.class)
                .equals(asylumCaseBefore.read(HEARING_CHANNEL, DynamicList.class));
        }
        return result;
    }
}


