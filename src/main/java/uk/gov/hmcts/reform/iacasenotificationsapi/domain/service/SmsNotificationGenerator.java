package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

public class SmsNotificationGenerator implements NotificationGenerator {

    private final List<SmsNotificationPersonalisation> personalisationList;
    private final NotificationIdAppender notificationIdAppender;
    private final GovNotifyNotificationSender notificationSender;

    public SmsNotificationGenerator(
        List<SmsNotificationPersonalisation> aipPersonalisationList,
        GovNotifyNotificationSender notificationSender,
        NotificationIdAppender notificationIdAppender) {

        this.personalisationList = aipPersonalisationList;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
    }

    @Override
    public void generate(Callback<AsylumCase> callback) {

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        personalisationList.forEach(personalisation -> {
            String referenceId = personalisation.getReferenceId(callback.getCaseDetails().getId());
            List<String> notificationIds = createSms(personalisation, asylumCase, referenceId, callback);
            notificationIdAppender.appendAll(asylumCase, referenceId, notificationIds);
        });
    }

    private List<String> createSms(
        final BaseNotificationPersonalisation personalisation,
        final AsylumCase asylumCase,
        final String referenceId,
        final Callback<AsylumCase> callback) {
        List<String> notificationIds = new ArrayList<>();

        SmsNotificationPersonalisation smsNotificationPersonalisation = (SmsNotificationPersonalisation) personalisation;

        Set<String> phoneNumbers = smsNotificationPersonalisation.getRecipientsList(asylumCase);

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
        final SmsNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback) {
        String smsTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();
        return notificationSender.sendSms(
            smsTemplateId,
            mobileNumber,
            personalisation.getPersonalisation(callback),
            referenceId
        );
    }
}
