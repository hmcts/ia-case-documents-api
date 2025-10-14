package uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.homeoffice;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.ARIA_LISTING_REFERENCE;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.HOME_OFFICE_REFERENCE_NUMBER;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.DateTimeExtractor;
import uk.gov.hmcts.reform.iacasedocumentsapi.infrastructure.PersonalisationProvider;
import uk.gov.hmcts.reform.iacasedocumentsapi.domain.personalisation.EmailNotificationPersonalisation;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.*;

@Service
public class HomeOfficeAdaReviewHearingRequirementsPersonalisation implements EmailNotificationPersonalisation {

    private final String homeOfficeCaseListedTemplateId;
    private final String iaExUiFrontendUrl;
    private final DateTimeExtractor dateTimeExtractor;
    private final EmailAddressFinder emailAddressFinder;
    private final CustomerServicesProvider customerServicesProvider;
    private final HearingDetailsFinder hearingDetailsFinder;

    public HomeOfficeAdaReviewHearingRequirementsPersonalisation(
        @Value("${govnotify.template.reviewHearingRequirements.homeOffice.ada.email}") String homeOfficeCaseListedTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        DateTimeExtractor dateTimeExtractor,
        EmailAddressFinder emailAddressFinder,
        CustomerServicesProvider customerServicesProvider,
        HearingDetailsFinder hearingDetailsFinder
    ) {
        this.homeOfficeCaseListedTemplateId = homeOfficeCaseListedTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.dateTimeExtractor = dateTimeExtractor;
        this.emailAddressFinder = emailAddressFinder;
        this.customerServicesProvider = customerServicesProvider;
        this.hearingDetailsFinder = hearingDetailsFinder;
    }

    @Override
    public String getTemplateId() {
        return homeOfficeCaseListedTemplateId;
    }

    @Override
    public Set<String> getRecipientsList(AsylumCase asylumCase) {
        return Collections.singleton(emailAddressFinder.getListCaseHomeOfficeEmailAddress(asylumCase));
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_HEARING_REQUIREMENTS_AGREED_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        final Builder<String, String> hearingRequirementFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("appealReferenceNumber", asylumCase.read(APPEAL_REFERENCE_NUMBER, String.class).orElse(""))
            .put("ariaListingReference", asylumCase.read(ARIA_LISTING_REFERENCE, String.class).orElse(""))
            .put("homeOfficeReferenceNumber", asylumCase.read(HOME_OFFICE_REFERENCE_NUMBER, String.class).orElse(""))
            .put("appellantGivenNames", asylumCase.read(APPELLANT_GIVEN_NAMES, String.class).orElse(""))
            .put("appellantFamilyName", asylumCase.read(APPELLANT_FAMILY_NAME, String.class).orElse(""))
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .put("hearingDate", dateTimeExtractor.extractHearingDate(hearingDetailsFinder.getHearingDateTime(asylumCase)))
            .put("hearingCentreAddress", hearingDetailsFinder.getHearingCentreLocation(asylumCase));

        PersonalisationProvider.buildHearingRequirementsFields(asylumCase, hearingRequirementFields);

        return hearingRequirementFields.build();
    }
}
