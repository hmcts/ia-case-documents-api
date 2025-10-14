package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

@Service
public class AppellantSubmittedWithRemissionRequestPersonalisationSms implements SmsNotificationPersonalisation {
    private final String submittedRemissionRequestSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterAppealSubmitted;


    public AppellantSubmittedWithRemissionRequestPersonalisationSms(
        @Value("${govnotify.template.appealSubmitted.appellant.remission.sms}") String submittedRemissionRequestSmsTemplateId,
        @Value("${appellantDaysToWait.afterHearingRequirementsSubmitted}") int daysAfterAppealSubmitted,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder,
        SystemDateProvider systemDateProvider
    ) {
        this.submittedRemissionRequestSmsTemplateId = submittedRemissionRequestSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.systemDateProvider = systemDateProvider;
        this.daysAfterAppealSubmitted = daysAfterAppealSubmitted;
    }


    @Override
    public String getTemplateId() {
        return submittedRemissionRequestSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_SUBMITTED_WITH_REMISSION_REQUEST_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        final String dueDate = systemDateProvider.dueDate(daysAfterAppealSubmitted);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("appealSubmittedDaysAfter", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }

}
