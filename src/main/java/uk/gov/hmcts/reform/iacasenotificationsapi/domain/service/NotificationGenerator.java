package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.NotificationPersonalisation;

public class NotificationGenerator {

    private final List<NotificationPersonalisation> personalisationList;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;

    public NotificationGenerator(List<NotificationPersonalisation> personalisationList,
                                 NotificationSender notificationSender,
                                 NotificationIdAppender notificationIdAppender) {

        this.personalisationList = personalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        personalisationList.forEach(personalisation -> {

            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());

            String notificationId =
                notificationSender.sendEmail(
                    personalisation.getTemplateId(asylumCase),
                    personalisation.getEmailAddress(asylumCase),
                    personalisation.getPersonalisation(callback),
                    referenceId
                );

            Optional<List<IdValue<String>>> maybeNotificationSent =
                asylumCase.read(NOTIFICATIONS_SENT);

            List<IdValue<String>> notificationsSent =
                maybeNotificationSent
                    .orElseGet(ArrayList::new);

            asylumCase.write(NOTIFICATIONS_SENT,
                notificationIdAppender.append(
                    notificationsSent,
                    referenceId,
                    notificationId
                )
            );
        });
    }
}
