package uk.gov.hmcts.reform.iacasedocumentsapi.domain.templates;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.CLARIFYING_QUESTIONS_ANSWERS;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ClarifyingQuestion;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ClarifyingQuestionAnswer;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DirectionFinder;

@Component
public class ClarifyingQuestionsAnswersTemplate implements DocumentTemplate<AsylumCase> {

    private static final DateTimeFormatter QUESTION_SENT_OR_ANSWERED_DATE_FORMAT = DateTimeFormatter.ofPattern("ddMMyyyy");
    private final CustomerServicesProvider customerServicesProvider;
    private final DirectionFinder directionFinder;
    private final String templateName;

    public ClarifyingQuestionsAnswersTemplate(
        @Value("${clarifyingQuestionsAnswers.templateName}") String templateName,
        DirectionFinder directionFinder,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.templateName = templateName;
        this.customerServicesProvider = customerServicesProvider;
        this.directionFinder = directionFinder;
    }

    public String getName() {
        return templateName;
    }

    @Override
    public Map<String, Object> mapFieldValues(
        CaseDetails<AsylumCase> caseDetails
    ) {
        final AsylumCase asylumCase = caseDetails.getCaseData();
        final Map<String, Object> fieldValues = new HashMap<>();

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.REQUEST_CLARIFYING_QUESTIONS)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.REQUEST_CLARIFYING_QUESTIONS + "' is not present"));

        fieldValues.put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""));
        fieldValues.put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""));
        fieldValues.put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""));
        fieldValues.put("customerServicesTelephone", customerServicesProvider.getCustomerServicesTelephone());
        fieldValues.put("customerServicesEmail", customerServicesProvider.getCustomerServicesEmail());

        List<ClarifyingQuestionAnswer> clarifyingQuestionsAnswers = fetchClarifyingQuestionsAnswers(direction, asylumCase);
        AtomicInteger questionIndex = new AtomicInteger();
        List<ImmutableMap<Object, Object>> questionsAnswersList =
            clarifyingQuestionsAnswers.stream()
                .map(x -> buildQuestionAndAnswer(x, String.valueOf(questionIndex.incrementAndGet())))
                .collect(Collectors.toList());
        fieldValues.put(
            "questionsAnswers",
            questionsAnswersList
        );

        if (!clarifyingQuestionsAnswers.isEmpty()) {
            ClarifyingQuestionAnswer firstAnswer = clarifyingQuestionsAnswers.get(0);
            fieldValues.put("dateSent", formatDateForRendering(firstAnswer.getDateSent()));
            fieldValues.put("dateAnswered", formatDateForRendering(firstAnswer.getDateResponded()));
        }

        return fieldValues;
    }

    //Build the question and answer
    private ImmutableMap<Object, Object> buildQuestionAndAnswer(ClarifyingQuestionAnswer clarifyingQuestionAnswer, String questionIndex) {
        return ImmutableMap
            .builder()
            .put("questionIndex", questionIndex)
            .put("question", clarifyingQuestionAnswer.getQuestion())
            .put("answer", clarifyingQuestionAnswer.getAnswer())
            .put("supportingEvidence", getSupportingEvidence(clarifyingQuestionAnswer.getSupportingEvidence()))
            .build();
    }

    //Get the list of all supporting evidence file names
    private String getSupportingEvidence(List<IdValue<Document>> supportingEvidence) {
        final List<String> evidenceFileNames = supportingEvidence.stream()
            .map(IdValue::getValue)
            .map(Document::getDocumentFilename)
            .collect(Collectors.toList());
        return evidenceFileNames.isEmpty() ? "No supporting evidence provided" : StringUtils.join(evidenceFileNames, "\n");
    }


    //Fetch the questions and answers that are asked very recently
    private List<ClarifyingQuestionAnswer> fetchClarifyingQuestionsAnswers(Direction direction, AsylumCase asylumCase) {

        String dateSent = direction.getDateSent();
        String dateDue = direction.getDateDue();
        List<String> questions = direction.getClarifyingQuestions().stream().map(IdValue::getValue).map(ClarifyingQuestion::getQuestion).collect(Collectors.toList());
        questions.add("Do you want to tell us anything else about your case?");

        Optional<List<IdValue<ClarifyingQuestionAnswer>>> clarifyingQuestionsAnswers = asylumCase.read(CLARIFYING_QUESTIONS_ANSWERS);
        return clarifyingQuestionsAnswers.orElse(emptyList())
            .stream()
            .limit(questions.size())
            .map(IdValue::getValue)
            .filter(x -> dateSent.equals(x.getDateSent()) && dateDue.equals(x.getDueDate()) && questions.contains(x.getQuestion()))
            .collect(Collectors.toList());
    }

    private String formatDateForRendering(
        String date
    ) {
        if (!Strings.isNullOrEmpty(date)) {
            return LocalDate.parse(date).format(QUESTION_SENT_OR_ANSWERED_DATE_FORMAT);
        }
        return "";
    }

}
