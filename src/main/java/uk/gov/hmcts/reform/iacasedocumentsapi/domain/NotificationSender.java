package uk.gov.hmcts.reform.iacasedocumentsapi.domain;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

import java.io.InputStream;
import java.util.Map;


public interface NotificationSender<T> {

    String sendEmail(
        String templateId,
        String emailAddress,
        Map<String, String> personalisation,
        String reference,
        Callback<T> callback
    );

    String sendSms(
        String templateId,
        String phoneNumber,
        Map<String, String> personalisation,
        String reference,
        Callback<T> callback
    );

    String sendLetter(
        String templateId,
        String address,
        Map<String, String> personalisation,
        String reference,
        Callback<T> callback
    );

    String sendPrecompiledLetter(
        String reference,
        InputStream stream
    );
}
