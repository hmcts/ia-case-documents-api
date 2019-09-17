package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealStateNotificationReference.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Component
public class EndAppealNotifier extends AbstractNotifier implements PreSubmitCallbackHandler<AsylumCase>  {

    @Autowired
    public EndAppealNotifier(
            GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
            @Value("${endAppealHomeOfficeEmailAddress}") String endAppealEmailAddresses,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender,
            EndAppealPersonalisationFactory endAppealPersonalisationFactory
    ) {

        super(
                govNotifyTemplateIdConfiguration,
                endAppealEmailAddresses,
                END_APPEAL_HOME_OFFICE,
                END_APPEAL_LEGAL_REPRESENTATIVE,
                notificationSender,
                notificationIdAppender,
                endAppealPersonalisationFactory
        );

        homeOfficeTemplateId = govNotifyTemplateIdConfiguration.getEndAppealHomeOfficeTemplateId();
        legalRepresentativeTemplateId = govNotifyTemplateIdConfiguration.getEndAppealLegalRepresentativeTemplateId();
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


        sendNotificationToHomeOffice(callback, asylumCase);

        sendNotificationToLegalRepresentative(callback, asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }



}
