package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAipJourney;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantEditListingPersonalisationSms implements SmsNotificationPersonalisation {

    private final String editListingAppellantSmsTemplateId;
    private final String editListingLegallyReppedAppellantSmsTemplateId;
    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final RecipientsFinder recipientsFinder;


    public AppellantEditListingPersonalisationSms(
        @Value("${govnotify.template.caseEdited.appellant.sms}") String editListingAppellantSmsTemplateId,
        @Value("${govnotify.template.caseEdited.legallyReppedAppellant.sms}") String editListingLegallyReppedAppellantSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        PersonalisationProvider personalisationProvider,
        RecipientsFinder recipientsFinder
    ) {
        this.editListingAppellantSmsTemplateId = editListingAppellantSmsTemplateId;
        this.editListingLegallyReppedAppellantSmsTemplateId = editListingLegallyReppedAppellantSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.recipientsFinder = recipientsFinder;

    }


    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return isAipJourney(asylumCase) ? editListingAppellantSmsTemplateId : editListingLegallyReppedAppellantSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return isAipJourney(asylumCase) ?
            recipientsFinder.findAll(asylumCase, NotificationType.SMS) :
            recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        HearingCentre hearingCentre = callback.getCaseDetails().getCaseData()
            .read(HEARING_CENTRE, HearingCentre.class).orElseThrow(
                () -> new IllegalArgumentException("No hearing centre present"));
        return ImmutableMap
            .<String, String>builder()
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("tribunalCentre", hearingCentre.getValue())
            .put("hyperlink to service", iaAipFrontendUrl)
            .build();
    }
}
