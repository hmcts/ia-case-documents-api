package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.service.notify.*;

@Component
public class NotificationSenderHelper {

    private Cache<String, String> recentDeliveryReceiptCache;

    public String sendEmail(
            String templateId,
            String emailAddress,
            Map<String, String> personalisation,
            String reference,
            RetryableNotificationClient notificationClient,
            Integer deduplicateSendsWithinSeconds,
            Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
                emailAddress + reference,
                k -> {
                    try {
                        logger.info("Attempting to send email notification to GovNotify: {}", reference);

                        SendEmailResponse response = notificationClient.sendEmail(
                                templateId,
                                emailAddress,
                                personalisation,
                                reference
                        );

                        String notificationId = response.getNotificationId().toString();

                        logger.info("Successfully sent email notification to GovNotify: {} ({})",
                                reference,
                                notificationId
                        );

                        return notificationId;

                    } catch (NotificationClientException e) {
                        logger.error("Failed to send sms using GovNotify for case reference {}", reference, e);
                    }
                    return Strings.EMPTY;
                }
        );
    }

    public String sendEmailWithLink(
            String templateId,
            String emailAddress,
            Map<String, Object> personalisation,
            String reference,
            RetryableNotificationClient notificationClient,
            Integer deduplicateSendsWithinSeconds,
            Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
                emailAddress + reference,
                k -> {
                    try {
                        logger.info("Attempting to send email notification to GovNotify: {}", reference);

                        SendEmailResponse response = notificationClient.sendEmail(
                                templateId,
                                emailAddress,
                                personalisation,
                                reference
                        );

                        String notificationId = response.getNotificationId().toString();

                        logger.info("Successfully sent email notification to GovNotify: {} ({})",
                                reference,
                                notificationId
                        );

                        return notificationId;

                    } catch (NotificationClientException e) {
                        throw new NotificationServiceResponseException("Failed to send email using GovNotify", e);
                    }
                }
        );
    }

    public String sendSms(
            String templateId,
            String phoneNumber,
            Map<String, String> personalisation,
            String reference,
            RetryableNotificationClient notificationClient,
            Integer deduplicateSendsWithinSeconds,
            Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
                phoneNumber + reference,
                k -> {
                    try {
                        logger.info("Attempting to send a text message notification to GovNotify: {}", reference);

                        SendSmsResponse response = notificationClient.sendSms(
                                templateId,
                                phoneNumber,
                                personalisation,
                                reference
                        );

                        String notificationId = response.getNotificationId().toString();

                        logger.info("Successfully sent sms notification to GovNotify: {} ({})",
                                reference,
                                notificationId
                        );

                        return notificationId;

                    } catch (NotificationClientException e) {
                        logger.error("Failed to send sms using GovNotify for case reference {}", reference, e);
                    }
                    return Strings.EMPTY;
                }
        );
    }

    public String sendLetter(
        String templateId,
        String address,
        Map<String, String> personalisation,
        String reference,
        RetryableNotificationClient notificationClient,
        Integer deduplicateSendsWithinSeconds,
        Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
            address + reference,
            k -> {
                try {
                    logger.info("Attempting to send letter notification to GovNotify: {}", reference);

                    SendLetterResponse response = notificationClient.sendLetter(
                        templateId,
                        personalisation,
                        reference
                    );

                    String notificationId = response.getNotificationId().toString();

                    logger.info("Successfully sent letter notification to GovNotify: {} ({})",
                        reference,
                        notificationId
                    );

                    return notificationId;

                } catch (NotificationClientException e) {
                    logger.error("Failed to send letter using GovNotify", reference, e);
                }
                return Strings.EMPTY;
            }
        );
    }

    public String sendPrecompiledLetter(
        String reference,
        InputStream stream,
        RetryableNotificationClient notificationClient,
        Integer deduplicateSendsWithinSeconds,
        Logger logger
    ) {
        recentDeliveryReceiptCache = getOrCreateDeliveryReceiptCache(deduplicateSendsWithinSeconds);
        return recentDeliveryReceiptCache.get(
            reference,
            k -> {
                try {
                    logger.info("Attempting to send pre-compiled letter notification to GovNotify: {}", reference);

                    LetterResponse response = notificationClient.sendPrecompiledLetter(
                        reference,
                        stream
                    );

                    String notificationId = response.getNotificationId().toString();

                    logger.info("Successfully sent pre-compiled letter notification to GovNotify: {} ({})",
                        reference,
                        notificationId
                    );

                    return notificationId;

                } catch (NotificationClientException e) {
                    logger.error("Failed to send pre-compiled letter using GovNotify", reference, e);
                }
                return Strings.EMPTY;
            }
        );
    }

    private Cache<String, String> getOrCreateDeliveryReceiptCache(int deduplicateSendsWithinSeconds) {
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
