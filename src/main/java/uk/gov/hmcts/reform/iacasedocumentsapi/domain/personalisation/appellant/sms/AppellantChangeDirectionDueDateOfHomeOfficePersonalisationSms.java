package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;


@Service
public class AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms implements SmsNotificationPersonalisation {

    private final String smsTemplateId;
    private final PersonalisationProvider personalisationProvider;
    private final RecipientsFinder recipientsFinder;

    public AppellantChangeDirectionDueDateOfHomeOfficePersonalisationSms(
        @Value("${govnotify.template.changeDirectionDueDateOfHomeOffice.appellant.sms}") String smsTemplateId,
        PersonalisationProvider personalisationProvider,
        RecipientsFinder recipientsFinder
    ) {
        this.smsTemplateId = smsTemplateId;
        this.personalisationProvider = personalisationProvider;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId() {
        return smsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        return recipientsFinder.findAll(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_APPELLANT_CHANGE_DIRECTION_DUE_DATE_OF_HOME_OFFICE_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
                .put("Appeal Ref Number", asylumCase.read(AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }
}
