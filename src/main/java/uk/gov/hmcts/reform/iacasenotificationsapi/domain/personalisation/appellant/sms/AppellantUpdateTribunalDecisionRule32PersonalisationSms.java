package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;

import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

@Service
public class AppellantUpdateTribunalDecisionRule32PersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantUpdateTribunalDecisionRule32SmsTemplateId;
    private final CustomerServicesProvider customerServicesProvider;

    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AppellantUpdateTribunalDecisionRule32PersonalisationSms(
        @Value("${govnotify.template.updateTribunalDecision.rule32.appellant.sms}") String appellantUpdateTribunalDecisionRule32SmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        CustomerServicesProvider customerServicesProvider, RecipientsFinder recipientsFinder) {
        this.appellantUpdateTribunalDecisionRule32SmsTemplateId = appellantUpdateTribunalDecisionRule32SmsTemplateId;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
    }

    @Override
    public String getTemplateId() {
        return appellantUpdateTribunalDecisionRule32SmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_UPDATE_TRIBUNAL_DECISION_RULE_32_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return
            ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkAipTimeline", iaAipFrontendUrl)
                .build();
    }
}
