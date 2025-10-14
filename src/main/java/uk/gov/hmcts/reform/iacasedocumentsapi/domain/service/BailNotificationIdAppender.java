package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;

@Component
public class BailNotificationIdAppender {

    public void appendAll(final BailCase bailCase, final String referenceId, final List<String> notificationIds) {
        Optional<List<IdValue<String>>> maybeNotificationSent =
            bailCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent =
            maybeNotificationSent
                .orElseGet(ArrayList::new);

        notificationIds.forEach(notificationId ->
            bailCase.write(NOTIFICATIONS_SENT, append(
                    notificationsSent,
                    referenceId,
                    notificationId
                )
            ));
    }

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

        String qualifiedNotificationReference = numberOfExistingNotificationsOfSameKind > 0
            ?
            (notificationReference + "_" + UUID.randomUUID().toString()) : notificationReference;

        final List<IdValue<String>> newNotificationsSent = new ArrayList<>(existingNotificationsSent);

        newNotificationsSent.add(new IdValue<>(qualifiedNotificationReference, notificationId));

        return newNotificationsSent;
    }

}
