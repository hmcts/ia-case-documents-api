package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class CaseOfficerFtpaDecisionPersonalisation implements EmailNotificationPersonalisation {

    private final String  applicationReheardTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerFtpaDecisionPersonalisation(
        @Value("${govnotify.template.applicationReheard.caseOfficer.email}") String applicationReheardTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder) {
        this.applicationReheardTemplateId = applicationReheardTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return applicationReheardTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_APPLICATION_DECISION_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        return this.personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);
    }
}
