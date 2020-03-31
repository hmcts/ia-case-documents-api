package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;

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
public class RespondentAppellantFtpaSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final PersonalisationProvider personalisationProvider;
    private final String applyForFtpaTemplateId;
    private final String respondentEmailAddress;

    public RespondentAppellantFtpaSubmittedPersonalisation(
        @Value("${govnotify.template.applyForFtpa.other.email}") String applyForFtpaTemplateId,
        PersonalisationProvider personalisationProvider,
        @Value("${ftpaSubmitted.respondentEmailAddress}") String respondentEmailAddress
    ) {
        this.applyForFtpaTemplateId = applyForFtpaTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.respondentEmailAddress = respondentEmailAddress;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_RESPONDENT_APPELLANT_FTPA_SUBMITTED";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentEmailAddress);
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        return personalisationProvider.getPersonalisation(callback);
    }

    @Override
    public String getTemplateId() {
        return applyForFtpaTemplateId;
    }
}
