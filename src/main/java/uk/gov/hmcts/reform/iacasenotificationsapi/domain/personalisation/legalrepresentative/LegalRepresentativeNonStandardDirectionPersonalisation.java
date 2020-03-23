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
public class LegalRepresentativeNonStandardDirectionPersonalisation implements EmailNotificationPersonalisation {

    private static final String legalRepNonStandardDirectionSuffix = "_LEGAL_REP_NON_STANDARD_DIRECTION";

    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;
    private final String legalRepresentativeNonStandardDirectionTemplateId;


    public LegalRepresentativeNonStandardDirectionPersonalisation(
        @Value("${govnotify.template.nonStandardDirection.legalRep.email}") String legalRepresentativeNonStandardDirectionTemplateId,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder) {

        this.legalRepresentativeNonStandardDirectionTemplateId = legalRepresentativeNonStandardDirectionTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }

    @Override
    public String getTemplateId() {
        return legalRepresentativeNonStandardDirectionTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + legalRepNonStandardDirectionSuffix;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getPersonalisation(callback);
    }
}
