package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EndAppealHomeOfficePersonalisationFactory;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;


@Component
public class EndAppealHomeOfficeNotifier implements PreSubmitCallbackHandler<AsylumCase> {

    private final String endAppealHomeOfficeTemplateId;
    private final String endAppealEmailAddresses;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;
    private final EndAppealHomeOfficePersonalisationFactory personalisationFactory;

    public EndAppealHomeOfficeNotifier(
        @Value("${govnotify.template.endAppealHomeOfficeTemplateId}") String endAppealHomeOfficeTemplateId,
        @Value("${endAppealHomeOfficeEmailAddress}") String endAppealEmailAddresses,
        NotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender,
        EndAppealHomeOfficePersonalisationFactory personalisationFactory
    ) {
        requireNonNull(endAppealHomeOfficeTemplateId, "endAppealHomeOfficeTemplateId must not be null");

        this.endAppealHomeOfficeTemplateId = endAppealHomeOfficeTemplateId;
        this.endAppealEmailAddresses = endAppealEmailAddresses;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
        this.personalisationFactory = personalisationFactory;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.END_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String reference =
            callback.getCaseDetails().getId()
            + "_END_APPEAL_HOME_OFFICE";

        Map<String, String> personalisation = personalisationFactory.create(asylumCase);

        String notificationId =
            notificationSender.sendEmail(
                endAppealHomeOfficeTemplateId,
                endAppealEmailAddresses,
                personalisation,
                reference
            );

        Optional<List<IdValue<String>>> maybeNotificationSent = asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationsSent = maybeNotificationSent
            .orElseGet(ArrayList::new);

        asylumCase.write(NOTIFICATIONS_SENT,
            notificationIdAppender.append(
                notificationsSent,
                reference,
                notificationId
            )
        );

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
