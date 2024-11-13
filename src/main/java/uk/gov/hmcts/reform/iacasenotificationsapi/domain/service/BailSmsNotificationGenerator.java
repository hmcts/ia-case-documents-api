package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailSmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.BailGovNotifyNotificationSender;

public class BailSmsNotificationGenerator implements BailNotificationGenerator {

    private final List<BailSmsNotificationPersonalisation> personalisationList;
    private final BailNotificationIdAppender notificationIdAppender;
    private final BailGovNotifyNotificationSender notificationSender;

    public BailSmsNotificationGenerator(
        List<BailSmsNotificationPersonalisation> aipPersonalisationList,
        BailGovNotifyNotificationSender notificationSender,
        BailNotificationIdAppender notificationIdAppender) {

        this.personalisationList = aipPersonalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<BailCase> callback) {

        final BailCase bailCase = callback.getCaseDetails().getCaseData();

        personalisationList.forEach(personalisation -> {
            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = createSms(personalisation, bailCase, referenceId, callback);
            notificationIdAppender.appendAll(bailCase, referenceId, notificationIds);
        });
    }

    private List<String> createSms(
        final BaseNotificationPersonalisation<BailCase> personalisation,
        final BailCase bailCase,
        final String referenceId,
        final Callback<BailCase> callback) {
        List<String> notificationIds = new ArrayList<>();

        BailSmsNotificationPersonalisation smsNotificationPersonalisation = (BailSmsNotificationPersonalisation) personalisation;

        Set<String> phoneNumbers = smsNotificationPersonalisation.getRecipientsList(bailCase);

        notificationIds.addAll(
            phoneNumbers.stream()
                .map(phoneNumber ->
                    sendSms(
                        phoneNumber,
                        smsNotificationPersonalisation,
                        referenceId,
                        callback))
                .collect(Collectors.toList())
        );

        return notificationIds;
    }

    private String sendSms(
        final String mobileNumber,
        final BailSmsNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<BailCase> callback) {
        String smsTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();
        return notificationSender.sendSms(
            smsTemplateId,
            mobileNumber,
            personalisation.getPersonalisation(callback),
            referenceId,
            callback
        );
    }
}
