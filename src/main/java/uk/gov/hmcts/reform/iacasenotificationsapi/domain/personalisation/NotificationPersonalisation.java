package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import java.util.Collections;
import java.util.Map;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;

public interface NotificationPersonalisation {

    default String getTemplateId(AsylumCase asylumCase) {
        return getTemplateId();
    }

    default String getTemplateId() {
        return "";
    }

    String getEmailAddress(AsylumCase asylumCase);

    String getReferenceId(Long caseId);

    default Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return Collections.emptyMap();
    }

    default Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        return getPersonalisation(callback.getCaseDetails().getCaseData());
    }
}