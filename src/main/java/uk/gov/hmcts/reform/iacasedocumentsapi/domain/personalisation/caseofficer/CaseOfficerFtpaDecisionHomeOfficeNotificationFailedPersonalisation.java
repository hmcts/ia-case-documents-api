package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.caseofficer;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class CaseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation implements EmailNotificationPersonalisation {

    private final String ftpaDecisionCaseOfficerNotificationFailedTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final FeatureToggler featureToggler;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public CaseOfficerFtpaDecisionHomeOfficeNotificationFailedPersonalisation(
            @NotNull(message = "appealOutcomeHomeOfficeNotificationFailedTemplateId cannot be null")
            @Value("${govnotify.template.ftpaDecisionHomeOfficeNotificationFailed.caseOfficer.email}") String appealOutcomeHomeOfficeNotificationFailedTemplateId,
            PersonalisationProvider personalisationProvider,
            EmailAddressFinder emailAddressFinder,
            FeatureToggler featureToggler) {

        this.ftpaDecisionCaseOfficerNotificationFailedTemplateId = appealOutcomeHomeOfficeNotificationFailedTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
        this.featureToggler = featureToggler;
    }

    @Override
    public String getTemplateId() {
        return ftpaDecisionCaseOfficerNotificationFailedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return featureToggler.getValue("tcw-notifications-feature", true)
                ? Collections.singleton(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase))
                : Collections.emptySet();
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_DECISION_HO_NOTIFICATION_FAILED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .build();
    }
}
