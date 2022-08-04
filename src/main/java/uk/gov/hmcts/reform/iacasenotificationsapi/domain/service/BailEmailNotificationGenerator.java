package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.BailGovNotifyNotificationSender;

public class BailEmailNotificationGenerator implements BailNotificationGenerator {

    protected final List<BailEmailNotificationPersonalisation> bailEmailNotificationPersonalisations;
    protected final BailNotificationIdAppender notificationIdAppender;
    protected final BailGovNotifyNotificationSender notificationSender;

    public BailEmailNotificationGenerator(
        List<BailEmailNotificationPersonalisation> bailEmailNotificationPersonalisations,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        this.bailEmailNotificationPersonalisations = bailEmailNotificationPersonalisations;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<BailCase> callback) {

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        bailEmailNotificationPersonalisations.forEach(bailEmailNotificationPersonalisation -> {
            String referenceId = bailEmailNotificationPersonalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = createEmail(bailEmailNotificationPersonalisation, bailCase, referenceId, callback);
            notificationIdAppender.appendAll(bailCase, referenceId, notificationIds);
        });
    }

    protected List<String> createEmail(
        final BaseNotificationPersonalisation personalisation,
        final BailCase bailCase,
        final String referenceId,
        final Callback<BailCase> callback) {

        BailEmailNotificationPersonalisation emailNotificationPersonalisation = (BailEmailNotificationPersonalisation) personalisation;
        Set<String> subscriberEmails = emailNotificationPersonalisation.getRecipientsList(bailCase);

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
        final BailEmailNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<BailCase> callback) {

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
