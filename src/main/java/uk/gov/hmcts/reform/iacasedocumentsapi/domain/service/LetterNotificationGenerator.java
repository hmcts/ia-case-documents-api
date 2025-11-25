package uk.gov.hmcts.reform.iacasedocumentsapi.domain.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.LetterNotificationPersonalisation;

public class LetterNotificationGenerator implements NotificationGenerator {

    protected final List<LetterNotificationPersonalisation> personalisationList;
    protected final NotificationIdAppender notificationIdAppender;
    protected final NotificationSender<AsylumCase> notificationSender;

    public LetterNotificationGenerator(
        List<LetterNotificationPersonalisation> personalisationList,
        NotificationSender<AsylumCase> notificationSender,
        NotificationIdAppender notificationIdAppender) {

        this.personalisationList = personalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        personalisationList.forEach(personalisation -> {
            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = createLetter(personalisation, asylumCase, referenceId, callback);
            notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
        });
    }

    protected List<String> createLetter(
        final BaseNotificationPersonalisation personalisation,
        final AsylumCase asylumCase,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        LetterNotificationPersonalisation letterNotificationPersonalisation = (LetterNotificationPersonalisation) personalisation;
        Set<String> addresses = letterNotificationPersonalisation.getRecipientsList(asylumCase);

        return addresses.stream()
            .map(address ->
                sendLetter(
                    address,
                    letterNotificationPersonalisation,
                    referenceId,
                    callback)).collect(Collectors.toList());
    }

    protected String sendLetter(
        final String address,
        final LetterNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback) {

        String letterTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();

        return notificationSender.sendLetter(
            letterTemplateId,
            address,
            personalisation.getPersonalisation(callback),
            referenceId,
            callback
        );
    }

}
