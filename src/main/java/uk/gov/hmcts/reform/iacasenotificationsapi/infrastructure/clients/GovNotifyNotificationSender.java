package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelper;

@Service
public class GovNotifyNotificationSender implements NotificationSender {

    private static final org.slf4j.Logger LOG = getLogger(GovNotifyNotificationSender.class);

    private final int deduplicateSendsWithinSeconds;
    private final RetryableNotificationClient notificationClient;
    private final NotificationSenderHelper senderHelper;

    public GovNotifyNotificationSender(
        @Value("${notificationSender.deduplicateSendsWithinSeconds}") int deduplicateSendsWithinSeconds,
        RetryableNotificationClient notificationClient,
        NotificationSenderHelper senderHelper
    ) {
        this.deduplicateSendsWithinSeconds = deduplicateSendsWithinSeconds;
        this.notificationClient = notificationClient;
        this.senderHelper = senderHelper;
    }

    public synchronized String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference
    ) {
        return senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        );
    }

    @Override
    public synchronized String sendSms(
        final String templateId,
        final String phoneNumber,
        final Map<String, String> personalisation,
        final String reference) {

        return senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        );
    }
}
