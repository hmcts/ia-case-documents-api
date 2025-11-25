package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;


@Service
public class AppellantUnlinkAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String endAppealAppellantSmsTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantUnlinkAppealPersonalisationSms(
            @Value("${govnotify.template.unlinkAppealBeforeListing.appellant.sms}") String endAppealAppellantSmsTemplateId,
            RecipientsFinder recipientsFinder
    ) {
        this.endAppealAppellantSmsTemplateId = endAppealAppellantSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return endAppealAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UNLINK_APPEAL_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .build();
    }
}
