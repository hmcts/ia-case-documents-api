package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.SystemDateProvider;

@Service
public class AiPAppellantRefundRequestedNotificationSms implements SmsNotificationPersonalisation {

    private final String refundRequestedAipSmsTemplateId;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;
    private final SystemDateProvider systemDateProvider;
    private final int daysToWaitAfterSubmittingAppealRemission;

    public AiPAppellantRefundRequestedNotificationSms(
        @Value("${govnotify.template.requestFeeRemission.appellant.sms}") String refundRequestedAipSmsTemplateId,
        RecipientsFinder recipientsFinder,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        @Value("${appellantDaysToWait.afterSubmittingAppealRemission}") int daysToWaitAfterSubmittingAppealRemission,
        SystemDateProvider systemDateProvider
    ) {
        this.refundRequestedAipSmsTemplateId = refundRequestedAipSmsTemplateId;
        this.recipientsFinder = recipientsFinder;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.daysToWaitAfterSubmittingAppealRemission = daysToWaitAfterSubmittingAppealRemission;
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public String getTemplateId() {
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
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final String refundRequestDueDate = systemDateProvider.dueDate(daysToWaitAfterSubmittingAppealRemission);

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return
            ImmutableMap
                .<String, String>builder()
                .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
                .put("linkToService", iaAipFrontendUrl)
                .put("14 days after refund request sent", refundRequestDueDate)
                .build();
    }
}

