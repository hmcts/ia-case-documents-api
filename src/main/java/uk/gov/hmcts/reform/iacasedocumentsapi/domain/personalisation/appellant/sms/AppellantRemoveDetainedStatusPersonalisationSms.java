package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.*;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;

@Service
public class AppellantRemoveDetainedStatusPersonalisationSms implements SmsNotificationPersonalisation {

    private final String removeDetainedStatusSmsTemplateId;

    public AppellantRemoveDetainedStatusPersonalisationSms(
            @NotNull(message = "removeDetentionStatusSmsTemplateId cannot be null")
            @Value("${govnotify.template.removeDetentionStatus.appellant.sms}") String removeDetentionStatusSmsTemplateId
    ) {
        this.removeDetainedStatusSmsTemplateId = removeDetentionStatusSmsTemplateId;
    }


    @Override
    public String getTemplateId() {
        return removeDetainedStatusSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        Set<String> recipients = new HashSet<>();

        Optional<List<String>> contactPreference = asylumCase.read(CONTACT_PREFERENCE_UN_REP);
        if (!contactPreference.isPresent() || !contactPreference.get().contains(ContactPreference.WANTS_SMS.getValue())) {
            return recipients;
        }

        String mobileNumber = asylumCase.read(MOBILE_NUMBER, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Mobile number not found"));

        recipients.add(mobileNumber);
        return recipients;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REMOVE_DETENTION_STATUS_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return
                ImmutableMap
                        .<String, String>builder()
                        .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                        .build();
    }
}
