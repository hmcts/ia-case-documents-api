package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_RESPONDENT_DECISION_OUTCOME_TYPE;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.FtpaDecisionOutcomeType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeFtpaApplicationDecisionRespondentPersonalisation  implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String homeOfficeEmailAddress;
    private final String applicationGrantedApplicantHomeOfficeTemplateId;
    private final String applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
    private final String applicationNotAdmittedApplicantHomeOfficeTemplateId;
    private final String applicationRefusedGrantedApplicantHomeOfficeTemplateId;
    private final String applicationReheardApplicantHomeHomeOfficeTemplateId;
    private final String applicationAllowedHomeOfficeTemplateId;
    private final String applicationDismissedHomeOfficeTemplateId;

    public HomeOfficeFtpaApplicationDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.applicant.homeOffice.email}") String applicationGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.applicant.homeOffice.email}") String applicationPartiallyGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.applicant.homeOffice.email}") String applicationNotAdmittedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationRefused.applicant.homeOffice.email}") String applicationRefusedGrantedApplicantHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationReheard.applicant.homeOffice.email}") String applicationReheardApplicantHomeHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationAllowed.homeOffice.email}") String applicationAllowedHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationDismissed.homeOffice.email}") String applicationDismissedHomeOfficeTemplateId,
        PersonalisationProvider personalisationProvider,
        @Value("${allowedAppealHomeOfficeEmailAddress}") String homeOfficeEmailAddress) {
        this.applicationGrantedApplicantHomeOfficeTemplateId = applicationGrantedApplicantHomeOfficeTemplateId;
        this.applicationPartiallyGrantedApplicantHomeOfficeTemplateId = applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
        this.applicationNotAdmittedApplicantHomeOfficeTemplateId = applicationNotAdmittedApplicantHomeOfficeTemplateId;
        this.applicationRefusedGrantedApplicantHomeOfficeTemplateId = applicationRefusedGrantedApplicantHomeOfficeTemplateId;
        this.applicationReheardApplicantHomeHomeOfficeTemplateId = applicationReheardApplicantHomeHomeOfficeTemplateId;
        this.applicationAllowedHomeOfficeTemplateId = applicationAllowedHomeOfficeTemplateId;
        this.applicationDismissedHomeOfficeTemplateId = applicationDismissedHomeOfficeTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present"));

        if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedApplicantHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedApplicantHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedApplicantHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_REHEARD.toString())) {
            return applicationReheardApplicantHomeHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_ALLOWED.toString())) {
            return applicationAllowedHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_DISMISSED.toString())) {
            return applicationDismissedHomeOfficeTemplateId;
        } else {
            return applicationNotAdmittedApplicantHomeOfficeTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(homeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase);
    }
}
