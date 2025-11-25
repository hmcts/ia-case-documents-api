package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.StoredNotification;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.RetryableNotificationClient;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS;

@Slf4j
@Component
public class NotificationSenderHelper<T extends CaseData> {

    private Cache<String, String> recentDeliveryReceiptCache;

    public String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference,
        RetryableNotificationClient notificationClient,
        Integer deduplicateSendsWithinSeconds,
        Logger logger,
        Callback<T> callback
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
                    logger.error("Failed to send email using GovNotify for case reference {}", reference, e);
                    storeFailedNotification(callback, e, reference, "email", emailAddress);
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
        Logger logger,
        Callback<T> callback
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
                    storeFailedNotification(callback, e, reference, "sms", phoneNumber);
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

    private void storeFailedNotification(Callback<T> callback, NotificationClientException e,
                                         String reference, String method, String phoneNumber) {
        AsylumCase asylumCase = (AsylumCase) callback.getCaseDetails().getCaseData();
        Optional<List<IdValue<StoredNotification>>> maybeExistingNotifications =
            asylumCase.read(NOTIFICATIONS);
        List<IdValue<StoredNotification>> allNotifications = maybeExistingNotifications.orElse(emptyList());

        String errorMessage = e.getMessage();
        StoredNotification storedNotification = getFailedNotification(errorMessage, reference,
            method, phoneNumber);
        allNotifications = append(storedNotification, allNotifications);
        List<IdValue<StoredNotification>> sortedNotifications = sortNotificationsByDate(allNotifications);
        asylumCase.write(NOTIFICATIONS, sortedNotifications);
    }

    private static StoredNotification getFailedNotification(String errorMessage, String reference, String method,
                                                            String sentTo) {
        ZonedDateTime zonedSentAt = ZonedDateTime.now()
            .withZoneSameInstant(ZoneId.of("Europe/London"));
        String sentAt = zonedSentAt.toLocalDateTime().toString();
        List<String> errorMessages = extractErrorMessages(errorMessage);
        return StoredNotification.builder()
            .notificationId("N/A")
            .notificationDateSent(sentAt)
            .notificationSentTo(sentTo)
            .notificationBody("N/A")
            .notificationMethod(StringUtils.capitalize(method))
            .notificationStatus("Failed")
            .notificationReference(reference)
            .notificationSubject(reference)
            .notificationErrorMessage(String.join("; ", errorMessages))
            .build();
    }

    private List<IdValue<StoredNotification>> append(
        StoredNotification newItem,
        List<IdValue<StoredNotification>> existingItems
    ) {

        requireNonNull(newItem);

        final List<IdValue<StoredNotification>> allItems = new ArrayList<>();

        int index = existingItems.size() + 1;

        IdValue<StoredNotification> itemIdValue = new IdValue<>(String.valueOf(index--), newItem);

        allItems.add(itemIdValue);

        for (IdValue<StoredNotification> existingItem : existingItems) {
            allItems.add(new IdValue<>(
                String.valueOf(index--),
                existingItem.getValue()));
        }

        return allItems;
    }

    public static List<String> extractErrorMessages(String exceptionMessage) {
        List<String> messages = new ArrayList<>();
        try {
            String jsonPart = exceptionMessage.substring(exceptionMessage.indexOf("{"));

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonPart);

            JsonNode errorsNode = rootNode.path("errors");
            if (errorsNode.isArray()) {
                for (JsonNode errorNode : errorsNode) {
                    String message = errorNode.path("message").asText();
                    messages.add(StringUtils.capitalize(message));
                }
            }
        } catch (Exception e) {
            log.warn("Extracting error messages failed: " + e.getMessage());
        }
        return messages;
    }

    private List<IdValue<StoredNotification>> sortNotificationsByDate(List<IdValue<StoredNotification>> allNotifications) {
        List<IdValue<StoredNotification>> mutableNotifications = new ArrayList<>(allNotifications);
        mutableNotifications.sort(Comparator.comparing(notification ->
                LocalDateTime.parse(notification.getValue().getNotificationDateSent()),
            Comparator.reverseOrder()
        ));
        return mutableNotifications;
    }
}
