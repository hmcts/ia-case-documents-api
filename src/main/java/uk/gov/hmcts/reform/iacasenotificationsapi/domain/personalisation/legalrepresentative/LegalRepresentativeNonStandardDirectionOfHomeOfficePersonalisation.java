package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
public class LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String legalRepNonStandardDirectionOfHomeOfficeBeforeListingTemplateId;
    private final String legalRepNonStandardDirectionOfHomeOfficeAfterListingTemplateId;
    private final String iaExUiFrontendUrl;
    private final PersonalisationProvider personalisationProvider;
    private final CustomerServicesProvider customerServicesProvider;

    public LegalRepresentativeNonStandardDirectionOfHomeOfficePersonalisation(
        @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeBeforeListing.legalRep.email}") String legalRepNonStandardDirectionOfHomeOfficeBeforeListingTemplateId,
        @Value("${govnotify.template.nonStandardDirectionOfHomeOfficeAfterListing.legalRep.email}") String legalRepNonStandardDirectionOfHomeOfficeAfterListingTemplateId,
        @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
        PersonalisationProvider personalisationProvider,
        CustomerServicesProvider customerServicesProvider
    ) {
        this.legalRepNonStandardDirectionOfHomeOfficeBeforeListingTemplateId = legalRepNonStandardDirectionOfHomeOfficeBeforeListingTemplateId;
        this.legalRepNonStandardDirectionOfHomeOfficeAfterListingTemplateId = legalRepNonStandardDirectionOfHomeOfficeAfterListingTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.personalisationProvider = personalisationProvider;
        this.customerServicesProvider = customerServicesProvider;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
            ? legalRepNonStandardDirectionOfHomeOfficeAfterListingTemplateId : legalRepNonStandardDirectionOfHomeOfficeBeforeListingTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_LEGAL_REP_NON_STANDARD_DIRECTION_OF_HOME_OFFICE";
    }

    @Override
    public Map<String, String> getPersonalisation(Callback<AsylumCase> callback) {
        requireNonNull(callback, "callback must not be null");

        final ImmutableMap.Builder<String, String> listCaseFields = ImmutableMap
            .<String, String>builder()
            .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
            .put("linkToOnlineService", iaExUiFrontendUrl)
            .putAll(personalisationProvider.getPersonalisation(callback));

        return listCaseFields.build();
    }

    protected boolean isAppealListed(AsylumCase asylumCase) {
        final Optional<HearingCentre> appealListed = asylumCase
            .read(AsylumCaseDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.class);

        return appealListed.isPresent();
    }
}
