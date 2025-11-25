package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.service.notify.NotificationClientException;

public interface EmailWithLinkNotificationPersonalisation extends BaseNotificationPersonalisation<AsylumCase> {
    default Map<String, Object> getPersonalisationForLink(Callback<AsylumCase> callback) {
        try {
            return getPersonalisationForLink(callback.getCaseDetails().getCaseData());
        } catch (NotificationClientException | IOException e) {
            // NotificationClient - if size is more than 2 MB.
            throw new IllegalArgumentException(e);
        }
    }

    default Map<String, Object> getPersonalisationForLink(AsylumCase asylumCase) throws IOException, NotificationClientException {
        return Collections.emptyMap();
    }

}
