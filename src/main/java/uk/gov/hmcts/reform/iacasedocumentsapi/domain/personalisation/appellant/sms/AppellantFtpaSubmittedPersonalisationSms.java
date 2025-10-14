package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantFtpaSubmittedPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantFtpaSubmittedSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantFtpaSubmittedPersonalisationSms(
        @Value("${govnotify.template.applyForFtpa.aip.sms}") String appellantFtpaSubmittedSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantFtpaSubmittedSmsTemplateId = appellantFtpaSubmittedSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return appellantFtpaSubmittedSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_IN_PERSON_FTPA_SUBMITTED_SMS";
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class)
                .orElse(""))
            .put("linkToService", iaAipFrontendUrl)
            .build();
    }
}
