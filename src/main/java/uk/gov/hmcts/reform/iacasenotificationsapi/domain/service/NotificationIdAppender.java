package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Component
public class NotificationIdAppender {

    public List<IdValue<String>> append(
        List<IdValue<String>> existingNotificationsSent,
        String notificationReference,
        String notificationId
    ) {
        long numberOfExistingNotificationsOfSameKind =
            existingNotificationsSent
                .stream()
                .map(IdValue::getId)
                .filter(existingNotificationReference -> existingNotificationReference.startsWith(notificationReference))
                .count();

        String qualifiedNotificationReference;

        if (numberOfExistingNotificationsOfSameKind > 0) {
            qualifiedNotificationReference = notificationReference + "_" + (numberOfExistingNotificationsOfSameKind + 1);
        } else {
            qualifiedNotificationReference = notificationReference;
        }

        final List<IdValue<String>> newNotificationsSent = new ArrayList<>(existingNotificationsSent);

        newNotificationsSent.add(new IdValue<>(qualifiedNotificationReference, notificationId));

        return newNotificationsSent;
    }
}
