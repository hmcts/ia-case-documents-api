package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.FTPA_APPELLANT_DECISION_OUTCOME_TYPE;

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
public class HomeOfficeFtpaApplicationDecisionAppellantPersonalisation implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String homeOfficeEmailAddress;
    private final String applicationGrantedOtherPartyHomeOfficeTemplateId;
    private final String applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId;
    private final String applicationNotAdmittedOtherPartyHomeOfficeTemplateId;
    private final String applicationRefusedGrantedOtherPartyHomeOfficeTemplateId;

    public HomeOfficeFtpaApplicationDecisionAppellantPersonalisation(
        @Value("${govnotify.template.applicationGranted.otherParty.homeOffice.email}") String applicationGrantedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationPartiallyGranted.otherParty.homeOffice.email}") String applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationNotAdmitted.otherParty.homeOffice.email}") String applicationNotAdmittedOtherPartyHomeOfficeTemplateId,
        @Value("${govnotify.template.applicationRefused.otherParty.homeOffice.email}") String applicationRefusedGrantedOtherPartyHomeOfficeTemplateId,
        PersonalisationProvider personalisationProvider,
        @Value("${allowedAppealHomeOfficeEmailAddress}") String homeOfficeEmailAddress) {
        this.applicationGrantedOtherPartyHomeOfficeTemplateId = applicationGrantedOtherPartyHomeOfficeTemplateId;
        this.applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId = applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId;
        this.applicationNotAdmittedOtherPartyHomeOfficeTemplateId = applicationNotAdmittedOtherPartyHomeOfficeTemplateId;
        this.applicationRefusedGrantedOtherPartyHomeOfficeTemplateId = applicationRefusedGrantedOtherPartyHomeOfficeTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.homeOfficeEmailAddress = homeOfficeEmailAddress;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        FtpaDecisionOutcomeType ftpaDecisionOutcomeType = asylumCase
            .read(FTPA_APPELLANT_DECISION_OUTCOME_TYPE, FtpaDecisionOutcomeType.class)
            .orElseThrow(() -> new IllegalStateException("ftpaRespondentDecisionOutcomeType is not present"));

        if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_GRANTED.toString())) {
            return applicationGrantedOtherPartyHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_PARTIALLY_GRANTED.toString())) {
            return applicationPartiallyGrantedOtherPartyHomeOfficeTemplateId;
        } else if (ftpaDecisionOutcomeType.toString().equals(FtpaDecisionOutcomeType.FTPA_REFUSED.toString())) {
            return applicationRefusedGrantedOtherPartyHomeOfficeTemplateId;
        } else {
            return applicationNotAdmittedOtherPartyHomeOfficeTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(homeOfficeEmailAddress);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_HOME_OFFICE_APPELLANT";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getHomeOfficeHeaderPersonalisation(asylumCase);
    }
}
