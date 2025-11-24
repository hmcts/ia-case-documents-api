package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.TimeExtensionFinder;

@Service
public class AppellantReviewTimeExtensionRefusedPersonalisationSms implements SmsNotificationPersonalisation {

    private final String reviewTimeExtensionRefusedAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;
    private final TimeExtensionFinder timeExtensionFinder;

    public AppellantReviewTimeExtensionRefusedPersonalisationSms(
        @Value("${govnotify.template.reviewTimeExtensionRefused.appellant.sms}") String reviewTimeExtensionRefusedAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        TimeExtensionFinder timeExtensionFinder
    ) {
        this.reviewTimeExtensionRefusedAppellantSmsTemplateId = reviewTimeExtensionRefusedAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
        this.timeExtensionFinder = timeExtensionFinder;
    }


    @Override
    public String getTemplateId() {
        return reviewTimeExtensionRefusedAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REVIEW_TIME_EXTENSION_REFUSED_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        State currentState = callback.getCaseDetails().getState();
        requireNonNull(currentState, "currentState must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        requireNonNull(asylumCase, "asylumCase must not be null");
        final IdValue<TimeExtension> timeExtensionIdValue = timeExtensionFinder.findCurrentTimeExtension(currentState, TimeExtensionStatus.REFUSED, asylumCase);
        final String nextActionText = timeExtensionFinder.findNextActionText(currentState);

        final String dueDate =
            LocalDate
                .parse(timeExtensionIdValue.getValue().getDecisionOutcomeDate())
                .format(DateTimeFormatter.ofPattern("d MMM yyyy"));
        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("decision reason", timeExtensionIdValue.getValue().getDecisionReason())
                .put("Next action text", nextActionText)
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
