package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.MANCHESTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@ExtendWith(MockitoExtension.class)
public class DecisionAndReasonsTemplateTest {

    @Mock private StringProvider stringProvider;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private String templateName = "decision-and-reasons-template.docx";
    private DecisionAndReasonsTemplate decisionAndReasonsTemplate;
    private String currentYear;

    @BeforeEach
    public void setUp() {

        currentYear = String.valueOf(LocalDate.now().getYear());

        decisionAndReasonsTemplate =
                new DecisionAndReasonsTemplate(
                        templateName,
                        stringProvider
                );
    }

    @Test
    public void should_return_template_name() {

        assertEquals(templateName, decisionAndReasonsTemplate.getName());
    }

    @Test
    public void should_map_case_data_to_template_field_values() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some-appeal-ref"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("some-given-name"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("some-family-name"));
        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(asylumCase.read(APPELLANT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-appellant-rep"));
        when(asylumCase.read(RESPONDENT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-respondent-rep"));

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-introduction"));
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-summary"));

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-agreed-immigration-history"));
        when(asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-respondents-immigration-history"));
        when(asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("some-immigration-disagreement"));

        when(asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of("some-agreed-schedule"));
        when(asylumCase.read(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of("some-disputed-schedule"));
        when(asylumCase.read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("some-schedule-disagreement"));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        Map<String, Object> templateFieldValues = decisionAndReasonsTemplate.mapFieldValues(caseDetails);

        assertEquals(templateFieldValues.size(), 21);

        assertEquals(templateFieldValues.get("decisionsandreasons"), "[userImage:decisionsandreasons.png]");
        assertEquals(templateFieldValues.get("appealReferenceNumber"), "some-appeal-ref");
        assertEquals(templateFieldValues.get("hearingDate"), "25122020");
        assertEquals(templateFieldValues.get("hearingTime"), "1234");
        assertEquals(templateFieldValues.get("hearingCentre"), "Manchester");

        assertEquals(templateFieldValues.get("appellantGivenNames"), "some-given-name");
        assertEquals(templateFieldValues.get("appellantFamilyName"), "some-family-name");

        assertEquals(templateFieldValues.get("anonymityOrder"), "No");
        assertEquals(templateFieldValues.get("appellantRepresentative"), "some-appellant-rep");
        assertEquals(templateFieldValues.get("respondentRepresentative"), "some-respondent-rep");

        assertEquals(templateFieldValues.get("caseIntroductionDescription"), "some-case-introduction");
        assertEquals(templateFieldValues.get("appellantCaseSummaryDescription"), "some-case-summary");

        assertEquals(templateFieldValues.get("immigrationHistoryAgreement"), "No");
        assertEquals(templateFieldValues.get("agreedImmigrationHistory"), "some-agreed-immigration-history");
        assertEquals(templateFieldValues.get("respondentsImmigrationHistoryDescription"), "some-respondents-immigration-history");
        assertEquals(templateFieldValues.get("immigrationHistoryDisagreementDescription"), "some-immigration-disagreement");

        assertEquals(templateFieldValues.get("scheduleIssueAgreement"), "No");
        assertEquals(templateFieldValues.get("appellantsScheduleOfIssuesDescription"), "some-agreed-schedule");
        assertEquals(templateFieldValues.get("appellantsDisputedScheduleOfIssuesDescription"), "some-disputed-schedule");
        assertEquals(templateFieldValues.get("scheduleOfIssuesDisagreementDescription"), "some-schedule-disagreement");

        assertEquals(templateFieldValues.get("currentYear"), currentYear);
    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some-appeal-ref"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("some-given-name"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("some-family-name"));
        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(asylumCase.read(APPELLANT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-appellant-rep"));
        when(asylumCase.read(RESPONDENT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-respondent-rep"));

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-introduction"));
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-summary"));

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-agreed-immigration-history"));
        when(asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-respondents-immigration-history"));
        when(asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("some-immigration-disagreement"));

        when(asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of("some-agreed-schedule"));
        when(asylumCase.read(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.of("some-disputed-schedule"));
        when(asylumCase.read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("some-schedule-disagreement"));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateFieldValues = decisionAndReasonsTemplate.mapFieldValues(caseDetails);

        assertEquals(templateFieldValues.size(), 21);

        assertEquals(templateFieldValues.get("decisionsandreasons"), "[userImage:decisionsandreasons.png]");
        assertEquals(templateFieldValues.get("appealReferenceNumber"), "some-appeal-ref");
        assertEquals(templateFieldValues.get("hearingDate"), "25122020");
        assertEquals(templateFieldValues.get("hearingTime"), "1234");
        assertEquals(templateFieldValues.get("hearingCentre"), "Manchester");

        assertEquals(templateFieldValues.get("appellantGivenNames"), "some-given-name");
        assertEquals(templateFieldValues.get("appellantFamilyName"), "some-family-name");

        assertEquals(templateFieldValues.get("anonymityOrder"), "No");
        assertEquals(templateFieldValues.get("appellantRepresentative"), "some-appellant-rep");
        assertEquals(templateFieldValues.get("respondentRepresentative"), "some-respondent-rep");

        assertEquals(templateFieldValues.get("caseIntroductionDescription"), "");
        assertEquals(templateFieldValues.get("appellantCaseSummaryDescription"), "");

        assertEquals(templateFieldValues.get("immigrationHistoryAgreement"), "No");
        assertEquals(templateFieldValues.get("agreedImmigrationHistory"), "");
        assertEquals(templateFieldValues.get("respondentsImmigrationHistoryDescription"), "");
        assertEquals(templateFieldValues.get("immigrationHistoryDisagreementDescription"), "");

        assertEquals(templateFieldValues.get("scheduleIssueAgreement"), "No");
        assertEquals(templateFieldValues.get("appellantsScheduleOfIssuesDescription"), "");
        assertEquals(templateFieldValues.get("appellantsDisputedScheduleOfIssuesDescription"), "");
        assertEquals(templateFieldValues.get("scheduleOfIssuesDisagreementDescription"), "");

        assertEquals(templateFieldValues.get("currentYear"), currentYear);
    }


    @Test
    public void should_be_tolerant_of_missing_hearing_date() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some-appeal-ref"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(""));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("some-given-name"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("some-family-name"));
        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(asylumCase.read(APPELLANT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-appellant-rep"));
        when(asylumCase.read(RESPONDENT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-respondent-rep"));

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-introduction"));
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-summary"));

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));
        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateFieldValues = decisionAndReasonsTemplate.mapFieldValues(caseDetails);

        assertEquals(templateFieldValues.size(), 21);


        assertEquals(templateFieldValues.get("hearingDate"), "");
        assertEquals(templateFieldValues.get("hearingTime"), "");

    }

    @Test
    public void handling_should_throw_if_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("listCaseHearingCentre is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_anonymity_order_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some-appeal-ref"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("some-given-name"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("some-family-name"));
        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("anonymity order must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_immigration_history_agreement_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some-appeal-ref"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("some-given-name"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("some-family-name"));
        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(asylumCase.read(APPELLANT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-appellant-rep"));
        when(asylumCase.read(RESPONDENT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-respondent-rep"));

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-introduction"));
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-summary"));

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("immigrationHistoryAgreement must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_schedule_of_issues_agreement_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(MANCHESTER));
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of("some-appeal-ref"));

        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of("2020-12-25T12:34:56"));
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of("some-given-name"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of("some-family-name"));
        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(asylumCase.read(APPELLANT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-appellant-rep"));
        when(asylumCase.read(RESPONDENT_REPRESENTATIVE, String.class)).thenReturn(Optional.of("some-respondent-rep"));

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-introduction"));
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-case-summary"));

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));
        when(asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-agreed-immigration-history"));
        when(asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.of("some-respondents-immigration-history"));
        when(asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.of("some-immigration-disagreement"));

        when(asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)).thenReturn(Optional.of(NO));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(stringProvider.get("hearingCentreName", "manchester")).thenReturn(Optional.of("Manchester"));

        when(asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("scheduleOfIssuesAgreement must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }
}
