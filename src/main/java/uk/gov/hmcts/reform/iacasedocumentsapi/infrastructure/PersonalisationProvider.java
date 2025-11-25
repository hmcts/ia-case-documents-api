package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getApplicationById;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.retrieveLatestApplyForCosts;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.DateUtils.formatDateForNotification;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;

@Service
public class PersonalisationProvider {

    private static final String HEARING_CENTRE_ADDRESS_CONST = "hearingCentreAddress";
    private static final String APPEAL_REFERENCE_NUMBER_CONST = "appealReferenceNumber";
    private static final String ARIA_LISTING_REFERENCE_CONST = "ariaListingReference";
    private static final String APPELLANT_GIVEN_NAMES_CONST = "appellantGivenNames";
    private static final String APPELLANT_FAMILY_NAME_CONST = "appellantFamilyName";
    private static final String ASYLUM_NOT_NULL_MESSAGE = "asylumCase must not be null";
    private static final String HOME_OFFICE_REFERENCE_NUMBER_CONST = "homeOfficeReferenceNumber";
    private static final String LINK_TO_ONLINE_SERVICE = "linkToOnlineService";
    private final String iaExUiFrontendUrl;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final DirectionFinder directionFinder;
    private final DateTimeExtractor dateTimeExtractor;
    private final String recipientReferenceNumber = "recipientReferenceNumber";
    private final String recipient = "recipient";

    ImmutableMap.Builder<String, AsylumCaseDefinition> personalisationBuilder = new ImmutableMap.Builder<String, AsylumCaseDefinition>()
        .put(APPEAL_REFERENCE_NUMBER_CONST, APPEAL_REFERENCE_NUMBER)
        .put("legalRepReferenceNumber", LEGAL_REP_REFERENCE_NUMBER)
        .put(ARIA_LISTING_REFERENCE_CONST, ARIA_LISTING_REFERENCE)
        .put(HOME_OFFICE_REFERENCE_NUMBER_CONST, HOME_OFFICE_REFERENCE_NUMBER)
        .put(APPELLANT_GIVEN_NAMES_CONST, APPELLANT_GIVEN_NAMES)
        .put(APPELLANT_FAMILY_NAME_CONST, APPELLANT_FAMILY_NAME);

    Map<Event, Map<String, AsylumCaseDefinition>> eventDefinition = new ImmutableMap.Builder<Event, Map<String, AsylumCaseDefinition>>()
        .put(CHANGE_DIRECTION_DUE_DATE, personalisationBuilder
            .build())
        .put(DRAFT_HEARING_REQUIREMENTS, personalisationBuilder
            .build())
        .put(EDIT_CASE_LISTING, personalisationBuilder
            .build())
        .put(UPLOAD_ADDITIONAL_EVIDENCE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE, personalisationBuilder
            .build())
        .put(UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER, personalisationBuilder
            .build())
        .put(SEND_DIRECTION, personalisationBuilder
            .build())
        .put(APPLY_FOR_FTPA_APPELLANT, personalisationBuilder
            .build())
        .put(APPLY_FOR_FTPA_RESPONDENT, personalisationBuilder
            .build())
        .build();

    public PersonalisationProvider(
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        HearingDetailsFinder hearingDetailsFinder,
        DirectionFinder directionFinder,
        DateTimeExtractor dateTimeExtractor) {
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.directionFinder = directionFinder;
        this.dateTimeExtractor = dateTimeExtractor;
    }

    public Map<String, String> getEditCaseListingPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<CaseDetails<AsylumCase>> caseDetailsBefore = callback.getCaseDetailsBefore();

        final String hearingDateTime =
            hearingDetailsFinder.getHearingDateTime(asylumCase);

        String hearingCentreNameBefore = "";
        String oldHearingDate = "";


