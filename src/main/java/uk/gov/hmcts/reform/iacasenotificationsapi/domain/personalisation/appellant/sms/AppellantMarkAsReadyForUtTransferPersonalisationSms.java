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
public class AppellantMarkAsReadyForUtTransferPersonalisationSms implements SmsNotificationPersonalisation {

    private final String markReadyForUtTransferAppellantTemplateId;
    private final RecipientsFinder recipientsFinder;

    public AppellantMarkAsReadyForUtTransferPersonalisationSms(
        @Value("${govnotify.template.markAsReadyForUtTransfer.appellant.sms}") String markReadyForUtTransferAppellantTemplateId,
        RecipientsFinder recipientsFinder
    ) {
        this.markReadyForUtTransferAppellantTemplateId = markReadyForUtTransferAppellantTemplateId;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return markReadyForUtTransferAppellantTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_MARK_AS_READY_FOR_UT_TRANSFER_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("utAppealReferenceNumber", asylumCase.read(AsylumCaseDefinition.UT_APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .build();
    }

}
