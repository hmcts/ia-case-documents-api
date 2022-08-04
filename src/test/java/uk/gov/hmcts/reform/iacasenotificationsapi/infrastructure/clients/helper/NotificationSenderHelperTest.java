package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.NotificationServiceResponseException;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.RetryableNotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;
import uk.gov.service.notify.SendSmsResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class NotificationSenderHelperTest {

    private static final org.slf4j.Logger LOG = getLogger(NotificationSenderHelperTest.class);

    @Mock
    private RetryableNotificationClient notificationClient;

    private NotificationSenderHelper senderHelper = new NotificationSenderHelper();

    private int deduplicateSendsWithinSeconds = 1;
    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private Map<String, String> personalisation = mock(Map.class);
    private String reference = "our-reference";

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
                senderHelper.sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );

        final String actualNotificationId2 =
                senderHelper.sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );

        final String actualNotificationIdForOther =
                senderHelper.sendEmail(
                        templateId,
                        otherEmailAddress,
                        personalisation,
                        otherReference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );


        assertEquals(expectedNotificationId.toString(), actualNotificationId1);
        assertEquals(expectedNotificationId.toString(), actualNotificationId2);
        assertEquals(expectedNotificationIdForOther.toString(), actualNotificationIdForOther);

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue(true, "We expect this to timeout");
        }

        final String actualNotificationId3 =
                senderHelper.sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
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
    public void wrap_gov_notify_email_exceptions() throws NotificationClientException {

        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendEmail(
                    templateId,
                    emailAddress,
                    personalisation,
                    reference
            );

        assertThatThrownBy(() ->
                senderHelper.sendEmail(
                        templateId,
                        emailAddress,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
                .hasMessage("Failed to send email using GovNotify")
                .hasCause(underlyingException);

    }

    @Test
    public void should_not_send_duplicate_sms_in_short_space_of_time() throws NotificationClientException {

        final String otherPhoneNumber = "07123456780";
        final String otherReference = "1111_SOME_OTHER_NOTIFICATION";

        final UUID expectedNotificationId = UUID.randomUUID();
        final UUID expectedNotificationIdForOther = UUID.randomUUID();

        SendSmsResponse sendSmsResponse = mock(SendSmsResponse.class);
        SendSmsResponse sendSmsResponseForOther = mock(SendSmsResponse.class);

        when(notificationClient.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
        )).thenReturn(sendSmsResponse);

        when(notificationClient.sendSms(
                templateId,
                otherPhoneNumber,
                personalisation,
                otherReference
        )).thenReturn(sendSmsResponseForOther);

        when(sendSmsResponse.getNotificationId()).thenReturn(expectedNotificationId);
        when(sendSmsResponseForOther.getNotificationId()).thenReturn(expectedNotificationIdForOther);

        final String actualNotificationId1 =
                senderHelper.sendSms(
                        templateId,
                        phoneNumber,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );

        final String actualNotificationId2 =
                senderHelper.sendSms(
                        templateId,
                        phoneNumber,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );

        final String actualNotificationIdForOther =
                senderHelper.sendSms(
                        templateId,
                        otherPhoneNumber,
                        personalisation,
                        otherReference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );

        assertEquals(expectedNotificationId.toString(), actualNotificationId1);
        assertEquals(expectedNotificationId.toString(), actualNotificationId2);
        assertEquals(expectedNotificationIdForOther.toString(), actualNotificationIdForOther);

        try {
            await().atMost(2, TimeUnit.SECONDS).until(() -> false);
        } catch (ConditionTimeoutException e) {
            assertTrue(true, "We expect this to timeout");
        }

        final String actualNotificationId3 =
                senderHelper.sendSms(
                        templateId,
                        phoneNumber,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                );

        assertEquals(expectedNotificationId.toString(), actualNotificationId3);

        verify(notificationClient, times(2)).sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
        );

        verify(notificationClient, times(1)).sendSms(
                templateId,
                otherPhoneNumber,
                personalisation,
                otherReference
        );
    }

    @Test
    public void wrap_gov_notify_sms_exceptions() throws NotificationClientException {
        NotificationClientException underlyingException = mock(NotificationClientException.class);

        doThrow(underlyingException)
            .when(notificationClient)
            .sendSms(
                    templateId,
                    phoneNumber,
                    personalisation,
                    reference
            );

        assertThatThrownBy(() ->
                senderHelper.sendSms(
                        templateId,
                        phoneNumber,
                        personalisation,
                        reference,
                        notificationClient,
                        deduplicateSendsWithinSeconds,
                        LOG
                )
        ).isExactlyInstanceOf(NotificationServiceResponseException.class)
                .hasMessage("Failed to send sms using GovNotify")
                .hasCause(underlyingException);

    }
}
