package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

public class EmailNotificationGenerator implements NotificationGenerator {

    protected final List<EmailNotificationPersonalisation> personalisationList;
    protected final NotificationIdAppender notificationIdAppender;
    protected final GovNotifyNotificationSender notificationSender;

    public EmailNotificationGenerator(
        List<EmailNotificationPersonalisation> repPersonalisationList,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        this.personalisationList = repPersonalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        personalisationList.forEach(personalisation -> {
            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = createEmail(personalisation, asylumCase, referenceId, callback);
            notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
        });
    }

    protected List<String> createEmail(
        final BaseNotificationPersonalisation personalisation,
        final AsylumCase asylumCase,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        EmailNotificationPersonalisation emailNotificationPersonalisation = (EmailNotificationPersonalisation) personalisation;
        Set<String> subscriberEmails = emailNotificationPersonalisation.getRecipientsList(asylumCase);

        return subscriberEmails.stream()
            .map(email ->
                sendEmail(
                    email,
                    emailNotificationPersonalisation,
                    referenceId,
                    callback)).collect(Collectors.toList());
    }

    protected String sendEmail(
        final String email,
        final EmailNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        String emailTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();

        return notificationSender.sendEmail(
            emailTemplateId,
            email,
            personalisation.getPersonalisation(callback),
            referenceId
        );
    }

}
