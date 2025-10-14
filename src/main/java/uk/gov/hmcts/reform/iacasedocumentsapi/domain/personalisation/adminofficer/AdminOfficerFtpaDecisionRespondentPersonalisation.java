package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.adminofficer;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isInternalCase;

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
public class AdminOfficerFtpaDecisionRespondentPersonalisation implements EmailNotificationPersonalisation, FtpaNotificationPersonalisationUtil {

    private final String applicationGrantedAdminTemplateId;
    private final String applicationGrantedAdminWithoutListingTemplateId;
    private final String applicationPartiallyGrantedAdminTemplateId;
    private final String applicationPartiallyGrantedAdminWithoutListingTemplateId;
    private final String ctscAdminFtpaDecisionEmailAddress;
    private final String upperTribunalPermissionApplicationsEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AdminOfficerFtpaDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.admin.email}") String applicationGrantedAdminTemplateId,
        @Value("${govnotify.template.applicationGranted.admin.withoutListing.email}") String applicationGrantedAdminWithoutListingTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.admin.email}") String applicationPartiallyGrantedAdminTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.admin.withoutListing.email}") String applicationPartiallyGrantedAdminWithoutListingTemplateId,

        @Value("${ctscAdminFtpaDecisionEmailAddress}") String ctscAdminFtpaDecisionEmailAddress,
        @Value("${upperTribunalPermissionApplicationsEmailAddress}") String upperTribunalPermissionApplicationsEmailAddress,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedAdminTemplateId = applicationGrantedAdminTemplateId;
        this.applicationGrantedAdminWithoutListingTemplateId = applicationGrantedAdminWithoutListingTemplateId;
        this.applicationPartiallyGrantedAdminTemplateId = applicationPartiallyGrantedAdminTemplateId;
        this.applicationPartiallyGrantedAdminWithoutListingTemplateId = applicationPartiallyGrantedAdminWithoutListingTemplateId;
        this.ctscAdminFtpaDecisionEmailAddress = ctscAdminFtpaDecisionEmailAddress;
        this.upperTribunalPermissionApplicationsEmailAddress = upperTribunalPermissionApplicationsEmailAddress;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        Optional<FtpaDecisionOutcomeType> ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class);

        if (!ftpaDecisionOutcomeType.isPresent()) {
            ftpaDecisionOutcomeType = Optional.ofNullable(asylumCase
                .read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
                .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present")));
        }

        if (ftpaDecisionOutcomeType.isPresent() && ftpaDecisionOutcomeType.get().toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return isInternalCase(asylumCase) ? applicationGrantedAdminWithoutListingTemplateId : applicationGrantedAdminTemplateId;
        } else {
            return isInternalCase(asylumCase) ? applicationPartiallyGrantedAdminWithoutListingTemplateId : applicationPartiallyGrantedAdminTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return ftpaRespondentLjRjDecision(asylumCase)
            .map(decision -> List.of(FTPA_GRANTED,FTPA_PARTIALLY_GRANTED).contains(decision)
                ? Set.of(upperTribunalPermissionApplicationsEmailAddress)
                : Set.of(ctscAdminFtpaDecisionEmailAddress))
            .orElseThrow(() -> new IllegalStateException("Respondent FTPA decision not present"));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_ADMIN_OFFICER_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return ImmutableMap.<String, String>builder()
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .putAll(personalisationProvider.getTribunalHeaderPersonalisation(asylumCase))
            .build();
    }
}
