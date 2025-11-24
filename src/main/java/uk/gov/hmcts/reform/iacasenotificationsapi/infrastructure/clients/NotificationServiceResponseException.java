package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

public class NotificationServiceResponseException extends RuntimeException {

    public NotificationServiceResponseException(
        String message,
        Throwable cause) {

        super(message, cause);

    }

}
