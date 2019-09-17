package uk.gov.hmcts.reform.iacasenotificationsapi.domain.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.NotificationSender;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.NotificationPersonalisation;

@RunWith(MockitoJUnitRunner.class)
public class NotificationGeneratorTest {

    @Mock NotificationPersonalisation notificationPersonalisation1;
    @Mock NotificationPersonalisation notificationPersonalisation2;
    @Mock NotificationSender notificationSender;
    @Mock NotificationIdAppender notificationIdAppender;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;
    @Mock AsylumCase asylumCase;

    private List<NotificationPersonalisation> notificationPersonalisationList;

    private NotificationGenerator notificationGenerator;

    private Long caseId = 12345L;

    private String templateId1 = "templateId1";
    private String templateId2 = "templateId1";

    private String refId1 = "refId1";
    private String refId2 = "refId2";

    private String emailAddress1 = "email1@example.com";
    private String emailAddress2 = "email2@example.com";

    private Map<String, String> personalizationMap1 = Collections.emptyMap();
    private Map<String, String> personalizationMap2 = Collections.emptyMap();

    private List<IdValue<String>> notificationsSent = newArrayList();

    private String notificationId1 = "notificationId1";
    private String notificationId2 = "notificationId2";

    @Before
    public void setup() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);

        when(asylumCase.read(AsylumCaseDefinition.NOTIFICATIONS_SENT)).thenReturn(Optional.of(notificationsSent));

        when(notificationPersonalisation1.getReferenceId(caseId)).thenReturn(refId1);
        when(notificationPersonalisation2.getReferenceId(caseId)).thenReturn(refId2);

        when(notificationPersonalisation1.getEmailAddress(asylumCase)).thenReturn(emailAddress1);
        when(notificationPersonalisation2.getEmailAddress(asylumCase)).thenReturn(emailAddress2);

        when(notificationPersonalisation1.getTemplateId()).thenReturn(templateId1);
        when(notificationPersonalisation2.getTemplateId()).thenReturn(templateId2);

        when(notificationPersonalisation1.getPersonalisation(callback)).thenReturn(personalizationMap1);
        when(notificationPersonalisation2.getPersonalisation(callback)).thenReturn(personalizationMap2);

        when(notificationSender.sendEmail(templateId1, emailAddress1, personalizationMap1, refId1)).thenReturn(notificationId1);
        when(notificationSender.sendEmail(templateId2, emailAddress2, personalizationMap2, refId2)).thenReturn(notificationId2);

        when(notificationIdAppender.append(notificationsSent, refId1, notificationId1)).thenReturn(notificationsSent);
        when(notificationIdAppender.append(notificationsSent, refId2, notificationId2)).thenReturn(notificationsSent);

        notificationPersonalisationList = newArrayList(notificationPersonalisation1, notificationPersonalisation2);

        notificationGenerator = new NotificationGenerator(notificationPersonalisationList, notificationSender, notificationIdAppender);
    }

    @Test
    public void should_send_notification_for_each_personalisation() {
        notificationGenerator.generate(callback);

        verify(notificationSender).sendEmail(templateId1, emailAddress1, personalizationMap1, refId1);
        verify(notificationSender).sendEmail(templateId2, emailAddress2, personalizationMap2, refId2);

        verify(notificationIdAppender).append(notificationsSent, refId1, notificationId1);
        verify(notificationIdAppender).append(notificationsSent, refId2, notificationId2);

        verify(asylumCase, times(2)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }

    @Test
    public void should_not_send_notification_when_personalisation_list_empty() {
        notificationGenerator = new NotificationGenerator(newArrayList(), notificationSender, notificationIdAppender);
        notificationGenerator.generate(callback);

        verifyZeroInteractions(notificationSender);

        verifyZeroInteractions(notificationIdAppender);

        verify(asylumCase, times(0)).write(AsylumCaseDefinition.NOTIFICATIONS_SENT, notificationsSent);
    }
}