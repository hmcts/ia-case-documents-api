package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.appellant.email;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;
import static uk.gov.hmcts.reform.iacasedocumentsapi.domain.utils.AsylumCaseUtils.isAipJourney;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.NotificationType;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.service.RecipientsFinder;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.HearingDetailsFinder;


@Service
public class AppellantListCasePersonalisationEmail implements EmailNotificationPersonalisation {

    private final String appellantCaseListedTemplateId;
    private final String listAssistHearingAppellantCaseListedTemplateId;
    private final String legallyReppedAppellantCaseListedTemplateId;
    private final String listAssistHearingLegallyReppedAppellantCaseListedTemplateId;
    private final DateTimeExtractor dateTimeExtractor;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;
    private final RecipientsFinder recipientsFinder;
    private final String iaAipFrontendUrl;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public AppellantListCasePersonalisationEmail(
        @Value("${govnotify.template.caseListed.appellant.email}") String appellantCaseListedEmailTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseListed.appellant.email}") String listAssistHearingAppellantCaseListedTemplateId,
        @Value("${govnotify.template.caseListed.legallyReppedAppellant.email}") String legallyReppedAppellantCaseListedTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseListed.legallyReppedAppellant.email}") String listAssistHearingLegallyReppedAppellantCaseListedTemplateId,
        @Value("${iaAipFrontendUrl}") String iaAipFrontendUrl,
        DateTimeExtractor dateTimeExtractor,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder,
        RecipientsFinder recipientsFinder
    ) {
        this.appellantCaseListedTemplateId = appellantCaseListedEmailTemplateId;
        this.listAssistHearingAppellantCaseListedTemplateId = listAssistHearingAppellantCaseListedTemplateId;
        this.legallyReppedAppellantCaseListedTemplateId = legallyReppedAppellantCaseListedTemplateId;
        this.listAssistHearingLegallyReppedAppellantCaseListedTemplateId = listAssistHearingLegallyReppedAppellantCaseListedTemplateId;
        this.iaAipFrontendUrl = iaAipFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
        this.recipientsFinder = recipientsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        if (asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
            return isAipJourney(asylumCase) ?
                listAssistHearingAppellantCaseListedTemplateId :
                listAssistHearingLegallyReppedAppellantCaseListedTemplateId;
        } else {
            return isAipJourney(asylumCase) ?
                appellantCaseListedTemplateId : legallyReppedAppellantCaseListedTemplateId;
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
        return caseId + "_CASE_LISTED_AIP_APPELLANT_EMAIL";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");
        HearingCentre hearingCentre = asylumCase.read(HEARING_CENTRE, HearingCentre.class).orElseThrow(
            () -> new IllegalArgumentException("No hearing centre present"));
        return ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("Hyperlink to service", iaAipFrontendUrl)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase))
            .put("tribunalCentre", hearingCentre.getValue())
            .build();


    }
}
