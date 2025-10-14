package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.*;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantRequestClarifyingQuestionsPersonalisationSms implements SmsNotificationPersonalisation {

    private final String requestClarifyingQuestionsSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final DirectionFinder directionFinder;


    public AppellantRequestClarifyingQuestionsPersonalisationSms(
        @Value("${govnotify.template.requestClarifyingQuestions.appellant.sms}") String requestClarifyingQuestionsSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        DirectionFinder directionFinder
    ) {
        this.requestClarifyingQuestionsSmsTemplateId = requestClarifyingQuestionsSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.directionFinder = directionFinder;

    }


    @Override
    public String getTemplateId() {
        return requestClarifyingQuestionsSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_CLARIFYING_QUESTIONS_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.REQUEST_CLARIFYING_QUESTIONS)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.REQUEST_CLARIFYING_QUESTIONS + "' is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("Hyperlink to service", iaAipFrontendUrl)
                .put("direction due date", directionDueDate)
                .build();
    }
}
