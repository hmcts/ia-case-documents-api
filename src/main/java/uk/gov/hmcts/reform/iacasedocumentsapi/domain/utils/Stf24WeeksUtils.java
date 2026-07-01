package uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static java.time.LocalDate.parse;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.STF_24W_CURRENT_STATUS_AUTO_GENERATED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.TRIBUNAL_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.Event.COMPLETE_CASE_REVIEW;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAppellantInUk;

@Slf4j
public class Stf24WeeksUtils {
    public static final String STF_24_WEEKS_REVIEW_DOCUMENT_CREATOR = "stf24WeeksReviewDocumentCreator";
    public static final String D_MMM_YYYY = "d MMM yyyy";
    public static final String APPELLANT_FULL_NAME = "appellantFullName";
    public static final String PRACTICE_DIRECTION = "practiceDirection";
    public static final String EMPTY_STRING = "";
    public static final String WEEKS_DEADLINE = "24WeeksDeadline";
    public static final int DAYS_14 = 14;
    public static final int DAYS_42 = 42;
    public static final int DAYS_56 = 56;
    public static final String DAYS_14_FROM_DATE_OF_DIRECTION_KEY = "14DaysFromDateOfDirection";
    public static final String DAYS_56_FROM_DATE_OF_DIRECTION = "56DaysFromDateOfDirection";
    public static final String DAYS_42_FROM_DATE_OF_DIRECTION_KEY = "42DaysFromDateOfDirection";
    public static final String DECISION_SENT_DATE = "decisionSentDate";
    public static final String APPEAL_RECEIVED_DATE = "appealReceivedDate";

    private Stf24WeeksUtils() {

    }

    public static boolean isCaseReviewFor24WeeksCase(Event event, AsylumCase asylumCase) {
        boolean inCountryAppeal = isAppellantInUk(asylumCase);
        boolean hasStf24W = hasStf24WeeksStatus(asylumCase);
        return event == COMPLETE_CASE_REVIEW
                && inCountryAppeal
                && hasStf24W;
    }

    public static boolean hasStf24WeeksStatus(AsylumCase asylumCase) {
        Optional<YesOrNo> read = asylumCase.read(STF_24W_CURRENT_STATUS_AUTO_GENERATED, YesOrNo.class);
        return read.map(value -> value.equals(YES)).orElse(false);
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

}
