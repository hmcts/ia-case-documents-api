package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class AdminOfficerUpperTribunalBundleFailedPersonalisation implements EmailNotificationPersonalisation {

    private final String upperTribunalBundleFailedAdminOfficerTemplateId;
    private final String ctscAdminFtpaDecisionEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    public AdminOfficerUpperTribunalBundleFailedPersonalisation(
        @Value("${govnotify.template.upperTribunalBundleFailed.adminOfficer.email}") String upperTribunalBundleFailedAdminOfficerTemplateId,
        @Value("${ctscAdminFtpaDecisionEmailAddress}") String ctscAdminFtpaDecisionEmailAddress,
        PersonalisationProvider personalisationProvider
    ) {
        this.upperTribunalBundleFailedAdminOfficerTemplateId = upperTribunalBundleFailedAdminOfficerTemplateId;
        this.ctscAdminFtpaDecisionEmailAddress = ctscAdminFtpaDecisionEmailAddress;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return upperTribunalBundleFailedAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(ctscAdminFtpaDecisionEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPPER_TRIBUNAL_BUNDLE_FAILED_ADMIN_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);
    }
}
