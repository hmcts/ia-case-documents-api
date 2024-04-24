package uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.JourneyType.AIP;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.AccessCodeGenerator;

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

}
