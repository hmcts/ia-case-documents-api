package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;

@RunWith(MockitoJUnitRunner.class)
public class NotificationGeneratorTest {

    @Mock EmailNotificationPersonalisation emailNotificationPersonalisation;
    @Mock EmailNotificationPersonalisation emailNotificationPersonalisation1;
    @Mock SmsNotificationPersonalisation smsNotificationPersonalisation1;
    @Mock SmsNotificationPersonalisation smsNotificationPersonalisation2;
    @Mock NotificationSender notificationSender;
    @Spy NotificationIdAppender notificationIdAppender;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    private List<EmailNotificationPersonalisation> repEmailNotificationPersonalisationList;
    private List<EmailNotificationPersonalisation> aipEmailNotificationPersonalisationList;
    private List<SmsNotificationPersonalisation> aipSmsNotificationPersonalisationList;

    private NotificationGenerator notificationGenerator;

    private Long caseId = 12345L;

    private String templateId1 = "templateId1";
    private String templateId2 = "templateId1";

    private String refId1 = "refId1";
    private String refId2 = "refId2";

    private String emailAddress1 = "email1@example.com";
    private String emailAddress2 = "email2@example.com";

    private String phoneNumber1 = "07123456789";
    private String phoneNumber2 = "07123456780";

    private Map<String, String> personalizationMap1 = emptyMap();
    private Map<String, String> personalizationMap2 = emptyMap();

    private List<IdValue<String>> notificationsSent = newArrayList();

    private String notificationId1 = "notificationId1";
    private String notificationId2 = "notificationId2";

    @Before
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        when(emailNotificationPersonalisation.getReferenceId(caseId)).thenReturn(refId1);
        when(emailNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId2);

        when(emailNotificationPersonalisation.getTemplateId()).thenReturn(templateId1);
        when(emailNotificationPersonalisation1.getTemplateId()).thenReturn(templateId2);

        when(emailNotificationPersonalisation.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(emailNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendEmail(templateId1, emailAddress1, personalizationMap1, refId1)).thenReturn(notificationId1);
        when(notificationSender.sendEmail(templateId2, emailAddress2, personalizationMap2, refId2)).thenReturn(notificationId2);

        when(smsNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId1);
        when(smsNotificationPersonalisation2.getReferenceId(caseId)).thenReturn(refId2);

        when(smsNotificationPersonalisation1.getTemplateId()).thenReturn(templateId1);
        when(smsNotificationPersonalisation2.getTemplateId()).thenReturn(templateId2);

        when(smsNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(smsNotificationPersonalisation2.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendSms(templateId1, phoneNumber1, personalizationMap1, refId1)).thenReturn(notificationId1);
        when(notificationSender.sendSms(templateId2, phoneNumber2, personalizationMap2, refId2)).thenReturn(notificationId2);

        when(notificationIdAppender.append(notificationsSent, refId1, notificationId1)).thenReturn(notificationsSent);
        when(notificationIdAppender.append(notificationsSent, refId2, notificationId2)).thenReturn(notificationsSent);

        repEmailNotificationPersonalisationList = newArrayList(emailNotificationPersonalisation, emailNotificationPersonalisation1);
        aipEmailNotificationPersonalisationList = newArrayList(emailNotificationPersonalisation, emailNotificationPersonalisation1);
        aipSmsNotificationPersonalisationList = newArrayList(smsNotificationPersonalisation1, smsNotificationPersonalisation2);

    }

    @Test
    public void should_send_notification_for_each_email_personalisation() {
        notificationGenerator = new EmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress1));
        when(emailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_Aip_notification_emails_for_each_email_personalisation_using_the_subscriber_mode() {
        notificationGenerator = new EmailNotificationGenerator(aipEmailNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress1));
        when(emailNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(emailAddress2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_Aip_notification_Sms_for_each_personalisation_using_the_subscriber_mode() {
        notificationGenerator = new SmsNotificationGenerator(aipSmsNotificationPersonalisationList, notificationSender, notificationIdAppender);

        when(smsNotificationPersonalisation1.getRecipientsList(asylumCase)).thenReturn(singleton(phoneNumber1));
        when(smsNotificationPersonalisation2.getRecipientsList(asylumCase)).thenReturn(singleton(phoneNumber2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendSms(templateId1, phoneNumber1, personalizationMap1, refId1);
        verify(notificationSender).sendSms(templateId2, phoneNumber2, personalizationMap2, refId2);

        verify(notificationIdAppender).appendAll(asylumCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(asylumCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_email_personalisation_list_empty() {
        notificationGenerator = new EmailNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_sms_personalisation_list_empty() {
        notificationGenerator = new SmsNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }
}
