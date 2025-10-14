package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.utils.FtpaNotificationPersonalisationUtil;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;


@Service
public class AdminOfficerFtpaDecisionAppellantPersonalisation implements EmailNotificationPersonalisation, FtpaNotificationPersonalisationUtil {

    private final String applicationGrantedAdminTemplateId;
    private final String applicationPartiallyGrantedAdminTemplateId;
    private final String ctscAdminFtpaDecisionEmailAddress;
    private final String upperTribunalPermissionApplicationsEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AdminOfficerFtpaDecisionAppellantPersonalisation(
        @Value("${govnotify.template.applicationGranted.admin.email}") String applicationGrantedAdminTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.admin.email}") String applicationPartiallyGrantedAdminTemplateId,
        @Value("${ctscAdminFtpaDecisionEmailAddress}") String ctscAdminFtpaDecisionEmailAddress,
        @Value("${upperTribunalPermissionApplicationsEmailAddress}") String upperTribunalPermissionApplicationsEmailAddress,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedAdminTemplateId = applicationGrantedAdminTemplateId;
        this.applicationPartiallyGrantedAdminTemplateId = applicationPartiallyGrantedAdminTemplateId;
        this.ctscAdminFtpaDecisionEmailAddress = ctscAdminFtpaDecisionEmailAddress;
        this.upperTribunalPermissionApplicationsEmailAddress = upperTribunalPermissionApplicationsEmailAddress;
        this.personalisationProvider = personalisationProvider;

    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        if (!ftpaDecisionOutcomeType.isPresent()) {
            ftpaDecisionOutcomeType = Optional.ofNullable(asylumCase
                .read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present")));
        }

        if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedAdminTemplateId;
        } else {
            return applicationPartiallyGrantedAdminTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return ftpaAppellantLjRjDecision(asylumCase)
            .map(decision -> List.of(FTPA_GRANTED,FTPA_PARTIALLY_GRANTED).contains(decision)
                ? Set.of(upperTribunalPermissionApplicationsEmailAddress)
                : Set.of(ctscAdminFtpaDecisionEmailAddress))
            .orElseThrow(() -> new IllegalStateException("Appellant FTPA decision not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_ADMIN_OFFICER_APPELLANT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return ImmutableMap.<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .build();
    }

}
