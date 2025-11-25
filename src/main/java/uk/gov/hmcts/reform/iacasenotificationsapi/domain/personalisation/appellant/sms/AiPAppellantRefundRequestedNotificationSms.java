package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealType;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.SystemDateProvider;

@Service
public class AiPAppellantRefundRequestedNotificationSms implements SmsNotificationPersonalisation {

    private final String refundRequestedAipSmsTemplateId;
    private final String refundRequestedAipPaPayLaterSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterSubmittingAppealRemission;

    public AiPAppellantRefundRequestedNotificationSms(
        @Value("${govnotify.template.requestFeeRemission.appellant.sms}") String refundRequestedAipSmsTemplateId,
        @Value("${govnotify.template.requestFeeRemission.appellant.paPayLater.sms}") String refundRequestedAipPaPayLaterSmsTemplateId,
        RecipientsFinder recipientsFinder,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmittingAppealRemission}") int daysToWaitAfterSubmittingAppealRemission,
        SystemDateProvider systemDateProvider
    ) {
        this.refundRequestedAipSmsTemplateId = refundRequestedAipSmsTemplateId;
        this.refundRequestedAipPaPayLaterSmsTemplateId = refundRequestedAipPaPayLaterSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmittingAppealRemission = daysToWaitAfterSubmittingAppealRemission;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<AppealType> maybeAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (maybeAppealType.isPresent() && maybeAppealType.get() == AppealType.PA) {
            Optional<String> maybePaymentOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class);

            if (maybePaymentOption.isPresent()) {
                String paymentOption = maybePaymentOption.get();
                if ("payLater".equals(paymentOption) || "payOffline".equals(paymentOption)) {
                    return refundRequestedAipPaPayLaterSmsTemplateId;
                }
            }
        }
        return refundRequestedAipSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_REFUND_REQUESTED_AIP_NOTIFICATION_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final String refundRequestDueDate = systemDateProvider.dueDate(daysToWaitAfterSubmittingAppealRemission);
        final String correctDateKey = getTemplateId(asylumCase).equals(refundRequestedAipPaPayLaterSmsTemplateId)
                ? "14 days after remission request sent"
                : "14 days after refund request sent";

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl)
                .put(correctDateKey, refundRequestDueDate)
                .build();
    }
}

