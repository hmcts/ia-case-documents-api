package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.caseofficer;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class CaseOfficerAppealOutcomeHomeOfficeNotificationFailedPersonalisation implements EmailNotificationPersonalisation {

    private final String appealOutcomeCaseOfficerNotificationFailedTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;

    public CaseOfficerAppealOutcomeHomeOfficeNotificationFailedPersonalisation(
        @NotNull(message = "appealOutcomeHomeOfficeNotificationFailedTemplateId cannot be null")
        @Value("${govnotify.template.decisionHomeOfficeNotificationFailed.caseOfficer.email}") String appealOutcomeHomeOfficeNotificationFailedTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder
    ) {

        this.appealOutcomeCaseOfficerNotificationFailedTemplateId = appealOutcomeHomeOfficeNotificationFailedTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return appealOutcomeCaseOfficerNotificationFailedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPEAL_OUTCOME_HO_NOTIFICATION_FAILED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return this.personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);
    }
}
