package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantSubmitReasonsForAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String reasonsForAppealSubmittedAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final int daysToWaitAfterReasonsForAppeal;
    private final RecipientsFinder recipientsFinder;
    private final SystemDateProvider systemDateProvider;

    public AppellantSubmitReasonsForAppealPersonalisationSms(
        @Value("${govnotify.template.submitReasonsForAppeal.appellant.sms}") String reasonsForAppealSubmittedAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterReasonsForAppeal}") int daysToWaitAfterReasonsForAppeal,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.reasonsForAppealSubmittedAppellantSmsTemplateId = reasonsForAppealSubmittedAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterReasonsForAppeal = daysToWaitAfterReasonsForAppeal;
        this.recipientsFinder = recipientsFinder;
        this.systemDateProvider = systemDateProvider;
    }


    @Override
    public String getTemplateId() {
        return reasonsForAppealSubmittedAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMIT_REASONS_FOR_APPEAL_APPELLANT_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysToWaitAfterReasonsForAppeal);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
