package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

public interface BaseNotificationPersonalisation {

    String getReferenceId(Long caseId);

    default String getTemplateId() {
        return null;
    }

    default String getTemplateId(AsylumCase asylumCase) {
        return null;
    }

    Set<String> getRecipientsList(AsylumCase asylumCase);

    default Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Collections.emptyMap();
    }

    default Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        return getPersonalisation(callback.getCaseDetails().getCaseData());
    }
}
