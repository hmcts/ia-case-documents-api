package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

public class NotificationServiceResponseException extends RuntimeException {

    public NotificationServiceResponseException(
        String message,
        Throwable cause) {

        super(message, cause);

    }

}
