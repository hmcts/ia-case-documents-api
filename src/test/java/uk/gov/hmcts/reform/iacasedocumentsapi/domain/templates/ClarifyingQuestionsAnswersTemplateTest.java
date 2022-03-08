package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CLARIFYING_QUESTIONS_ANSWERS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;

@ExtendWith(MockitoExtension.class)
class ClarifyingQuestionsAnswersTemplateTest {

    private final String templateName = "CLARIFYING_QUESTIONS_ANSWERS_TEMPLATE.docx";
    @Mock
    DirectionFinder directionFinder;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CustomerServicesProvider customerServicesProvider;
    private ClarifyingQuestionsAnswersTemplate clarifyingQuestionsAnswersTemplate;

    @BeforeEach
    public void setUp() {

        clarifyingQuestionsAnswersTemplate = new ClarifyingQuestionsAnswersTemplate(
            templateName, directionFinder, customerServicesProvider
        );
    }

    @Test
    void should_return_template_name() {
        assertEquals(templateName, clarifyingQuestionsAnswersTemplate.getName());
    }

    @Test
    void should_map_case_data_to_template_field_values() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(directionFinder.findFirst(Mockito.any(), eq(DirectionTag.REQUEST_CLARIFYING_QUESTIONS))).thenReturn(Optional.of(getDirection()));
        when(asylumCase.read(CLARIFYING_QUESTIONS_ANSWERS)).thenReturn(Optional.of(getClarifyingQuestionsAnswers()));
        String appealReferenceNumber = "RP/11111/2020";
        when(asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealReferenceNumber));
        String appellantGivenNames = "Talha";
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class)).thenReturn(Optional.of(appellantGivenNames));
        String appellantFamilyName = "Awan";
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class)).thenReturn(Optional.of(appellantFamilyName));
        String homeOfficeReferenceNumber = "A1234567/001";
        when(asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(homeOfficeReferenceNumber));

        String customerServicesTelephone = "555 555 555";
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn(customerServicesTelephone);
        String customerServicesEmail = "customer.services@example.com";
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn(customerServicesEmail);


        Map<String, Object> templateFieldValues = clarifyingQuestionsAnswersTemplate.mapFieldValues(caseDetails);
        assertEquals(9, templateFieldValues.size());

        assertEquals(appealReferenceNumber, templateFieldValues.get("appealReferenceNumber"));
        assertEquals(appellantGivenNames, templateFieldValues.get("appellantGivenNames"));
        assertEquals(appellantFamilyName, templateFieldValues.get("appellantFamilyName"));
        assertEquals(homeOfficeReferenceNumber, templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(customerServicesTelephone, templateFieldValues.get("customerServicesTelephone"));
        assertEquals(customerServicesEmail, templateFieldValues.get("customerServicesEmail"));
        String expectedDate = "22122021";
        assertEquals(expectedDate, templateFieldValues.get("dateSent"));
        assertEquals(expectedDate, templateFieldValues.get("dateAnswered"));

        assertEquals(1, ((List<?>) templateFieldValues.get("questionsAnswers")).size());
        ImmutableMap<Object, Object> expected = ImmutableMap
            .builder()
            .put("questionIndex", "1")
            .put("question", "Question")
            .put("answer", "Answer")
            .put("supportingEvidence", "some-filename")
            .build();
        assertEquals(expected, ((List<?>) templateFieldValues.get("questionsAnswers")).get(0));

    }

    @Test
    void should_throw_missing_direction_for_clarifying_questions() {
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        assertThatThrownBy(() -> clarifyingQuestionsAnswersTemplate.mapFieldValues(caseDetails))
            .hasMessage("direction 'requestClarifyingQuestions' is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_be_tolerant_of_missing_data() {

        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(Mockito.any(), eq(String.class))).thenReturn(Optional.empty())
            .thenReturn(Optional.empty())
            .thenReturn(Optional.empty())
            .thenReturn(Optional.empty());
        when(directionFinder.findFirst(Mockito.any(), eq(DirectionTag.REQUEST_CLARIFYING_QUESTIONS))).thenReturn(Optional.of(getDirection()));
        when((customerServicesProvider.getCustomerServicesTelephone())).thenReturn("");
        when((customerServicesProvider.getCustomerServicesEmail())).thenReturn("");

        Map<String, Object> templateFieldValues = clarifyingQuestionsAnswersTemplate.mapFieldValues(caseDetails);

        assertEquals(7, templateFieldValues.size());
        assertEquals("", templateFieldValues.get("appealReferenceNumber"));
        assertEquals("", templateFieldValues.get("appellantGivenNames"));
        assertEquals("", templateFieldValues.get("appellantFamilyName"));
        assertEquals("", templateFieldValues.get("homeOfficeReferenceNumber"));
        assertEquals(0, ((List<?>) templateFieldValues.get("questionsAnswers")).size());
    }

    private Document createDocument() {
        return
            new Document("some-url",
                "some-binary-url",
                "some-filename");
    }

    private Direction getDirection() {
        return new Direction("some explanation",
            Parties.APPELLANT,
            "2021-12-22",
            "2021-12-22",
            DirectionTag.REQUEST_CLARIFYING_QUESTIONS,
            Collections.emptyList(),
            Collections.singletonList(new IdValue<>("id", new ClarifyingQuestion("Question"))),
            "some-id",
            "some-direction-type");
    }

    private List<IdValue<ClarifyingQuestionAnswer>> getClarifyingQuestionsAnswers() {

        List<IdValue<Document>> supportingEvidence = Collections.singletonList(
            new IdValue<>("docid", createDocument())
        );
        return Collections.singletonList(
            new IdValue<>("id", new ClarifyingQuestionAnswer("2021-12-22", "2021-12-22",
                "2021-12-22", "Question", "Answer",
                "directionId", supportingEvidence))
        );
    }
}
