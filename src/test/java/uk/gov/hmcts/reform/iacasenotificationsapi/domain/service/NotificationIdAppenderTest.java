package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static junit.framework.TestCase.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class NotificationIdAppenderTest {

    private final NotificationIdAppender notificationIdAppender = new NotificationIdAppender();

    private final IdValue<String> existingNotification1 = new IdValue<>("foo", "111-222");
    private final IdValue<String> existingNotification2 = new IdValue<>("bar", "333-444");

    private final List<IdValue<String>> existingNotificationsSent =
        Arrays.asList(
            existingNotification1,
            existingNotification2
        );

    @Test
    public void should_append_first_notification_without_qualifier() {

        List<IdValue<String>> actualNotificationsSent =
            notificationIdAppender.append(
                existingNotificationsSent,
                "something",
                "555-666"
            );

        assertEquals(3, actualNotificationsSent.size());
        assertEquals(existingNotification1, actualNotificationsSent.get(0));
        assertEquals(existingNotification2, actualNotificationsSent.get(1));

        assertEquals("something", actualNotificationsSent.get(2).getId());
        assertEquals("555-666", actualNotificationsSent.get(2).getValue());
    }

    @Test
    public void should_append_subsequent_notifications_with_qualifiers() {

        List<IdValue<String>> actualNotificationsSent1 =
            notificationIdAppender.append(
                existingNotificationsSent,
                "foo",
                "555-666"
            );

        List<IdValue<String>> actualNotificationsSent2 =
            notificationIdAppender.append(
                actualNotificationsSent1,
                "foo",
                "777-888"
            );

        assertEquals(4, actualNotificationsSent2.size());
        assertEquals(existingNotification1, actualNotificationsSent2.get(0));
        assertEquals(existingNotification2, actualNotificationsSent2.get(1));

        assertEquals("foo_2", actualNotificationsSent2.get(2).getId());
        assertEquals("555-666", actualNotificationsSent2.get(2).getValue());

        assertEquals("foo_3", actualNotificationsSent2.get(3).getId());
        assertEquals("777-888", actualNotificationsSent2.get(3).getValue());
    }
}
