package uk.gov.hmcts.reform.iacasenotificationsapi.domain.personalisation.legalrepresentative;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacasenotificationsapi.domain.utils.AsylumCaseUtils.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import javax.validation.constraints.NotNull;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacasenotificationsapi.domain.entities.AsylumCaseDefinition;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.DetentionFacilityNameFinder;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.CustomerServicesProvider;
import uk.gov.hmcts.reform.iacasenotificationsapi.infrastructure.PersonalisationProvider;

@Service
@Slf4j
public class LegalRepresentativeUpdateDetentionLocationPersonalisation implements LegalRepresentativeEmailNotificationPersonalisation {

    private final String updateDetentionLocationBeforeListingAppellantTemplateId;
    private final String updateDetentionLocationAfterListingAppellantTemplateId;
    private final String iaExUiFrontendUrl;
    private final CustomerServicesProvider customerServicesProvider;
    private final PersonalisationProvider personalisationProvider;
    private final DetentionFacilityNameFinder detentionFacilityNameFinder;

    @Value("${govnotify.emailPrefix.ada}")
    private String adaPrefix;
    @Value("${govnotify.emailPrefix.nonAda}")
    private String nonAdaPrefix;

    public LegalRepresentativeUpdateDetentionLocationPersonalisation(
            @NotNull(message = "updateDetentionLocationBeforeListingAppellantTemplateId cannot be null")
            @Value("${govnotify.template.updateDetentionLocation.legalRep.beforeListing.email}") String updateDetentionLocationBeforeListingAppellantTemplateId,
            @NotNull(message = "updateDetentionLocationAfterListingAppellantTemplateId cannot be null")
            @Value("${govnotify.template.updateDetentionLocation.legalRep.afterListing.email}") String updateDetentionLocationAfterListingAppellantTemplateId,
            @Value("${iaExUiFrontendUrl}") String iaExUiFrontendUrl,
            CustomerServicesProvider customerServicesProvider,
            PersonalisationProvider personalisationProvider,
            DetentionFacilityNameFinder detentionFacilityNameFinder
    ) {
        this.updateDetentionLocationBeforeListingAppellantTemplateId = updateDetentionLocationBeforeListingAppellantTemplateId;
        this.updateDetentionLocationAfterListingAppellantTemplateId = updateDetentionLocationAfterListingAppellantTemplateId;
        this.iaExUiFrontendUrl = iaExUiFrontendUrl;
        this.customerServicesProvider = customerServicesProvider;
        this.personalisationProvider = personalisationProvider;
        this.detentionFacilityNameFinder = detentionFacilityNameFinder;
    }

    @Override
    public String getTemplateId(AsylumCase asylumCase) {
        return isAppealListed(asylumCase)
                ? updateDetentionLocationAfterListingAppellantTemplateId : updateDetentionLocationBeforeListingAppellantTemplateId;
    }

    @Override
    public String getReferenceId(Long caseId) {
        return caseId + "_UPDATE_DETENTION_LOCATION_LEGAL_REP";
    }

    @Override
    public Map<String, String> getPersonalisation(AsylumCase asylumCase) {
        requireNonNull(asylumCase, "asylumCase must not be null");

        log.info("Sending email notification for update detention location");
        
        String previousDetentionLocationName = asylumCase.read(AsylumCaseDefinition.PREVIOUS_DETENTION_LOCATION, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Previous Detention location is missing"));
        String newDetentionFacilityName = getDetentionFacilityName(asylumCase);

        String detentionFacility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElse("");

        String oldDetentionLocation = "";
        String newDetentionLocation = "";

        if (detentionFacility.equals("other")) {
            oldDetentionLocation = previousDetentionLocationName;
            newDetentionLocation = newDetentionFacilityName;
        } else {
            oldDetentionLocation = detentionFacilityNameFinder.getDetentionFacility(previousDetentionLocationName);
            newDetentionLocation = detentionFacilityNameFinder.getDetentionFacility(newDetentionFacilityName);
        }

        return ImmutableMap
                .<String, String>builder()
                .putAll(customerServicesProvider.getCustomerServicesPersonalisation())
                .putAll(personalisationProvider.getLegalRepHeaderPersonalisation(asylumCase))
                .put("subjectPrefix", isAcceleratedDetainedAppeal(asylumCase) ? adaPrefix : nonAdaPrefix)
                .put("oldDetentionLocation", oldDetentionLocation)
                .put("newDetentionLocation", newDetentionLocation)
                .put("linkToOnlineService", iaExUiFrontendUrl)
                .build();
    }
}
