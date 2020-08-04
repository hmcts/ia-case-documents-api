package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.adminofficer;

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
public class AdminOfficerFtpaDecisionRespondentPersonalisation implements EmailNotificationPersonalisation {

    private final String applicationGrantedAdminTemplateId;
    private final String applicationPartiallyGrantedAdminTemplateId;
    private final String reviewHearingRequirementsAdminOfficerEmailAddress;
    private final PersonalisationProvider personalisationProvider;

    public AdminOfficerFtpaDecisionRespondentPersonalisation(
        @Value("${govnotify.template.applicationGranted.admin.email}") String applicationGrantedAdminTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.admin.email}") String applicationPartiallyGrantedAdminTemplateId,
        @Value("${reviewHearingRequirementsAdminOfficerEmailAddress}") String reviewHearingRequirementsAdminOfficerEmailAddress,
        PersonalisationProvider personalisationProvider) {
        this.applicationGrantedAdminTemplateId = applicationGrantedAdminTemplateId;
        this.applicationPartiallyGrantedAdminTemplateId = applicationPartiallyGrantedAdminTemplateId;
        this.reviewHearingRequirementsAdminOfficerEmailAddress = reviewHearingRequirementsAdminOfficerEmailAddress;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaAppellantDecisionOutcomeType is not present"));

        if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedAdminTemplateId;
        } else {
            return applicationPartiallyGrantedAdminTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(reviewHearingRequirementsAdminOfficerEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_ADMIN_OFFICER_RESPONDENT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);
    }
}
