package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.EmailAddressFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.config.GovNotifyTemplateIdConfiguration;

@Service
public class LegalRepresentativeUploadAdditionalEvidencePersonalisation implements EmailNotificationPersonalisation {

    private final GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration;
    private final PersonalisationProvider personalisationProvider;
    private final EmailAddressFinder emailAddressFinder;

    public LegalRepresentativeUploadAdditionalEvidencePersonalisation(
        GovNotifyTemplateIdConfiguration govNotifyTemplateIdConfiguration,
        PersonalisationProvider personalisationProvider,
        EmailAddressFinder emailAddressFinder
    ) {
        this.govNotifyTemplateIdConfiguration = govNotifyTemplateIdConfiguration;
        this.personalisationProvider = personalisationProvider;
        this.emailAddressFinder = emailAddressFinder;
    }


    @Override
    public String getTemplateId() {
        return govNotifyTemplateIdConfiguration.getUploadedAdditionalEvidenceTemplateId();
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getLegalRepEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPLOADED_ADDITIONAL_EVIDENCE_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getUploadAdditionalEvidencePersonalisation(callback.getCaseDetails().getCaseData());

    }
}
