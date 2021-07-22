package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.service.notify.*;

@ExtendWith(MockitoExtension.class)
class RetryableNotificationClientTest {


    @Mock
    private NotificationClientApi notificationClient;
    @Mock
    private SendEmailResponse sendEmailResponse;
    @Mock
    private SendSmsResponse sendSmsResponse;
    @Mock
    private Notification notification;

    private RetryableNotificationClient retryableNotificationClient;

    @BeforeEach
    void setup() {

        retryableNotificationClient = new RetryableNotificationClient(notificationClient);
    }

    @Test
    void should_retry_once_when_sending_email_failed() throws NotificationClientException {
        when(notificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(new NotificationClientException("some exception"))
            .thenReturn(sendEmailResponse);

        retryableNotificationClient.sendEmail(anyString(), anyString(), anyMap(), anyString());

        verify(notificationClient, times(2)).sendEmail(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void should_retry_once_when_sending_sms_failed() throws NotificationClientException {
        when(notificationClient.sendSms(anyString(), anyString(), anyMap(), anyString()))
            .thenThrow(new NotificationClientException("some exception"))
            .thenReturn(sendSmsResponse);

        retryableNotificationClient.sendSms(anyString(), anyString(), anyMap(), anyString());

        verify(notificationClient, times(2)).sendSms(anyString(), anyString(), anyMap(), anyString());
    }

    @Test
    void should_retry_once_when_getting_notification_failed() throws NotificationClientException {
        when(notificationClient.getNotificationById(anyString()))
            .thenThrow(new NotificationClientException("some exception"))
            .thenReturn(notification);

        retryableNotificationClient.getNotificationById(anyString());

        verify(notificationClient, times(2)).getNotificationById(anyString());
    }
}
