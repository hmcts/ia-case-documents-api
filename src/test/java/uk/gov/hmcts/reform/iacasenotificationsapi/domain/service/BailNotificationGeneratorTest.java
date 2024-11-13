package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCaseFieldDefinition.NOTIFICATIONS_SENT;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailEmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.BailSmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.BailGovNotifyNotificationSender;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class BailNotificationGeneratorTest {

    @Mock
    BailEmailNotificationPersonalisation emailNotificationPersonalisation;
    @Mock
    BailEmailNotificationPersonalisation emailNotificationPersonalisation1;
    @Mock
    BailSmsNotificationPersonalisation smsNotificationPersonalisation1;
    @Mock
    BailSmsNotificationPersonalisation smsNotificationPersonalisation2;
    @Mock
    BailGovNotifyNotificationSender notificationSender;
    @Spy
    BailNotificationIdAppender notificationIdAppender;
    @Mock
    Callback<BailCase> callback;
    @Mock
    CaseDetails<BailCase> caseDetails;
    @Mock
    BailCase bailCase;

    private List<BailEmailNotificationPersonalisation> repEmailNotificationPersonalisationList;
    private List<BailEmailNotificationPersonalisation> adminEmailNotificationPersonalisationList;
    private List<BailSmsNotificationPersonalisation> applicantSmsNotificationPersonalisationList;

    private BailNotificationGenerator notificationGenerator;

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

    @BeforeEach
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(bailCase.read(NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        when(emailNotificationPersonalisation.getReferenceId(caseId)).thenReturn(refId1);
        when(emailNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId2);

        when(emailNotificationPersonalisation.getTemplateId()).thenReturn(templateId1);
        when(emailNotificationPersonalisation1.getTemplateId()).thenReturn(templateId2);

        when(emailNotificationPersonalisation.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(emailNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback))
            .thenReturn(notificationId1);
        when(notificationSender.sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback))
            .thenReturn(notificationId2);

        when(smsNotificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId1);
        when(smsNotificationPersonalisation2.getReferenceId(caseId)).thenReturn(refId2);

        when(smsNotificationPersonalisation1.getTemplateId()).thenReturn(templateId1);
        when(smsNotificationPersonalisation2.getTemplateId()).thenReturn(templateId2);

        when(smsNotificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(smsNotificationPersonalisation2.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendSms(templateId1, phoneNumber1, personalizationMap1, refId1, callback))
            .thenReturn(notificationId1);
        when(notificationSender.sendSms(templateId2, phoneNumber2, personalizationMap2, refId2, callback))
            .thenReturn(notificationId2);

        when(notificationIdAppender.append(notificationsSent, refId1, notificationId1)).thenReturn(notificationsSent);
        when(notificationIdAppender.append(notificationsSent, refId2, notificationId2)).thenReturn(notificationsSent);

        repEmailNotificationPersonalisationList =
            newArrayList(emailNotificationPersonalisation, emailNotificationPersonalisation1);
        adminEmailNotificationPersonalisationList =
            newArrayList(emailNotificationPersonalisation, emailNotificationPersonalisation1);
        applicantSmsNotificationPersonalisationList =
            newArrayList(smsNotificationPersonalisation1, smsNotificationPersonalisation2);

    }

    @Test
    public void should_send_notification_for_each_email_personalisation() {
        notificationGenerator =
            new BailEmailNotificationGenerator(repEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(bailCase)).thenReturn(singleton(emailAddress1));
        when(emailNotificationPersonalisation1.getRecipientsList(bailCase)).thenReturn(singleton(emailAddress2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(bailCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(bailCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(bailCase, times(2)).write(NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_admin_notification_emails_for_each_email_personalisation() {
        notificationGenerator =
            new BailEmailNotificationGenerator(adminEmailNotificationPersonalisationList, notificationSender,
                notificationIdAppender);

        when(emailNotificationPersonalisation.getRecipientsList(bailCase)).thenReturn(singleton(emailAddress1));
        when(emailNotificationPersonalisation1.getRecipientsList(bailCase)).thenReturn(singleton(emailAddress2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(bailCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(bailCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(bailCase, times(2)).write(NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_send_applicant_notification_Sms_for_each_personalisation_using_the_subscriber_mode() {
        notificationGenerator = new BailSmsNotificationGenerator(applicantSmsNotificationPersonalisationList, notificationSender,
            notificationIdAppender);

        when(smsNotificationPersonalisation1.getRecipientsList(bailCase)).thenReturn(singleton(phoneNumber1));
        when(smsNotificationPersonalisation2.getRecipientsList(bailCase)).thenReturn(singleton(phoneNumber2));

        notificationGenerator.generate(callback);

        verify(notificationSender).sendSms(templateId1, phoneNumber1, personalizationMap1, refId1, callback);
        verify(notificationSender).sendSms(templateId2, phoneNumber2, personalizationMap2, refId2, callback);

        verify(notificationIdAppender).appendAll(bailCase, refId1, Collections.singletonList(notificationId1));
        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).appendAll(bailCase, refId2, Collections.singletonList(notificationId2));
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(bailCase, times(2)).write(NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_email_personalisation_list_empty() {
        notificationGenerator = new BailEmailNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(bailCase, times(0)).write(NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_sms_personalisation_list_empty() {
        notificationGenerator = new BailSmsNotificationGenerator(emptyList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyNoInteractions(notificationSender);
        verifyNoInteractions(notificationIdAppender);

        verify(bailCase, times(0)).write(NOTIFICATIONS_SENT, notificationsSent);
    }
}