        if (caseDetailsBefore.isPresent()) {

            AsylumCase asylumCaseBefore = caseDetailsBefore.get().getCaseData();

            hearingCentreNameBefore =
                hearingDetailsFinder.getOldHearingCentreName(asylumCaseBefore);

            oldHearingDate =
                asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class).orElse("");
        }

        final Builder<String, String> caseListingValues = ImmutableMap
            .<String, String>builder()
            .put(LINK_TO_ONLINE_SERVICE, iaExUiFrontendUrl)
            .put("oldHearingCentre", hearingCentreNameBefore)
            .put("oldHearingDate", oldHearingDate.isEmpty() ? oldHearingDate : dateTimeExtractor.extractHearingDate(oldHearingDate))
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDateTime))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDateTime))
            .put("hearingCentreName", hearingDetailsFinder.getHearingCentreName(asylumCase))
            .put(HEARING_CENTRE_ADDRESS_CONST, hearingDetailsFinder.getHearingCentreLocation(asylumCase));

        buildHearingRequirementsFields(asylumCase, caseListingValues);

        return caseListingValues.build();
    }

    public static void buildHearingRequirementsFields(AsylumCase asylumCase, Builder<String, String> caseListingValues) {

        final Optional<YesOrNo> isSubmitRequirementsAvailable = asylumCase.read(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE);

        if (isSubmitRequirementsAvailable.isPresent() && isSubmitRequirementsAvailable.get() == YesOrNo.YES) {

            caseListingValues
                .put("hearingRequirementVulnerabilities", generateAdjustmentOutput(asylumCase,
                    VULNERABILITIES_DECISION_FOR_DISPLAY,
                    VULNERABILITIES_TRIBUNAL_RESPONSE,
                    "No special adjustments are being made to accommodate vulnerabilities"))
                .put("hearingRequirementMultimedia", generateAdjustmentOutput(asylumCase,
                    MULTIMEDIA_DECISION_FOR_DISPLAY,
                    MULTIMEDIA_TRIBUNAL_RESPONSE,
                    "No multimedia equipment is being provided"))
                .put("hearingRequirementSingleSexCourt", generateAdjustmentOutput(asylumCase,
                    SINGLE_SEX_COURT_DECISION_FOR_DISPLAY,
                    SINGLE_SEX_COURT_TRIBUNAL_RESPONSE,
                    "The court will not be single sex"))
                .put("hearingRequirementInCameraCourt", generateAdjustmentOutput(asylumCase,
                    IN_CAMERA_COURT_DECISION_FOR_DISPLAY,
                    IN_CAMERA_COURT_TRIBUNAL_RESPONSE,
                    "The hearing will be held in public court"))
                .put("hearingRequirementOther", generateAdjustmentOutput(asylumCase,
                    OTHER_DECISION_FOR_DISPLAY,
                    ADDITIONAL_TRIBUNAL_RESPONSE,
                    "No other adjustments are being made"))
                .put("remoteVideoCallTribunalResponse", readStringCaseField(asylumCase, REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE,
                    ""));

        } else {

            caseListingValues
                    .put("hearingRequirementVulnerabilities", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_VULNERABILITIES,
                        "No special adjustments are being made to accommodate vulnerabilities"))
                    .put("hearingRequirementMultimedia", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_MULTIMEDIA,
                        "No multimedia equipment is being provided"))
                    .put("hearingRequirementSingleSexCourt", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_SINGLE_SEX_COURT,
                        "The court will not be single sex"))
                    .put("hearingRequirementInCameraCourt", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_IN_CAMERA_COURT,
                        "The hearing will be held in public court"))
                    .put("hearingRequirementOther", readStringCaseField(asylumCase, LIST_CASE_REQUIREMENTS_OTHER,
                        "No other adjustments are being made"))
                    .put("remoteVideoCallTribunalResponse", readStringCaseField(asylumCase, REMOTE_VIDEO_CALL_TRIBUNAL_RESPONSE,
                        ""));

        }
    }

    /**
     * If the Display fields are present then that means the adjustments must be already granted/refused and responded by LO.
     * If no Display fields are present then we try to fetch the Response fields as there could be older cases
     * which haven't had the Granted/Refused fields in place.
     * If no Response fields are present then no adjustments are required.
     */
    private static String generateAdjustmentOutput(AsylumCase asylumCase, AsylumCaseDefinition displayField,
                                            AsylumCaseDefinition responseField, String noAdjustmentRequiredText) {

        String defaultOutput = readStringCaseField(asylumCase, responseField, noAdjustmentRequiredText);

        if (asylumCase.read(displayField, String.class).isPresent()) {
            return "Request " + readStringCaseField(asylumCase, displayField, defaultOutput);
        } else {
            return defaultOutput;
        }
    }

    public Map<String, String> getNonStandardDirectionPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final Direction direction = directionFinder
            .findFirst(asylumCase, DirectionTag.NONE)
            .orElseThrow(() -> new IllegalStateException("non-standard direction is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("iaCaseListHyperLink", iaExUiFrontendUrl)
            .put("explanation", direction.getExplanation())
            .put("dueDate", directionDueDate)
            .build();

    }

    public Map<String, String> getChangeDirectionDueDatePersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        String directionEditDueDate = asylumCase.read(DIRECTION_EDIT_DATE_DUE, String.class)
            .orElseThrow(() -> new IllegalStateException("Direction edit date due is not present"));

        String directionEditExplanation = asylumCase.read(DIRECTION_EDIT_EXPLANATION, String.class)
            .orElseThrow(() -> new IllegalStateException("Direction edit explanation is not present"));

        final String directionDueDate =
            LocalDate
                .parse(directionEditDueDate)
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return ImmutableMap
            .<String, String>builder()
            .put("iaCaseListHyperLink", iaExUiFrontendUrl)
            .put("explanation", directionEditExplanation)
            .put("dueDate", directionDueDate)
            .build();
    }

    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Map<String, String> immutableMap = eventDefinition
            .get(callback.getEvent())
            .entrySet()
            .stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> asylumCase.read(e.getValue(), String.class).orElse("N/A")));

        if (callback.getEvent() == Event.SEND_DIRECTION) {
            immutableMap.putAll(getNonStandardDirectionPersonalisation(callback));
        } else if (callback.getEvent() == Event.CHANGE_DIRECTION_DUE_DATE) {
            immutableMap.putAll(getChangeDirectionDueDatePersonalisation(callback));
        } else if (callback.getEvent() == Event.EDIT_CASE_LISTING) {
            immutableMap.putAll(getEditCaseListingPersonalisation(callback));
        }

        return immutableMap;
    }

    public Map<String, String> getReviewedHearingRequirementsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .build();
    }

    private static String readStringCaseField(final AsylumCase asylumCase, final AsylumCaseDefinition caseField, final String defaultIfNotPresent) {

        final Optional<String> optionalFieldValue = asylumCase.read(caseField, String.class);
        return optionalFieldValue.isPresent() && !optionalFieldValue.get().isEmpty() ? optionalFieldValue.get() : defaultIfNotPresent;
    }

    public Map<String, String> getHomeOfficeHeaderPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, ASYLUM_NOT_NULL_MESSAGE);

        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put(ARIA_LISTING_REFERENCE_CONST, asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put(HOME_OFFICE_REFERENCE_NUMBER_CONST, asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .build();
    }

    public Map<String, String> getRespondentHeaderPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, ASYLUM_NOT_NULL_MESSAGE);

        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put(ARIA_LISTING_REFERENCE_CONST, asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("respondentReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .build();
    }

    public Map<String, String> getLegalRepHeaderPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, ASYLUM_NOT_NULL_MESSAGE);

        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put(ARIA_LISTING_REFERENCE_CONST, asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .build();
    }

    public Map<String, String> getTribunalHeaderPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, ASYLUM_NOT_NULL_MESSAGE);

        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put(ARIA_LISTING_REFERENCE_CONST, asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .put(LINK_TO_ONLINE_SERVICE, iaExUiFrontendUrl)
            .build();
    }

    public Map<String, String> getAppellantPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, ASYLUM_NOT_NULL_MESSAGE);

        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put(HOME_OFFICE_REFERENCE_NUMBER_CONST, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .build();
    }

    public Map<String, String> getAppellantCredentials(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put(APPELLANT_GIVEN_NAMES_CONST, asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put(APPELLANT_FAMILY_NAME_CONST, asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .build();
    }

    public Map<String, String> getApplyForCostsPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put(APPEAL_REFERENCE_NUMBER_CONST, asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(getAppellantCredentials(asylumCase))
            .put(LINK_TO_ONLINE_SERVICE, iaExUiFrontendUrl)
            .build();
    }

    public Map<String, String> getTypeForLatestCreatedApplyForCosts(AsylumCase asylumCase) {
        return ImmutableMap
            .<String, String>builder()
            .put("appliedCostsType", retrieveLatestApplyForCosts(asylumCase).getAppliedCostsType().replaceAll("costs", "").trim()).build();
    }

    public Map<String, String> getTypeForSelectedApplyForCosts(AsylumCase asylumCase, AsylumCaseDefinition definition) {
        return ImmutableMap
            .<String, String>builder()
            .put("appliedCostsType", getApplicationById(asylumCase, definition).getAppliedCostsType().replaceAll("costs", "").trim()).build();
    }

    public Map<String, String> getHomeOfficeRecipientHeader(AsylumCase asylumCase) {
        final String homeOffice = "Home Office";

        return ImmutableMap
            .<String, String>builder()
            .put(recipient, homeOffice)
            .put(recipientReferenceNumber, asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }

    public Map<String, String> getLegalRepRecipientHeader(AsylumCase asylumCase) {
        final String yourPrefix = "Your";

        return ImmutableMap
            .<String, String>builder()
            .put(recipient, yourPrefix)
            .put(recipientReferenceNumber, asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .build();
    }

    public Map<String, String> retrieveSelectedApplicationId(AsylumCase asylumCase, AsylumCaseDefinition definition) {
        DynamicList selectedApplication = asylumCase.read(definition, DynamicList.class)
            .orElseThrow(() -> new IllegalStateException(definition + " is not present"));

        String applicationNumber = selectedApplication.getValue().getLabel().split(",")[0].replaceAll("[^0-9]", "");

        return ImmutableMap.<String, String>builder()
            .put("applicationId", applicationNumber)
            .build();
    }

    public Map<String, String> getApplyToCostsCreationDate(AsylumCase asylumCase) {
        String latestApplyForCostsCreationDate = retrieveLatestApplyForCosts(asylumCase).getApplyForCostsCreationDate();

        return ImmutableMap
            .<String, String>builder()
            .put("creationDate", formatDateForNotification(LocalDate.parse(latestApplyForCostsCreationDate)))
            .build();
    }

    public Map<String, String> getDecideCostsPersonalisation(AsylumCase asylumCase) {
        String costsDecision = getApplicationById(asylumCase, DECIDE_COSTS_APPLICATION_LIST).getApplyForCostsDecision();

        return ImmutableMap
            .<String, String>builder()
            .put("costsDecisionType", costsDecision)
            .build();

    }

}
