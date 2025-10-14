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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AppellantEndAppealAutomaticallyPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantEndAppealAutomaticallyTemplateId;
    private final int daysToAskReinstate;
    private final String iaAipFrontendUrl;
    private final RecipientsFinder recipientsFinder;

    public AppellantEndAppealAutomaticallyPersonalisationSms(
        @Value("${govnotify.template.endAppealAutomatically.appellant.sms}") String appellantEndAppealAutomaticallyTemplateId,
        @Value("${appellantDaysToAskReinstate}") int daysToAskReinstate,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
            RecipientsFinder recipientsFinder
    ) {
        this.appellantEndAppealAutomaticallyTemplateId = appellantEndAppealAutomaticallyTemplateId;
        this.daysToAskReinstate = daysToAskReinstate;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return appellantEndAppealAutomaticallyTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_ENDED_AUTOMATICALLY_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("endAppealApprover", asylumCase.read(AsylumCaseDefinition.END_APPEAL_APPROVER_TYPE, String.class).orElse(""))
                        .put("deadLine", asylumCase.read(AsylumCaseDefinition.END_APPEAL_DATE, String.class)
                        .map(date -> LocalDate.parse(date).plusDays(daysToAskReinstate).format(DateTimeFormatter.ofPattern("d MMM yyyy")))
                        .orElse(""))
                        .put("Hyperlink to service", iaAipFrontendUrl)
                        .build();
    }
}
