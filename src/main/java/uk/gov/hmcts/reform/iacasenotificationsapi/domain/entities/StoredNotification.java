package uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities;

import lombok.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.Document;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@AllArgsConstructor
public class StoredNotification {
    @NonNull private String notificationId;
    @NonNull private String notificationDateSent;
    @NonNull private String notificationSentTo;
    @NonNull private String notificationBody;
    @Setter private Document notificationDocument;
    @NonNull private String notificationMethod;
    @NonNull private String notificationStatus;
    @NonNull private String notificationReference;
    @NonNull private String notificationSubject;
    private String notificationErrorMessage;
}
