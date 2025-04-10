package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.ApplicationContextProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BaseNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.GovNotifyNotificationSender;

import static uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder.NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING;

@Slf4j
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

        ApplicationContextProvider.getApplicationContext().getBean(CustomerServicesProvider.class)
            .setCorrectEmail(asylumCase);

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
        final Callback<AsylumCase> callback
    ) {
        EmailNotificationPersonalisation emailNotificationPersonalisation = (EmailNotificationPersonalisation) personalisation;
        Set<String> subscriberEmails = emailNotificationPersonalisation.getRecipientsList(asylumCase);

        return subscriberEmails.stream()
            .filter(this::isValidEmailAddress)
            .map(email -> sendEmail(
                email,
                emailNotificationPersonalisation,
                referenceId,
                callback
            ))
            .filter(StringUtils::isNotBlank).collect(Collectors.toList());
    }

    protected String sendEmail(
        final String email,
        final EmailNotificationPersonalisation personalisation,
        final String referenceId,
        final Callback<AsylumCase> callback
    ) {
        String emailTemplateId = personalisation.getTemplateId() == null
            ?
            personalisation.getTemplateId(callback.getCaseDetails().getCaseData()) : personalisation.getTemplateId();

        return notificationSender.sendEmail(
            emailTemplateId,
            email,
            personalisation.getPersonalisation(callback),
            referenceId,
            callback
        );
    }

    private boolean isValidEmailAddress(String email) {
        if (email.equals(NO_EMAIL_ADDRESS_DECISION_WITHOUT_HEARING)) {
            log.warn("Invalid email address {}", email);
            return false;
        }

        return true;
    }
}
