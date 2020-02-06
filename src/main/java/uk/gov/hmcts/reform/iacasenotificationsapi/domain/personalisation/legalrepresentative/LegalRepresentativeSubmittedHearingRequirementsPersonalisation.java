package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class LegalRepresentativeSubmittedHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final EmailAddressFinder emailAddressFinder;
    private final PersonalisationProvider personalisationProvider;

    public LegalRepresentativeSubmittedHearingRequirementsPersonalisation(
        PersonalisationProvider personalisationProvider,
        GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
        EmailAddressFinder emailAddressFinder
    ) {
        this.personalisationProvider = personalisationProvider;
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return govNotifyTemplateIdConfiguration.getSubmittedHearingRequirementsLegalRepTemplateId();
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REP_OF_SUBMITTED_HEARING_REQUIREMENTS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        return personalisationProvider.getSubmittedHearingRequirementsPersonalisation(asylumCase);
    }

}
