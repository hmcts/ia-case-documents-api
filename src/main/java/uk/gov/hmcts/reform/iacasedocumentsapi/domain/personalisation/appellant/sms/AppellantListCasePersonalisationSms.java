package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.sms;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.SmsNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;


@Service
public class AppellantListCasePersonalisationSms implements SmsNotificationPersonalisation {

    private final String appellantCaseListedSmsTemplateId;
    private final String legallyReppedAppellantCaseListedSmsTemplateId;
    private final DateTimeExtractor dateTimeExtractor;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;


    public AppellantListCasePersonalisationSms(
        @Value("${govnotify.template.caseListed.appellant.sms}") String appellantCaseListedSmsTemplateId,
        @Value("${govnotify.template.caseListed.legallyReppedAppellant.sms}") String legallyReppedAppellantCaseListedSmsTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        DateTimeExtractor dateTimeExtractor,
        HearingDetailsFinder hearingDetailsFinder,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantCaseListedSmsTemplateId = appellantCaseListedSmsTemplateId;
        this.legallyReppedAppellantCaseListedSmsTemplateId = legallyReppedAppellantCaseListedSmsTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return isAipJourney(asylumCase) ? appellantCaseListedSmsTemplateId : legallyReppedAppellantCaseListedSmsTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return isAipJourney(asylumCase) ?
            recipientsFinder.findAll(asylumCase, NotificationType.SMS) :
            recipientsFinder.findReppedAppellant(asylumCase, NotificationType.SMS);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_AIP_APPELLANT_SMS";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        HearingCentre hearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class).orElseThrow(
            () -> new IllegalArgumentException("No hearing centre present"));
        return ImmutableMap
            .<String, String>builder()
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("Hyperlink to service", iaAipFrontendUrl)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase))
            .put("tribunalCentre", hearingCentre.getValue())
            .build();

    }
}
