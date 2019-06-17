package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre.MANCHESTER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo.NO;

import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.StringProvider;

@RunWith(MockitoJUnitRunner.class)
public class DecisionAndReasonsTemplateTest {

    @Mock private StringProvider stringProvider;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    private String templateName = "decision-and-reasons-template.docx";
    private DecisionAndReasonsTemplate decisionAndReasonsTemplate;

    @Before
    public void setUp() {

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

        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of("Manchester, 123 Somewhere, North"));

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

        Map<String, Object> templateFieldValues = decisionAndReasonsTemplate.mapFieldValues(caseDetails);

        assertThat(templateFieldValues.size()).isEqualTo(20);

        assertThat(templateFieldValues.get("decisionsandreasons")).isEqualTo("[userImage:decisionsandreasons.png]");
        assertThat(templateFieldValues.get("appealReferenceNumber")).isEqualTo("some-appeal-ref");
        assertThat(templateFieldValues.get("hearingDate")).isEqualTo("25122020");
        assertThat(templateFieldValues.get("hearingTime")).isEqualTo("1234");
        assertThat(templateFieldValues.get("hearingCentre")).isEqualTo("Manchester\n123 Somewhere\nNorth");

        assertThat(templateFieldValues.get("appellantGivenNames")).isEqualTo("some-given-name");
        assertThat(templateFieldValues.get("appellantFamilyName")).isEqualTo("some-family-name");

        assertThat(templateFieldValues.get("anonymityOrder")).isEqualTo("No");
        assertThat(templateFieldValues.get("appellantRepresentative")).isEqualTo("some-appellant-rep");
        assertThat(templateFieldValues.get("respondentRepresentative")).isEqualTo("some-respondent-rep");

        assertThat(templateFieldValues.get("caseIntroductionDescription")).isEqualTo("some-case-introduction");
        assertThat(templateFieldValues.get("appellantCaseSummaryDescription")).isEqualTo("some-case-summary");

        assertThat(templateFieldValues.get("immigrationHistoryAgreement")).isEqualTo("No");
        assertThat(templateFieldValues.get("agreedImmigrationHistory")).isEqualTo("some-agreed-immigration-history");
        assertThat(templateFieldValues.get("respondentsImmigrationHistoryDescription")).isEqualTo("some-respondents-immigration-history");
        assertThat(templateFieldValues.get("immigrationHistoryDisagreementDescription")).isEqualTo("some-immigration-disagreement");

        assertThat(templateFieldValues.get("scheduleIssueAgreement")).isEqualTo("No");
        assertThat(templateFieldValues.get("appellantsScheduleOfIssuesDescription")).isEqualTo("some-agreed-schedule");
        assertThat(templateFieldValues.get("appellantsDisputedScheduleOfIssuesDescription")).isEqualTo("some-disputed-schedule");
        assertThat(templateFieldValues.get("scheduleOfIssuesDisagreementDescription")).isEqualTo("some-schedule-disagreement");

    }

    @Test
    public void should_be_tolerant_of_missing_data() {

        when(asylumCase.read(CASE_INTRODUCTION_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_CASE_SUMMARY_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(AGREED_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(RESPONDENTS_IMMIGRATION_HISTORY_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(IMMIGRATION_HISTORY_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(asylumCase.read(APPELLANTS_AGREED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANTS_DISPUTED_SCHEDULE_OF_ISSUES_DESCRIPTION, String.class)).thenReturn(Optional.empty());
        when(asylumCase.read(SCHEDULE_OF_ISSUES_DISAGREEMENT_DESCRIPTION, String.class)).thenReturn(Optional.empty());

        when(stringProvider.get("hearingCentreAddress", "manchester")).thenReturn(Optional.of("Manchester, 123 Somewhere, North"));

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Map<String, Object> templateFieldValues = decisionAndReasonsTemplate.mapFieldValues(caseDetails);

        assertThat(templateFieldValues.size()).isEqualTo(20);

        assertThat(templateFieldValues.get("decisionsandreasons")).isEqualTo("[userImage:decisionsandreasons.png]");
        assertThat(templateFieldValues.get("appealReferenceNumber")).isEqualTo("some-appeal-ref");
        assertThat(templateFieldValues.get("hearingDate")).isEqualTo("25122020");
        assertThat(templateFieldValues.get("hearingTime")).isEqualTo("1234");
        assertThat(templateFieldValues.get("hearingCentre")).isEqualTo("Manchester\n123 Somewhere\nNorth");

        assertThat(templateFieldValues.get("appellantGivenNames")).isEqualTo("some-given-name");
        assertThat(templateFieldValues.get("appellantFamilyName")).isEqualTo("some-family-name");

        assertThat(templateFieldValues.get("anonymityOrder")).isEqualTo("No");
        assertThat(templateFieldValues.get("appellantRepresentative")).isEqualTo("some-appellant-rep");
        assertThat(templateFieldValues.get("respondentRepresentative")).isEqualTo("some-respondent-rep");

        assertThat(templateFieldValues.get("caseIntroductionDescription")).isEqualTo("");
        assertThat(templateFieldValues.get("appellantCaseSummaryDescription")).isEqualTo("");

        assertThat(templateFieldValues.get("immigrationHistoryAgreement")).isEqualTo("No");
        assertThat(templateFieldValues.get("agreedImmigrationHistory")).isEqualTo("");
        assertThat(templateFieldValues.get("respondentsImmigrationHistoryDescription")).isEqualTo("");
        assertThat(templateFieldValues.get("immigrationHistoryDisagreementDescription")).isEqualTo("");

        assertThat(templateFieldValues.get("scheduleIssueAgreement")).isEqualTo("No");
        assertThat(templateFieldValues.get("appellantsScheduleOfIssuesDescription")).isEqualTo("");
        assertThat(templateFieldValues.get("appellantsDisputedScheduleOfIssuesDescription")).isEqualTo("");
        assertThat(templateFieldValues.get("scheduleOfIssuesDisagreementDescription")).isEqualTo("");

    }

    @Test
    public void handling_should_throw_if_hearing_centre_not_present() {

        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("listCaseHearingCentre is not present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_anonymity_order_not_present() {

        when(asylumCase.read(ANONYMITY_ORDER, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("anonymity order must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_immigration_history_agreement_not_present() {

        when(asylumCase.read(IMMIGRATION_HISTORY_AGREEMENT, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("immigrationHistoryAgreement must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void handling_should_throw_if_schedule_of_issues_agreement_not_present() {

        when(asylumCase.read(SCHEDULE_OF_ISSUES_AGREEMENT, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decisionAndReasonsTemplate.mapFieldValues(caseDetails))
                .hasMessage("scheduleOfIssuesAgreement must be present")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

}