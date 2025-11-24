package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;



@Service
public class AppellantAppealExitedOnlinePersonalisationSms implements SmsNotificationPersonalisation {

    private final String appealExitedOnlineAppellantSmsTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantAppealExitedOnlinePersonalisationSms(
            @Value("${govnotify.template.removeAppealFromOnlineBeforeListing.appellant.sms}") String appealExitedOnlineAppellantSmsTemplateId,
            RecipientsFinder recipientsFinder
    ) {
        this.appealExitedOnlineAppellantSmsTemplateId = appealExitedOnlineAppellantSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return appealExitedOnlineAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_EXITED_ONLINE_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("reason", asylumCase.read(AsylumCaseDefinition.REMOVE_APPEAL_FROM_ONLINE_REASON, String.class)
                                .orElse(""))
                        .build();
    }
}
