package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumAppealType.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotificationAttachmentDocument;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
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

}
