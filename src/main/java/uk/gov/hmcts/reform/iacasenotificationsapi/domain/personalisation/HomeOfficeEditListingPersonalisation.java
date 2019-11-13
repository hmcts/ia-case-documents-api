package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation;

import static java.util.Objects.requireNonNull;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.BasePersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;

@Service
public class HomeOfficeEditListingPersonalisation implements NotificationPersonalisation {

    private final String homeOfficeCaseEditedTemplateId;
    private final BasePersonalisationProvider basePersonalisationProvider;
    private EmailAddressFinder emailAddressFinder;

    public HomeOfficeEditListingPersonalisation(
        @Value("${govnotify.template.homeOfficeCaseEditedTemplateId}") String homeOfficeCaseEditedTemplateId,
        EmailAddressFinder emailAddressFinder,
        BasePersonalisationProvider basePersonalisationProvider
    ) {
        this.homeOfficeCaseEditedTemplateId = homeOfficeCaseEditedTemplateId;
        this.emailAddressFinder = emailAddressFinder;
        this.basePersonalisationProvider = basePersonalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeCaseEditedTemplateId;
    }

    @Override
    public String getEmailAddress(AsylumCase asylumCase) {
        return emailAddressFinder.getHomeOfficeEmailAddress(asylumCase);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return basePersonalisationProvider.getEditCaseListingPersonalisation(callback);
    }
}
