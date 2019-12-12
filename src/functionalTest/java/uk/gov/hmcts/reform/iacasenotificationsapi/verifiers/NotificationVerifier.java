package uk.gov.hmcts.reform.iacasenotificationsapi.verifiers;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.util.MapValueExtractor;
import uk.gov.service.notify.Notification;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

@Component
@SuppressWarnings("unchecked")
public class NotificationVerifier implements Verifier {

    @Autowired private NotificationClient notificationClient;

    public void verify(
        long testCaseId,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    ) {
        String description = MapValueExtractor.extract(scenario, "description");

        List<Map<String, Object>> expectedNotifications =
            MapValueExtractor.extractOrDefault(scenario, "expectation.notifications", Collections.emptyList());

        if (expectedNotifications == null
            || expectedNotifications.size() == 0) {
            return;
        }

        List<Map<String, Object>> notificationsSent =
            MapValueExtractor.extractOrDefault(actualResponse, "data.notificationsSent", Collections.emptyList());

        if (notificationsSent.isEmpty()) {
            assertFalse(
                description + ": Notifications were delivered",
                notificationsSent.isEmpty()
            );
        }

        Map<String, String> notificationsSentMap =
            notificationsSent
                .stream()
                .collect(Collectors.toMap(
                    notificationSent -> (String) notificationSent.get("id"),
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
                        description + ": Notification " + expectedReference + " was delivered successfully",
                        true
                    );
                }

                try {

                    Notification notification = notificationClient.getNotificationById(deliveredNotificationId);

                    final String actualReference = notification.getReference().orElse("");
                    final String actualRecipient = notification.getEmailAddress().orElse(notification.getPhoneNumber().orElse(""));
                    final String actualSubject = notification.getSubject().orElse("");
                    final String actualBody = notification.getBody();

                    assertThat(
                        description + ": Notification " + expectedReference + " was delivered with correct reference",
                        actualReference,
                        equalTo(expectedReference)
                    );

                    assertThat(
                        description + ": Notification " + expectedReference + " was delivered to correct recipient",
                        actualRecipient,
                        equalTo(expectedRecipient)
                    );

                    assertThat(
                        description + ": Notification " + expectedReference + " was delivered with correct subject content",
                        actualSubject,
                        equalTo(expectedSubject)
                    );

                    if (expectedBodyUnknownType instanceof String) {

                        assertThat(
                            description + ": Notification " + expectedReference + " was delivered with correct body content",
                            actualBody,
                            equalTo((String) expectedBodyUnknownType)
                        );

                    } else {

                        List<String> expectedBodyMatches = (List<String>) expectedBodyUnknownType;

                        expectedBodyMatches.forEach(expectedBodyMatch -> {

                            assertThat(
                                description + ": Notification " + expectedReference + " was delivered with correct body content match",
                                actualBody,
                                containsString(expectedBodyMatch)
                            );
                        });
                    }

                } catch (NotificationClientException e) {
                    assertFalse(
                        description + ": Notification " + deliveredNotificationId + " was found on GovNotify",
                        true
                    );
                }
            });
    }
}
