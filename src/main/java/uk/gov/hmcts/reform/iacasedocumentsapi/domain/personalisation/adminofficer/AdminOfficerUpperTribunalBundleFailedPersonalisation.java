package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class AdminOfficerUpperTribunalBundleFailedPersonalisation implements EmailNotificationPersonalisation {

    private final String upperTribunalBundleFailedAdminOfficerTemplateId;
    private final String upperTribunalEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AdminOfficerUpperTribunalBundleFailedPersonalisation(
        @Value("${govnotify.template.upperTribunalBundleFailed.adminOfficer.email}") String upperTribunalBundleFailedAdminOfficerTemplateId,
        @Value("${upperTribunalPermissionApplicationsEmailAddress}") String upperTribunalEmailAddress,
        PersonalisationProvider personalisationProvider
    ) {
        this.upperTribunalBundleFailedAdminOfficerTemplateId = upperTribunalBundleFailedAdminOfficerTemplateId;
        this.upperTribunalEmailAddress = upperTribunalEmailAddress;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return upperTribunalBundleFailedAdminOfficerTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(upperTribunalEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPPER_TRIBUNAL_BUNDLE_FAILED_ADMIN_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return
            ImmutableMap
                .<String, String>builder()
                .putAll(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .build();
    }
}
