package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class HomeOfficeEditListingPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseEditedTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private EmailAddressFinder emailAddressFinder;

    public HomeOfficeEditListingPersonalisation(
        @Value("${govnotify.template.homeOfficeCaseEditedTemplateId}") String homeOfficeCaseEditedTemplateId,
        EmailAddressFinder emailAddressFinder,
        PersonalisationProvider personalisationProvider
    ) {
        this.homeOfficeCaseEditedTemplateId = homeOfficeCaseEditedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeCaseEditedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getEditCaseListingPersonalisation(callback);
    }
}
