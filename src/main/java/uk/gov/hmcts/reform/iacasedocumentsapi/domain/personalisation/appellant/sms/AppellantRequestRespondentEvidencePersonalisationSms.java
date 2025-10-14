package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DirectionFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantRequestRespondentEvidencePersonalisationSms implements SmsNotificationPersonalisation {

    private final String appealSubmittedOutOfTimeAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final DirectionFinder directionFinder;
    private final RecipientsFinder recipientsFinder;

    public AppellantRequestRespondentEvidencePersonalisationSms(
        @Value("${govnotify.template.requestRespondentEvidenceDirection.appellant.sms}") String appealSubmittedOutOfTimeAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        DirectionFinder directionFinder,
        RecipientsFinder recipientsFinder

    ) {
        this.appealSubmittedOutOfTimeAppellantSmsTemplateId = appealSubmittedOutOfTimeAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.directionFinder = directionFinder;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return appealSubmittedOutOfTimeAppellantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REQUEST_RESPONDENT_EVIDENCE_DIRECTION_APPELLANT_AIP_SMS";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }


    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Direction direction =
            directionFinder
                .findFirst(asylumCase, DirectionTag.RESPONDENT_EVIDENCE)
                .orElseThrow(() -> new IllegalStateException("direction '" + DirectionTag.RESPONDENT_EVIDENCE + "' is not present"));

        final String directionDueDate =
            LocalDate
                .parse(direction.getDateDue())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("direction due date", directionDueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
