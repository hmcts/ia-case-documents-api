package uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.clients;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class NotificationServiceResponseException extends UnknownErrorCodeException {

    public NotificationServiceResponseException(
        AlertLevel alertLevel,
        String message,
        Throwable cause) {

        super(alertLevel, message, cause);

    }

}
