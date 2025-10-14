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
public class AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantRecordOutOfTimeDecisionCanProceedSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantRecordOutOfTimeDecisionCanProceedPersonalisationSms(
            @Value("${govnotify.template.recordOutOfTimeDecision.appellant.canProceed.sms}") String appellantRecordOutOfTimeDecisionCanProceedSmsTemplateId,
            @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder
    ) {
        this.appellantRecordOutOfTimeDecisionCanProceedSmsTemplateId = appellantRecordOutOfTimeDecisionCanProceedSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return appellantRecordOutOfTimeDecisionCanProceedSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RECORD_OUT_OF_TIME_DECISION_CAN_PROCEED_AIP_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .build();
    }

}
