package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantRespondentFtpaSubmittedPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantRespondentFtpaSubmittedSmsTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantRespondentFtpaSubmittedPersonalisationSms(
        @Value("${govnotify.template.applyForFtpa.respondent.toAppellant.sms}") String appellantRespondentFtpaSubmittedSmsTemplateId,
        RecipientsFinder recipientsFinder
    ) {

        this.appellantRespondentFtpaSubmittedSmsTemplateId = appellantRespondentFtpaSubmittedSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return appellantRespondentFtpaSubmittedSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_FTPA_SUBMITTED_TO_APPELLANT_SMS";
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
            .build();
    }
}
