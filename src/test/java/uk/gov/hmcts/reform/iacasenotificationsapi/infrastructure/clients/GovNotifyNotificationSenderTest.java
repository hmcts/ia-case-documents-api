package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.UnrecoverableException;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class GovNotifyNotificationSenderTest {

    private int deduplicateSendsWithinSeconds = 1;
    @Mock private NotificationClient notificationClient;

    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private Map<String, String> personalisation = mock(Map.class);
    private String reference = "our-reference";

    private GovNotifyNotificationSender govNotifyNotificationSender;

    @Before
    public void setUp() {
        govNotifyNotificationSender =
            new GovNotifyNotificationSender(
                deduplicateSendsWithinSeconds,
                notificationClient
            );
    }

    @Test
    public void should_send_email_using_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);

        when(notificationClient.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        )).thenReturn(sendEmailResponse);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);

        String actualNotificationId =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_not_send_duplicate_emails_in_short_space_of_time() throws NotificationClientException {

        final String otherEmailAddress = "foo@bar.com";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendEmailResponse sendEmailResponse = mock(SendEmailResponse.class);
        SendEmailResponse sendEmailResponseForOther = mock(SendEmailResponse.class);

        when(notificationClient.sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        )).thenReturn(sendEmailResponse);

        when(notificationClient.sendEmail(
            templateId,
            otherEmailAddress,
            personalisation,
            otherReference
        )).thenReturn(sendEmailResponseForOther);

        when(sendEmailResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendEmailResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        final String actualNotificationId2 =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        final String actualNotificationIdForOther =
            govNotifyNotificationSender.sendEmail(
                templateId,
                otherEmailAddress,
                personalisation,
                otherReference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId1);
        assertEquals(expectedNotificationId.toString(), actualNotificationId2);
        assertEquals(expectedNotificationIdForOther.toString(), actualNotificationIdForOther);

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue("We expect this to timeout", true);
        }

        final String actualNotificationId3 =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendEmail(
            templateId,
            emailAddress,
            personalisation,
            reference
        );

        verify(notificationClient, times(1)).sendEmail(
            templateId,
            otherEmailAddress,
            personalisation,
            otherReference
        );
    }

    @Test
    public void wrap_gov_notify_exceptions() throws NotificationClientException {

        doThrow(NotificationClientException.class)
            .when(notificationClient)
            .sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        assertThatThrownBy(() ->
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            )
        ).hasMessage("Failed to send email using GovNotify")
            .isExactlyInstanceOf(UnrecoverableException.class);
    }
}
