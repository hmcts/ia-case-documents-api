package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.slf4j.LoggerFactory.getLogger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.UnrecoverableException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@Service
public class GovNotifyNotificationSender implements NotificationSender {

    private static final org.slf4j.Logger LOG = getLogger(GovNotifyNotificationSender.class);

    private final int deduplicateSendsWithinSeconds;
    private final NotificationClient notificationClient;

    private Cache<String, String> recentDeliveryReceiptCache;

    public GovNotifyNotificationSender(
        @Value("${notificationSender.deduplicateSendsWithinSeconds}") int deduplicateSendsWithinSeconds,
        NotificationClient notificationClient
    ) {
        this.deduplicateSendsWithinSeconds = deduplicateSendsWithinSeconds;
        this.notificationClient = notificationClient;
    }

    public synchronized String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache();

        return recentDeliveryReceiptCache.get(
            emailAddress + reference,
            k -> {

                try {

                    LOG.info("Attempting to send email notification to GovNotify: {}", reference);

                    SendEmailResponse response =
                        notificationClient
                            .sendEmail(
                                templateId,
                                emailAddress,
                                personalisation,
                                reference
                            );

                    String notificationId =
                        response
                            .getNotificationId()
                            .toString();

                    LOG.info(
                        "Successfully sent email notification to GovNotify: {} ({})",
                        reference,
                        notificationId
                    );

                    return notificationId;

                } catch (NotificationClientException e) {
                    throw new UnrecoverableException("Failed to send email using GovNotify", e);
                }
            }
        );
    }

    private Cache<String, String> getOrCreateDeliveryReceiptCache() {

        if (recentDeliveryReceiptCache == null) {
            recentDeliveryReceiptCache =
                Caffeine
                    .newBuilder()
                    .expireAfterWrite(deduplicateSendsWithinSeconds, TimeUnit.SECONDS)
                    .build();
        }

        return recentDeliveryReceiptCache;
    }
}
