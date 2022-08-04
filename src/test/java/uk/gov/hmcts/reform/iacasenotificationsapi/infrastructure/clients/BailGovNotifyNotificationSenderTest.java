package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients.helper.NotificationSenderHelper;
import uk.gov.service.notify.NotificationClientException;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class BailGovNotifyNotificationSenderTest {

    private static final org.slf4j.Logger LOG = getLogger(BailGovNotifyNotificationSender.class);

    private int deduplicateSendsWithinSeconds = 1;

    @Mock
    @Qualifier("BailClient")
    private RetryableNotificationClient notificationBailClient;

    @Mock private NotificationSenderHelper senderHelper;

    private String templateId = "a-b-c-d-e-f";
    private String emailAddress = "recipient@example.com";
    private String phoneNumber = "07123456789";
    private Map<String, String> personalisation = mock(Map.class);
    private String reference = "our-reference";

    private BailGovNotifyNotificationSender bailGovNotifyNotificationSender;

    @BeforeEach
    public void setUp() {
        bailGovNotifyNotificationSender =
            new BailGovNotifyNotificationSender(
                deduplicateSendsWithinSeconds,
                notificationBailClient,
                senderHelper
            );
    }

    @Test
    public void should_send_email_using_bail_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendEmail(
                templateId,
                emailAddress,
                personalisation,
                reference,
                notificationBailClient,
                deduplicateSendsWithinSeconds,
                LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
                bailGovNotifyNotificationSender.sendEmail(
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
                notificationBailClient,
                deduplicateSendsWithinSeconds,
                LOG
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }

    @Test
    public void should_send_sms_using_bail_gov_notify() throws NotificationClientException {

        final UUID expectedNotificationId = UUID.randomUUID();

        when(senderHelper.sendSms(
                templateId,
                phoneNumber,
                personalisation,
                reference,
                notificationBailClient,
                deduplicateSendsWithinSeconds,
                LOG
        )).thenReturn(String.valueOf(expectedNotificationId));

        String actualNotificationId =
                bailGovNotifyNotificationSender.sendSms(
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
                notificationBailClient,
                deduplicateSendsWithinSeconds,
                LOG
        );

        assertEquals(expectedNotificationId.toString(), actualNotificationId);
    }
}
