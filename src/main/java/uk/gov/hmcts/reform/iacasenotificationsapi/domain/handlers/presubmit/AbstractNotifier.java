package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.NOTIFICATIONS_SENT;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AppealStateNotificationReference;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.*;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.NotificationIdAppender;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

public abstract  class AbstractNotifier {

    private GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;

    protected String homeOfficeTemplateId;
    protected String legalRepresentativeTemplateId;
    private final String homeOfficeEmailAddresses;
    private final AppealStateNotificationReference homeOfficeReference;
    private final AppealStateNotificationReference legalRepresentativeReference;
    private final NotificationSender notificationSender;
    private final NotificationIdAppender notificationIdAppender;
    private final AbstractPersonalisationFactory personalisationFactory;

    public AbstractNotifier(
            GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
            String homeOfficeEmailAddresses,
            AppealStateNotificationReference homeOfficeReference,
            AppealStateNotificationReference legalRepresentativeReference,
            NotificationSender notificationSender,
            NotificationIdAppender notificationIdAppender,
            AbstractPersonalisationFactory personalisationFactory
    ) {
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.homeOfficeEmailAddresses = homeOfficeEmailAddresses;
        this.homeOfficeReference = homeOfficeReference;
        this.legalRepresentativeReference = legalRepresentativeReference;
        this.notificationSender = notificationSender;
        this.notificationIdAppender = notificationIdAppender;
        this.personalisationFactory = personalisationFactory;
    }



    public void sendNotificationToHomeOffice(
            Callback<AsylumCase> callback,
            AsylumCase asylumCase
    ) {

        String reference =
                callback.getCaseDetails().getId()
                    + homeOfficeReference.state();

        Map<String, String> personalisation = personalisationFactory.create(asylumCase);

        String notificationId =
                notificationSender.sendEmail(
                        homeOfficeTemplateId,
                        homeOfficeEmailAddresses,
                        personalisation,
                        reference
                );

        setNotificationStatus(asylumCase, reference, notificationId);
    }

    public void sendNotificationToLegalRepresentative(
            Callback<AsylumCase> callback,
            AsylumCase asylumCase
    ) {

        String reference =
                callback.getCaseDetails().getId()
                    + legalRepresentativeReference.state();

        String legalRepresentativeEmailAddress =
                asylumCase
                        .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
                        .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present"));

        Map<String, String> personalisation = personalisationFactory.create(asylumCase);

        String notificationId =
                notificationSender.sendEmail(
                        legalRepresentativeTemplateId,
                        legalRepresentativeEmailAddress,
                        personalisation,
                        reference
                );

        setNotificationStatus(asylumCase, reference, notificationId);
    }

    private void setNotificationStatus(
            AsylumCase asylumCase,
            String reference,
            String notificationId
    ) {

        Optional<List<IdValue<String>>> maybeNotificationSent = asylumCase.read(NOTIFICATIONS_SENT);

        List<IdValue<String>> notificationSent = maybeNotificationSent
                .orElseGet(ArrayList::new);

        asylumCase.write(NOTIFICATIONS_SENT,
                notificationIdAppender.append(
                        notificationSent,
                        reference,
                        notificationId
                )
        );
    }

}
