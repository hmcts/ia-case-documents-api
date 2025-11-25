package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
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
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;

@Service
public class AppellantEditListingPersonalisationEmail implements EmailNotificationPersonalisation {

    private final String editListingAppellantEmailTemplateId;
    private final String listAssistHearingEditListingAppellantEmailTemplateId;
    private final String editListingLegallyReppedAppellantEmailTemplateId;
    private final String listAssistHearingEditListingLegallyReppedAppellantEmailTemplateId;
    private final String iaAipFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;
    private final RecipientsFinder recipientsFinder;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantEditListingPersonalisationEmail(
        @Value("${govnotify.template.caseEdited.appellant.email}") String editListingAppellantEmailTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseEdited.appellant.email}") String listAssistHearingEditListingAppellantEmailTemplateId,
        @Value("${govnotify.template.caseEdited.legallyReppedAppellant.email}") String editListingLegallyReppedAppellantEmailTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseEdited.legallyReppedAppellant.email}") String listAssistHearingEditListingLegallyReppedAppellantEmailTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider,
        RecipientsFinder recipientsFinder,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.editListingAppellantEmailTemplateId = editListingAppellantEmailTemplateId;
        this.listAssistHearingEditListingAppellantEmailTemplateId = listAssistHearingEditListingAppellantEmailTemplateId;
        this.editListingLegallyReppedAppellantEmailTemplateId = editListingLegallyReppedAppellantEmailTemplateId;
        this.listAssistHearingEditListingLegallyReppedAppellantEmailTemplateId = listAssistHearingEditListingLegallyReppedAppellantEmailTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
        this.recipientsFinder = recipientsFinder;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return isAipJourney(asylumCase) ?
                listAssistHearingEditListingAppellantEmailTemplateId :
                listAssistHearingEditListingLegallyReppedAppellantEmailTemplateId;
        } else {
            return isAipJourney(asylumCase) ?
                editListingAppellantEmailTemplateId : editListingLegallyReppedAppellantEmailTemplateId;
        }
    }

    @Override
    public Set<String> getRecipientsList(final AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        return isAipJourney(asylumCase) ?
            recipientsFinder.findAll(asylumCase, NotificationType.EMAIL) :
            recipientsFinder.findReppedAppellant(asylumCase, NotificationType.EMAIL);
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_RE_LISTED_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");
        HearingCentre hearingCentre = callback.getCaseDetails().getCaseData()
            .read(HEARING_CENTRE, HearingCentre.class).orElseThrow(
                () -> new IllegalArgumentException("No hearing centre present"));
        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .putAll(personalisationProvider.getPersonalisation(callback))
            .put("subjectPrefix", isAcceleratedDetainedAppeal(callback.getCaseDetails().getCaseData()) ? adaPrefix : nonAdaPrefix)
            .put("tribunalCentre", hearingDetailsFinder.getHearingCentreName(callback.getCaseDetails().getCaseData()))
            .put("hyperlink to service", iaAipFrontendUrl)
            .build();
    }
}
