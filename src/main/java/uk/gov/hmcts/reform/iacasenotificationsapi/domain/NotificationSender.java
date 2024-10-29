package uk.gov.hmcts.reform.iacasenotificationsapi.domain;

import java.io.InputStream;
import java.util.Map;


public interface NotificationSender {

    String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference
    );

    String sendSms(
        String templateId,
        String phoneNumber,
        Map<String, String> personalisation,
        String reference
    );

    String sendLetter(
        String templateId,
        String address,
        Map<String, String> personalisation,
        String reference
    );

    String sendPrecompiledLetter(
        String reference,
        InputStream stream
    );
}
