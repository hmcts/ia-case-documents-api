package uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.clients;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class DocumentServiceResponseException extends UnknownErrorCodeException {

    public DocumentServiceResponseException(AlertLevel alertLevel, String message) {
        super(alertLevel, message);
    }

    public DocumentServiceResponseException(AlertLevel alertLevel, String message, Throwable cause) {
        super(alertLevel, message, cause);
    }

}
