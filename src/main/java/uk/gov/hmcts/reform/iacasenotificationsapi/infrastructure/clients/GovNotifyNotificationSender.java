package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.InputStream;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelper;

@Service
public class GovNotifyNotificationSender implements NotificationSender<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(GovNotifyNotificationSender.class);

    private final int deduplicateSendsWithinSeconds;
    private final RetryableNotificationClient notificationClient;
    private final NotificationSenderHelper<AsylumCase> senderHelper;

    public GovNotifyNotificationSender(
        @Value("${notificationSender.deduplicateSendsWithinSeconds}") int deduplicateSendsWithinSeconds,
        RetryableNotificationClient notificationClient,
        NotificationSenderHelper<AsylumCase> senderHelper
    ) {
        this.deduplicateSendsWithinSeconds = deduplicateSendsWithinSeconds;
        this.notificationClient = notificationClient;
        this.senderHelper = senderHelper;
    }

    public synchronized String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference,
        Callback<AsylumCase> callback
    ) {
        return senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                callback

        );
    }

    public synchronized String sendEmailWithLink(
            String templateId,
            String emailAddress,
            Map<String, Object> personalisation,
            String reference
    ) {
        return senderHelper.sendEmailWithLink(
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
        final String reference,
        final Callback<AsylumCase> callback) {

        return senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG,
                callback
        );
    }

    @Override
    public synchronized String sendLetter(
        final String templateId,
        final String address,
        final Map<String, String> personalisation,
        final String reference,
        final Callback<AsylumCase> callback) {

        return senderHelper.sendLetter(
            templateId,
            address,
            personalisation,
            reference,
            notificationClient,
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
            notificationClient,
            deduplicateSendsWithinSeconds,
            LOG
        );
    }
}
