package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REPRESENTATIVE_EMAIL_ADDRESS;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeEditListingPersonalisation implements EmailNotificationPersonalisation {

    private final String legalRepresentativeCaseEditedTemplateId;
    private final PersonalisationProvider personalisationProvider;

    public LegalRepresentativeEditListingPersonalisation(
        @Value("${govnotify.template.legalRepresentativeCaseEditedTemplateId}") String legalRepresentativeCaseEditedTemplateId,
        PersonalisationProvider personalisationProvider
    ) {
        this.legalRepresentativeCaseEditedTemplateId = legalRepresentativeCaseEditedTemplateId;
        this.personalisationProvider = personalisationProvider;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeCaseEditedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(asylumCase
            .read(LEGAL_REPRESENTATIVE_EMAIL_ADDRESS, String.class)
            .orElseThrow(() -> new IllegalStateException("legalRepresentativeEmailAddress is not present")));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getEditCaseListingPersonalisation(callback);
    }
}
