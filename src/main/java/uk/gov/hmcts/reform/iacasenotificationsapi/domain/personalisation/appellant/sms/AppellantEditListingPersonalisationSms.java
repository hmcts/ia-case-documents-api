package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantEditListingPersonalisationSms implements SmsNotificationPersonalisation {

    private final String editListingAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final RecipientsFinder recipientsFinder;


    public AppellantEditListingPersonalisationSms(
        @Value("${govnotify.template.caseEdited.appellant.sms}") String editListingAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        PersonalisationProvider personalisationProvider,
        RecipientsFinder recipientsFinder
    ) {
        this.editListingAppellantSmsTemplateId = editListingAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.recipientsFinder = recipientsFinder;

    }


    @Override
    public String getTemplateId() {
        return editListingAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("hyperlink to service", iaAipFrontendUrl);

        return listCaseFields.build();
    }
}
