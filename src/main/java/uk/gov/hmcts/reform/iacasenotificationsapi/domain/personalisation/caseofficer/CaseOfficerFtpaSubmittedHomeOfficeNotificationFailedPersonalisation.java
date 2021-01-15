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
public class CaseOfficerFtpaSubmittedHomeOfficeNotificationFailedPersonalisation implements EmailNotificationPersonalisation {

    private final String ftpaSubmittedHomeOfficeNotificationFailedTemplateId;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;

    public CaseOfficerFtpaSubmittedHomeOfficeNotificationFailedPersonalisation(
        @NotNull(message = "ftpaSubmittedHomeOfficeNotificationFailedTemplateId cannot be null") @Value("${govnotify.template.applyForFtpa.caseOfficer.email}") String ftpaSubmittedHomeOfficeNotificationFailedTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder
    ) {

        this.ftpaSubmittedHomeOfficeNotificationFailedTemplateId = ftpaSubmittedHomeOfficeNotificationFailedTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return ftpaSubmittedHomeOfficeNotificationFailedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHearingCentreEmailAddress(asylumCase));

    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_SUBMITTED_HO_NOTIFICATION_FAILED_CASE_OFFICER";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {

        return this.personalisationProvider.getTribunalHeaderPersonalisation(asylumCase);
    }
}
