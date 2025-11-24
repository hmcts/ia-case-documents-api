package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.REASON_FOR_LINK_APPEAL;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ReasonForLinkAppealOptions;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;


@Service
public class AppellantLinkAppealPersonalisationSms implements SmsNotificationPersonalisation {

    private final String linkAppealAppellantSmsTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantLinkAppealPersonalisationSms(
            @Value("${govnotify.template.linkAppealBeforeListing.appellant.sms}") String linkAppealAppellantSmsTemplateId,
            RecipientsFinder recipientsFinder
    ) {
        this.linkAppealAppellantSmsTemplateId = linkAppealAppellantSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
    }


    @Override
    public String getTemplateId() {
        return linkAppealAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LINK_APPEAL_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .put("reason", asylumCase.read(REASON_FOR_LINK_APPEAL, ReasonForLinkAppealOptions.class)
                                .map(ReasonForLinkAppealOptions::getId)
                                .orElse(""))
                        .build();
    }
}
