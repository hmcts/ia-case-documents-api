package uk.gov.hmcts.reform.iacasenotificationsapi.domain.handlers.presubmit;

import java.util.Map;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;

public interface CaseEmailNotifier {

    String getEmailAddress(AsylumCase asylumCase);

    Map<String, String> getPersonalisation(AsylumCase asylumCase);
}
