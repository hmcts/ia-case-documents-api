package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DocumentTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentCreator;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DocumentHandler;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.time.LocalDate.parse;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.LETTER_NOTIFICATION_DOCUMENTS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.SEND_LATE_TIMELINE_NOTICE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.formatDateTimeForRendering;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getAppellantPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getHearingChannel;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepAddressInCountryOrOoc;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.getLegalRepPersonalisation;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.hasBeenSubmittedAsLegalRepresentedInternalCase;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;

@Slf4j
public class Stf24WeeksUtils {
    public static final String STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR = "stf24WeeksReviewDocumentCreator";
    public static final String INTERNAL_CASE_LISTED_LR_24_WEEKS_LETTER = "internalCaseListedLr24WeeksLetter";
    public static final String INTERNAL_CASE_24_WEEKS_LISTED_LETTER = "internalCase24WeeksListedLetter";
    public static final String D_MMM_YYYY = "d MMM yyyy";
    public static final String APPELLANT_FULL_NAME = "appellantFullName";
    public static final String PRACTICE_DIRECTION = "practiceDirection";
    public static final String EMPTY_STRING = "";
    public static final String WEEKS_DEADLINE = "twentyFourWeeksDeadline";
    public static final int DAYS_14 = 14;
    public static final int DAYS_42 = 42;
    public static final int DAYS_56 = 56;
    public static final String DAYS_14_FROM_DATE_OF_DIRECTION_KEY = "fourteenDaysFromDateOfDirection";
    public static final String DAYS_56_FROM_DATE_OF_DIRECTION_KEY = "fiftySixDaysFromDateOfDirection";
    public static final String DAYS_42_FROM_DATE_OF_DIRECTION_KEY = "fortyTwoDaysFromDateOfDirection";
    public static final String DECISION_SENT_DATE = "decisionSentDate";
    public static final String APPEAL_RECEIVED_DATE = "appealReceivedDate";
    public static final String APPEAL_REFERENCE_NUMBER_KEY = "appealReferenceNumber";
    public static final String HOME_OFFICE_REFERENCE_NUMBER_KEY = "homeOfficeReferenceNumber";
    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("d MMMM yyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    public static final String INTERNAL_CASE_LISTED_LETTER_24_WEEKS_TEMPLATE_NAME = "${internalCaseListedLetter24Weeks.templateName}";


    private Stf24WeeksUtils() {

    }

    public static boolean isCaseReviewFor24WeeksCase(Event event, AsylumCase asylumCase) {
        boolean inCountryAppeal = isAppellantInUk(asylumCase);
        boolean hasStf24W = is24WeeksCase(asylumCase);
        return List.of(SEND_LATE_TIMELINE_NOTICE, COMPLETE_CASE_REVIEW).contains(event)
            && inCountryAppeal
            && hasStf24W;
    }

    public static boolean canHandleInternalCaseListedLrLetter(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        Objects.requireNonNull(callbackStage, "callbackStage must not be null");
        Objects.requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == LIST_CASE
            && hasBeenSubmittedAsLegalRepresentedInternalCase(asylumCase);
    }

    public static PreSubmitCallbackResponse<AsylumCase> handleInternalCaseListedLrLetter(
        Callback<AsylumCase> callback,
        DocumentCreator<AsylumCase> documentCreator,
        DocumentHandler documentHandler
    ) {
        CaseDetails<AsylumCase> caseDetails = callback.getCaseDetails();
        AsylumCase asylumCase = caseDetails.getCaseData();
        Document internalCaseListedLrLetter = documentCreator.create(caseDetails);

        documentHandler.addWithMetadataWithoutReplacingExistingDocuments(
            asylumCase,
            internalCaseListedLrLetter,
            LETTER_NOTIFICATION_DOCUMENTS,
            DocumentTag.INTERNAL_CASE_LISTED_LR_LETTER
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    public static boolean is24WeeksCase(AsylumCase asylumCase) {
        Optional<YesOrNo> read = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class);
        return read.map(value -> value.equals(YES)).orElse(false);
    }

    public static Map<String, Object> createInternalCaseListedLetterFieldValues(
        CaseDetails<AsylumCase> caseDetails,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider,
        boolean legalRepresentative,
        boolean remoteHearing
    ) {
        AsylumCase asylumCase = caseDetails.getCaseData();
        HearingCentre listedHearingCentre = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        Map<String, Object> fieldValues = new HashMap<>(legalRepresentative
            ? getLegalRepPersonalisation(asylumCase)
            : getAppellantPersonalisation(asylumCase));

        List<String> addressLines = legalRepresentative
            ? getLegalRepAddressInCountryOrOoc(asylumCase)
            : getAppellantAddressInCountryOrOoc(asylumCase);

        populateInternalCaseListedLetterFields(
            fieldValues,
            asylumCase,
            listedHearingCentre,
            customerServicesProvider,
            stringProvider,
            addressLines,
            remoteHearing
        );
        return fieldValues;
    }

    private static void populateInternalCaseListedLetterFields(
        Map<String, Object> fieldValues,
        AsylumCase asylumCase,
        HearingCentre listedHearingCentre,
        CustomerServicesProvider customerServicesProvider,
        StringProvider stringProvider,
        List<String> addressLines,
        boolean remoteHearing
    ) {
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getInternalCustomerServicesTelephone(asylumCase));
        fieldValues.put("customerServicesEmail", customerServicesProvider.getInternalCustomerServicesEmail(asylumCase));
        fieldValues.put("hearingLocation", remoteHearing
            ? "Remote"
            : stringProvider.get("hearingCentreAddress", listedHearingCentre.toString())
                .orElse("").replaceAll(",\\s*", "\n"));

        String hearingDateTime = asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("");
        fieldValues.put("hearingDate", formatDateTimeForRendering(hearingDateTime, DOCUMENT_DATE_FORMAT));
        fieldValues.put("hearingTime", formatDateTimeForRendering(hearingDateTime, DOCUMENT_TIME_FORMAT));
        fieldValues.put("dateLetterSent", formatDateForRendering(LocalDate.now().toString(), DOCUMENT_DATE_FORMAT));
        fieldValues.put("hearingChannel", getHearingChannel(asylumCase, "Unknown"));

        for (int i = 0; i < addressLines.size(); i++) {
            fieldValues.put("address_line_" + (i + 1), addressLines.get(i));
        }
    }

    public static String getHomeOfficeDecisionDate(AsylumCase asylumCase) {
        final String homeOfficeDecisionDate = getCaseDateDate(asylumCase, HOME_OFFICE_DECISION_DATE);
        return LocalDate.parse(homeOfficeDecisionDate).format(DateTimeFormatter.ofPattern(D_MMM_YYYY));
    }

    private static String getCaseDateDate(AsylumCase asylumCase, AsylumCaseDefinition asylumCaseDefinition) {
        return asylumCase
                .read(asylumCaseDefinition, String.class)
                .orElse("");
    }

    public static String getAppellantGivenName(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.APPELLANT_GIVEN_NAMES, String.class).orElse(EMPTY_STRING);
    }

