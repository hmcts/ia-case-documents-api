package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

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
public class LegalRepresentativeSubmittedHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String submittedHearingRequirementsLegalRepTemplateId;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;

    public LegalRepresentativeSubmittedHearingRequirementsPersonalisation(
        @Value("${govnotify.template.submittedHearingRequirements.legalRep.email}") String submittedHearingRequirementsLegalRepTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder
    ) {
        this.submittedHearingRequirementsLegalRepTemplateId = submittedHearingRequirementsLegalRepTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getPersonalisation(callback);
    }

    @Override
    public String getTemplateId() {
        return submittedHearingRequirementsLegalRepTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REP_OF_SUBMITTED_HEARING_REQUIREMENTS";
    }
}
