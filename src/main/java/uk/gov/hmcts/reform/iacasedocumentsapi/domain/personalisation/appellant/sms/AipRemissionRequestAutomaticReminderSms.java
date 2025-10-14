package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.CommonUtils.convertAsylumCaseFeeValue;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;

@Service
public class AipRemissionRequestAutomaticReminderSms implements SmsNotificationPersonalisation {
    private final String paymentRejectedReminderSmsTemplateId;
    private final String paymentPartiallyApprovedReminderSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    public AipRemissionRequestAutomaticReminderSms(
        @Value("${govnotify.template.remissionDecisionReminder.appellant.rejected.sms}") String paymentRejectedReminderSmsTemplateId,
        @Value("${govnotify.template.remissionDecisionReminder.appellant.partiallyApproved.sms}") String paymentPartiallyApprovedReminderSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl, RecipientsFinder recipientsFinder
    ) {
        this.paymentRejectedReminderSmsTemplateId = paymentRejectedReminderSmsTemplateId;
        this.paymentPartiallyApprovedReminderSmsTemplateId = paymentPartiallyApprovedReminderSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        RemissionDecision remissionDecision = asylumCase.read(AsylumCaseDefinition.REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision not found"));

        if (remissionDecision.equals(RemissionDecision.PARTIALLY_APPROVED)) {
            return paymentPartiallyApprovedReminderSmsTemplateId;
        } else if (remissionDecision.equals(RemissionDecision.REJECTED)) {
            return paymentRejectedReminderSmsTemplateId;
        }
        throw new IllegalStateException("error retrieving remission decision");
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_AIP_REMISSION_REMINDER_DECISION_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("feeAmount", convertAsylumCaseFeeValue(asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class).orElse("")))
            .put("deadline", asylumCase.read(REMISSION_REJECTED_DATE_PLUS_14DAYS, String.class).orElse(""))
            .put("onlineCaseReferenceNumber", asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse(""))
            .put("feeAmountRejected", convertAsylumCaseFeeValue(asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("")))
            .put("linkToOnlineService", iaAipFrontendUrl)
            .build();
    }
}