    public static String getAppellantFamilyName(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.APPELLANT_FAMILY_NAME, String.class).orElse(EMPTY_STRING);
    }

    public static String getAppellantFullName(AsylumCase asylumCase) {
        return (getAppellantGivenName(asylumCase) + " " + getAppellantFamilyName(asylumCase)).trim();
    }

    public static String populateStatutoryTimeFrame24wDate(AsylumCase asylumCase) {
        String tribunalReceivedDate = getCaseDateDate(asylumCase, TRIBUNAL_RECEIVED_DATE);
        String stf24WeeksAddedToDate;
        if (isEmpty(tribunalReceivedDate)) {
            String appealSubmissionDate = getCaseDateDate(asylumCase, APPEAL_SUBMISSION_DATE);
            stf24WeeksAddedToDate = add24WeeksToDate(appealSubmissionDate);
        } else {
            stf24WeeksAddedToDate = add24WeeksToDate(tribunalReceivedDate);
        }
        return stf24WeeksAddedToDate;
    }

    private static String add24WeeksToDate(String date) {
        LocalDate appealDate = parse(date);
        LocalDate stf24WeeksDate = appealDate.plusWeeks(24);
        return stf24WeeksDate.format(DateTimeFormatter.ofPattern(D_MMM_YYYY));
    }

    public static String getAppealReceivedDate(AsylumCase asylumCase) {
        String tribunalReceivedDate = getCaseDateDate(asylumCase, TRIBUNAL_RECEIVED_DATE);
        String appealReceivedDate;
        if (isEmpty(tribunalReceivedDate)) {
            appealReceivedDate = getCaseDateDate(asylumCase, APPEAL_SUBMISSION_DATE);
        } else {
            appealReceivedDate = tribunalReceivedDate;
        }

        if (isEmpty(appealReceivedDate)) {
            throw new IllegalStateException("Received date  is not present");
        }
        return LocalDate.parse(appealReceivedDate).format(DateTimeFormatter.ofPattern(D_MMM_YYYY));
    }

    public static boolean hasCompleteCaseReviewDate(AsylumCase asylumCase) {
        return asylumCase.read(AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE, String.class).isPresent();
    }

    public static void setCompleteCaseReviewContentForRemoval(AsylumCase asylumCase, Map<String, Object> fieldValues) {
        asylumCase
            .read(AsylumCaseDefinition.COMPLETE_CASE_REVIEW_DATE, String.class)
            .map(date -> LocalDate.parse(date).format(DateTimeFormatter.ofPattern(D_MMM_YYYY)))
            .ifPresent(completeCaseReviewDate -> fieldValues.put("completeCaseReviewDependentContent",
                "\nThe Pre-hearing Review and Directions sent to you on " + completeCaseReviewDate
                    + " will no longer apply and you must now follow any new directions and "
                    + "timescales sent by the Tribunal."));
    }
}
