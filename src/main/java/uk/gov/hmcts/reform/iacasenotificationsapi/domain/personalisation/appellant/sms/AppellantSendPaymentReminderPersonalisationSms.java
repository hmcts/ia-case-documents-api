package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.convertAsylumCaseFeeValue;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AppellantSendPaymentReminderPersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantSendPaymentReminderTemplateId;
    private final SystemDateProvider systemDateProvider;
    private final int daysAfterPaymentReminder;

    public AppellantSendPaymentReminderPersonalisationSms(
        @Value("${govnotify.template.sendPaymentReminder.appellant.sms}") String appellantSendPaymentReminderTemplateId,
        @Value("${appellantDaysToWait.afterPaymentReminder}") int daysAfterPaymentReminder,
        SystemDateProvider systemDateProvider
    ) {
        this.appellantSendPaymentReminderTemplateId = appellantSendPaymentReminderTemplateId;
        this.daysAfterPaymentReminder = daysAfterPaymentReminder;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return appellantSendPaymentReminderTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class)
            .map(email -> Collections.singleton(email))
            .orElse(Collections.emptySet());
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_INTERNAL_PAYMENT_REMINDER_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String dueDate = systemDateProvider.dueDate(daysAfterPaymentReminder);

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("onlineCaseReferenceNumber", asylumCase.read(AsylumCaseDefinition.CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
                .put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
                .put("dueDate", dueDate)
                .build();
    }
}
