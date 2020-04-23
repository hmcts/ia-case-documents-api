package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.respondent;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class RespondentFtpaSubmittedPersonalisation implements EmailNotificationPersonalisation {

    private final String ftpaSubmittedTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final String respondentEmailAddress;
    private final CustomerServicesProvider customerServicesProvider;

    public RespondentFtpaSubmittedPersonalisation(
        @Value("${govnotify.template.applyForFtpa.respondent.email}") String ftpaSubmittedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        @Value("${ftpaSubmitted.respondentEmailAddress}") String respondentEmailAddress,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.ftpaSubmittedTemplateId = ftpaSubmittedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.respondentEmailAddress = respondentEmailAddress;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_FTPA_SUBMITTED_RESPONDENT";
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(respondentEmailAddress);
    }

    @Override
    public String getTemplateId() {
        return ftpaSubmittedTemplateId;
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }
}
