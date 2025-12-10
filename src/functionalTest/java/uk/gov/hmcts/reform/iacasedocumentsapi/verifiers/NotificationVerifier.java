package uk.gov.hmcts.reform.iacasedocumentsapi.verifiers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.hmcts.reform.iacasedocumentsapi.util.MapValueExtractor;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClientException;

@Component
@SuppressWarnings("unchecked")
public class NotificationVerifier implements Verifier {

    @Autowired
    private RetryableNotificationClient notificationClient;

    @Qualifier("BailClient")
    @Autowired
    private RetryableNotificationClient bailNotificationClient;

    public void verify(
        long testCaseId,
        boolean isAsylumCase,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        String description = MapValueExtractor.extract(scenario, "description");

        List<Map<String, Object>> expectedNotifications =
            MapValueExtractor.extractOrDefault(scenario, "expectation.notifications", Collections.emptyList());

        if (expectedNotifications == null || expectedNotifications.isEmpty()) {
            return;
        }

        List<Map<String, Object>> notificationsSent;
        if (actualResponse.containsKey("confirmation_body")) {
            String confirmationBody =
                MapValueExtractor.extractOrThrow(actualResponse, "confirmation_body");
            String[] notificationSplit = confirmationBody.split("notificationsSent=\\[");
            String notifications = notificationSplit[1].split("\\)]")[0] + ")";
            notificationSplit = notifications.split("IdValue");
            List<String> notificationIdValues = Arrays.asList(notificationSplit);
            notificationsSent =
                notificationIdValues.stream()
                    .filter(notificationIdValue -> notificationIdValue.contains("(id="))
                    .map(notificationIdValue -> {
                        String trimmedIdValue = notificationIdValue
                            .replaceAll("\\(id=", "")
                            .replaceAll("\\)", "")
                            .replaceAll(",", "");
                        String[] idValueParts = trimmedIdValue.split("value=");
                        return Map.of(
                            "id", idValueParts[0].trim(),
                            "value", (Object) idValueParts[1].trim()
                        );
                    }).toList();
        } else {
            notificationsSent =
                MapValueExtractor.extractOrDefault(actualResponse, "data.notificationsSent", Collections.emptyList());
        }

        if (notificationsSent.isEmpty()) {
            assertFalse(
                notificationsSent.isEmpty(),
                description + ": Notifications were not delivered"
            );
        }

        Map<String, String> notificationsSentMap =
            notificationsSent
                .stream()
                .collect(Collectors.toMap(
                    notificationSent -> sanitizeNotificationId((String) notificationSent.get("id")),
                    notificationSent -> (String) notificationSent.get("value")
                ));

        expectedNotifications
            .forEach(expectedNotification -> {

                final String expectedReference = MapValueExtractor.extractOrThrow(expectedNotification, "reference");
                final String expectedRecipient = MapValueExtractor.extractOrThrow(expectedNotification, "recipient");
                final String expectedSubject = MapValueExtractor.extractOrThrow(expectedNotification, "subject");
                final Object expectedBodyUnknownType = MapValueExtractor.extractOrThrow(expectedNotification, "body");

                final String deliveredNotificationId = notificationsSentMap.get(expectedReference);
                if (Strings.isNullOrEmpty(deliveredNotificationId)) {
                    assertFalse(
                        true,
                        description
                            + ": Notification " + expectedReference + " was not delivered successfully"
                    );
                }

                try {

                    Notification notification = isAsylumCase
                        ? notificationClient.getNotificationById(deliveredNotificationId)
                        : bailNotificationClient.getNotificationById(deliveredNotificationId);

                    final String actualReference = sanitizeNotificationId(notification.getReference().orElse(""));
                    final String actualRecipient =
                        notification.getEmailAddress().orElse(notification.getPhoneNumber().orElse(""));
                    final String actualSubject = notification.getSubject().orElse("");
                    final String actualBody = notification.getBody();

                    assertThat(
                        description
                            + ": Notification "
                            + expectedReference
                            + " was delivered with wrong reference",
                        actualReference,
                        equalTo(expectedReference)
                    );

                    assertThat(
                        description
                            + ": Notification "
                            + expectedReference
                            + " was delivered to wrong recipient",
                        actualRecipient,
                        equalTo(expectedRecipient)
                    );

                    assertThat(
                        description
                            + ": Notification "
                            + expectedReference
                            + " was delivered with wrong subject content",
                        actualSubject,
                        equalTo(expectedSubject)
                    );

                    if (expectedBodyUnknownType instanceof String) {

                        assertThat(
                            description
                                + ": Notification "
                                + expectedReference
                                + " was delivered with wrong body content",
                            actualBody,
                            equalTo((String) expectedBodyUnknownType)
                        );

                    } else {

                        List<String> expectedBodyMatches = (List<String>) expectedBodyUnknownType;

                        expectedBodyMatches.forEach(expectedBodyMatch -> {

                            assertThat(
                                description
                                    + ": Notification "
                                    + expectedReference
                                    + " was delivered with wrong body content match",
                                actualBody,
                                containsString(expectedBodyMatch)
                            );
                        });
                    }

                } catch (NotificationClientException e) {
                    assertFalse(
                        true,
                        description + ": Notification " + deliveredNotificationId + " was not found on GovNotify"
                    );
                }
            });
    }

    private String sanitizeNotificationId(String notificationIdWithTimestamp) {
        // Regular expression to remove the last underscore and following timestamp in epochmillis
        Pattern pattern = Pattern.compile("^(.*)_\\d{13}$");
        Matcher matcher = pattern.matcher(notificationIdWithTimestamp);
        return matcher.find() ? matcher.group(1) : notificationIdWithTimestamp;
    }
}
