package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelper;

@Service
public class BailGovNotifyNotificationSender implements NotificationSender {

    private static final org.slf4j.Logger LOG = getLogger(BailGovNotifyNotificationSender.class);

    private final int deduplicateSendsWithinSeconds;

    @Autowired
    @Qualifier("BailClient")
    private final RetryableNotificationClient notificationBailClient;

    private final NotificationSenderHelper senderHelper;

    public BailGovNotifyNotificationSender(
        @Value("${notificationSender.deduplicateSendsWithinSeconds}") int deduplicateSendsWithinSeconds,
        RetryableNotificationClient notificationBailClient,
        NotificationSenderHelper senderHelper
    ) {
        this.deduplicateSendsWithinSeconds = deduplicateSendsWithinSeconds;
        this.notificationBailClient = notificationBailClient;
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
                notificationBailClient,
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
                notificationBailClient,
                deduplicateSendsWithinSeconds,
                LOG
        );
    }

    @Override
    public synchronized String sendLetter(
        final String templateId,
        final String address,
        final Map<String, String> personalisation,
        final String reference) {

        return senderHelper.sendSms(
            templateId,
            address,
            personalisation,
            reference,
            notificationBailClient,
            deduplicateSendsWithinSeconds,
            LOG
        );
    }

    @Override
    public synchronized String sendPrecompiledLetter(
        final String reference,
        final InputStream stream
    ) {
        return senderHelper.sendPrecompiledLetter(
            reference,
            stream,
            notificationBailClient,
            deduplicateSendsWithinSeconds,
            LOG
        );
    }
}
