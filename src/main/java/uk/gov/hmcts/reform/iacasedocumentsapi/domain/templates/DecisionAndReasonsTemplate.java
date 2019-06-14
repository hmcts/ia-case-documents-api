package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@Component
public class DecisionAndReasonsTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter DOCUMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private static final DateTimeFormatter DOCUMENT_TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

    private final String templateName;
    private final StringProvider stringProvider;

    public DecisionAndReasonsTemplate(
        @Value("${decisionAndReasons.templateName}") String templateName,
        StringProvider stringProvider
    ) {
        this.templateName = templateName;
        this.stringProvider = stringProvider;
    }

    public String getName() {
        return templateName;
    }

    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();

        final HearingCentre listedHearingCentre =
            asylumCase
                .read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
                .orElseThrow(() -> new IllegalStateException("listCaseHearingCentre is not present"));

        final Map<String, Object> fieldValues = new HashMap<>();

        fieldValues.put("decisionsandreasons", "[userImage:decisionsandreasons.png]");

        fieldValues.put(
                "hearingCentre",
                stringProvider.get("hearingCentreAddress", listedHearingCentre.toString()).orElse("")
                        .replaceAll(",\\s*", "\n")
        );

        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("hearingDate", formatDateForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));
        fieldValues.put("hearingTime", formatTimeForRendering(asylumCase.read(LIST_CASE_HEARING_DATE, String.class).orElse("")));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("anonymityOrder", asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("anonymity order must be present")).toString());

        fieldValues.put("appellantRepresentative", asylumCase.read(APPELLANT_REPRESENTATIVE, String.class).orElse(""));
        fieldValues.put("respondentRepresentative", asylumCase.read(RESPONDENT_REPRESENTATIVE, String.class).orElse(""));

        fieldValues.put("caseIntroductionDescription", asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("appellantCaseSummaryDescription", asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class).orElse(""));

        fieldValues.put("immigrationHistoryAgreement", asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("immigrationHistoryAgreement must be present")).toString());
        fieldValues.put("agreedImmigrationHistory", asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("respondentsImmigrationHistoryDescription", asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("immigrationHistoryDisagreementDescription", asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class).orElse(""));

        fieldValues.put("scheduleOfIssuesAgreement", asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("scheduleOfIssuesAgreement must be present")).toString());
        fieldValues.put("appellantsScheduleOfIssuesDescription", asylumCase.read(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("appellantsDisputedScheduleOfIssuesDescription", asylumCase.read(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class).orElse(""));
        fieldValues.put("scheduleOfIssuesDisagreementDescription", asylumCase.read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, String.class).orElse(""));

        return fieldValues;
    }

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_DATE_FORMAT);
        }

        return "";
    }

    private String formatTimeForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDateTime.parse(date).format(DOCUMENT_TIME_FORMAT);
        }

        return "";
    }
}
