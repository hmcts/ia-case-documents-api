package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelper;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class GovNotifyNotificationSenderTest {

    private static final org.slf4j.Logger LOG = getLogger(GovNotifyNotificationSender.class);

    private int deduplicateSendsWithinSeconds = 1;
    @Mock
    private RetryableNotificationClient notificationClient;

    @Mock private NotificationSenderHelper senderHelper;

    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private Map<String, String> personalisation = mock(Map.class);
    private Map<String, Object> personalisationWithLink = mock(Map.class);
    private String reference = "our-reference";

    private GovNotifyNotificationSender govNotifyNotificationSender;

    @BeforeEach
    public void setUp() {
        govNotifyNotificationSender =
            new GovNotifyNotificationSender(
                deduplicateSendsWithinSeconds,
                notificationClient,
                senderHelper
            );
    }

    @Test
    public void should_send_email_using_appeal_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference
            );

        verify(senderHelper, times(1)).sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_sms_using_appeal_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
            govNotifyNotificationSender.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference
            );

        verify(senderHelper, times(1)).sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_email_using_appeal_gov_notify_with_link() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
                govNotifyNotificationSender.sendEmailWithLink(
                        templateId,
                        emailAddress,
                        personalisationWithLink,
                        reference
                );

        verify(senderHelper, times(1)).sendEmailWithLink(
                templateId,
                emailAddress,
                personalisationWithLink,
                reference,
                notificationClient,
                deduplicateSendsWithinSeconds,
                LOG
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }
}
