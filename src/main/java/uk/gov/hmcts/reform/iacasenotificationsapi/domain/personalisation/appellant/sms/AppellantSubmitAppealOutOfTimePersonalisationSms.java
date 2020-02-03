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
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AppellantSubmitAppealOutOfTimePersonalisationSms implements SmsNotificationPersonalisation {

    private final String appealSubmittedOutOfTimeAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final RecipientsFinder recipientsFinder;

    public AppellantSubmitAppealOutOfTimePersonalisationSms(
        @Value("${govnotify.template.appealSubmittedOutOfTimeAppellant.sms}") String appealSubmittedOutOfTimeAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        SystemDateProvider systemDateProvider,
        RecipientsFinder recipientsFinder

    ) {
        this.appealSubmittedOutOfTimeAppellantSmsTemplateId = appealSubmittedOutOfTimeAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.systemDateProvider = systemDateProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return appealSubmittedOutOfTimeAppellantSmsTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_SUBMITTED_OUT_OF_TIME_APPELLANT_AIP_SMS";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }


    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(14);

        return
            ImmutableMap
                .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("due date", dueDate)
                .put("Hyperlink to service", iaAipFrontendUrl)
                .build();
    }
}
