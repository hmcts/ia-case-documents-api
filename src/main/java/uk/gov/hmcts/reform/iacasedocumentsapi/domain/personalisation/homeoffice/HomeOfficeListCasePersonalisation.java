package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.*;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.isAcceleratedDetainedAppeal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.*;

@Service
public class HomeOfficeListCasePersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseListedNonAdaTemplateId;
    private final String homeOfficeCaseListedAdaTemplateId;
    private final String listAssistHearingHomeOfficeCaseListedTemplateId;
    private final String iaExUiFrontendUrl;
    private final int appellantProvidingAppealArgumentDeadlineDelay;
    private final int respondentResponseToAppealArgumentDeadlineDelay;
    private final DateTimeExtractor dateTimeExtractor;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    public HomeOfficeListCasePersonalisation(
        @Value("${govnotify.template.caseListed.homeOffice.email.nonAda}") String homeOfficeCaseListedNonAdaTemplateId,
        @Value("${govnotify.template.caseListed.homeOffice.email.ada}") String homeOfficeCaseListedAdaTemplateId,
        @Value("${govnotify.template.listAssistHearing.caseListed.homeOffice.email}") String listAssistHearingHomeOfficeCaseListedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        @Value("${adaCaseListed.deadlines.appellantProvidingAppealArgumentDelay}") int appellantProvidingAppealArgumentDeadlineDelay,
        @Value("${adaCaseListed.deadlines.respondentResponseToAppealArgumentDelay}") int respondentResponseToAppealArgumentDeadlineDelay,
        DateTimeExtractor dateTimeExtractor,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.homeOfficeCaseListedNonAdaTemplateId = homeOfficeCaseListedNonAdaTemplateId;
        this.homeOfficeCaseListedAdaTemplateId = homeOfficeCaseListedAdaTemplateId;
        this.listAssistHearingHomeOfficeCaseListedTemplateId = listAssistHearingHomeOfficeCaseListedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.appellantProvidingAppealArgumentDeadlineDelay = appellantProvidingAppealArgumentDeadlineDelay;
        this.respondentResponseToAppealArgumentDeadlineDelay = respondentResponseToAppealArgumentDeadlineDelay;
        this.dateTimeExtractor = dateTimeExtractor;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {

        return AsylumCaseUtils.isAcceleratedDetainedAppeal(asylumCase)
            ? homeOfficeCaseListedAdaTemplateId
            : asylumCase.read(IS_INTEGRATED, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES
            ? listAssistHearingHomeOfficeCaseListedTemplateId : homeOfficeCaseListedNonAdaTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_CASE_LISTED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
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
