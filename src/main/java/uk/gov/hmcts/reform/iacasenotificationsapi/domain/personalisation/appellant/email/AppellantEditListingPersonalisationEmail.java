package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantEditListingPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String editListingAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;


    public AppellantEditListingPersonalisationEmail(
        @Value("${govnotify.template.caseEdited.appellant.email}") String editListingAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder
    ) {
        this.editListingAppellantEmailTemplateId = editListingAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return editListingAppellantEmailTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("hyperlink to service", iaAipFrontendUrl);

        return listCaseFields.build();
    }
}
