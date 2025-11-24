package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.IS_INTEGRATED;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LEGAL_REP_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.HearingDetailsFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeListCasePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepresentativeCaseListedNonAdaTemplateId;
    private final String legalRepresentativeCaseListedAdaTemplateId;
    private final String legalRepresentativeOutOfCountryCaseListedTemplateId;
    private final String listAssistHearingLegalRepresentativeCaseListedTemplateId;
    private final String listAssistHearingLegalRepresentativeOutOfCountryCaseListedTemplateId;
    private final String iaExUiFrontendUrl;
    private final int appellantProvidingAppealArgumentDeadlineDelay;
    private final int respondentResponseToAppealArgumentDeadlineDelay;
    private final DateTimeExtractor dateTimeExtractor;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeListCasePersonalisation(
        @Value("${govnotify.template.caseListed.legalRep.email.nonAda}") String legalRepresentativeCaseListedNonAdaTemplateId,
        @Value("${govnotify.template.caseListed.legalRep.email.ada}") String legalRepresentativeCaseListedAdaTemplateId,
        @Value("${govnotify.template.caseListed.remoteHearing.legalRep.email}") String legalRepresentativeOutOfCountryCaseListedTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseListed.legalRep.email}") String listAssistHearingLegalRepresentativeCaseListedTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseListed.remoteHearing.legalRep.email}") String listAssistHearingLegalRepresentativeOutOfCountryCaseListedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        @Value("${adaCaseListed.deadlines.appellantProvidingAppealArgumentDelay}") int appellantProvidingAppealArgumentDeadlineDelay,
        @Value("${adaCaseListed.deadlines.respondentResponseToAppealArgumentDelay}") int respondentResponseToAppealArgumentDeadlineDelay,
        DateTimeExtractor dateTimeExtractor,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.legalRepresentativeCaseListedNonAdaTemplateId = legalRepresentativeCaseListedNonAdaTemplateId;
        this.legalRepresentativeCaseListedAdaTemplateId = legalRepresentativeCaseListedAdaTemplateId;
        this.legalRepresentativeOutOfCountryCaseListedTemplateId = legalRepresentativeOutOfCountryCaseListedTemplateId;
        this.listAssistHearingLegalRepresentativeCaseListedTemplateId = listAssistHearingLegalRepresentativeCaseListedTemplateId;
        this.listAssistHearingLegalRepresentativeOutOfCountryCaseListedTemplateId = listAssistHearingLegalRepresentativeOutOfCountryCaseListedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appellantProvidingAppealArgumentDeadlineDelay = appellantProvidingAppealArgumentDeadlineDelay;
        this.respondentResponseToAppealArgumentDeadlineDelay = respondentResponseToAppealArgumentDeadlineDelay;
        this.dateTimeExtractor = dateTimeExtractor;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        YesOrNo isIntegrated = asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO);
        if (asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)
            .map(centre -> centre == HearingCentre.REMOTE_HEARING)
            .orElse(false)) {
            return (isIntegrated == YesOrNo.YES ?
                    listAssistHearingLegalRepresentativeOutOfCountryCaseListedTemplateId : legalRepresentativeOutOfCountryCaseListedTemplateId);
        } else {
            return isAcceleratedDetainedAppeal(asylumCase)
                ? legalRepresentativeCaseListedAdaTemplateId
                : (isIntegrated == YesOrNo.YES ?
                listAssistHearingLegalRepresentativeCaseListedTemplateId : legalRepresentativeCaseListedNonAdaTemplateId);
        }
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_LEGAL_REPRESENTATIVE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("legalRepReferenceNumber", asylumCase.read(LEGAL_REP_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingTime", dateTimeExtractor.extractHearingTime(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase));

        if (isAcceleratedDetainedAppeal(asylumCase)) {
            listCaseFields
                .put("appellantProvidingAppealArgumentDeadline",
                    LocalDate.now().plusDays(appellantProvidingAppealArgumentDeadlineDelay)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")))
                .put("respondentResponseToAppealArgumentDeadline",
                    LocalDate.now().plusDays(respondentResponseToAppealArgumentDeadlineDelay)
                        .format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        }

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, listCaseFields);

        return listCaseFields.build();

    }
}
